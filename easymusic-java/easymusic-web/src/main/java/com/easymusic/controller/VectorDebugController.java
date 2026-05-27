package com.easymusic.controller;

import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.po.MusicInfoAction;
import com.easymusic.entity.po.UserPreferenceProfile;
import com.easymusic.entity.query.MusicCreationQuery;
import com.easymusic.entity.query.MusicInfoActionQuery;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.mappers.MusicCreationMapper;
import com.easymusic.mappers.MusicInfoActionMapper;
import com.easymusic.mappers.MusicInfoMapper;
import com.easymusic.mappers.UserPreferenceProfileMapper;
import com.easymusic.service.MilvusService;
import com.easymusic.service.UserPreferenceService;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/vector")
public class VectorDebugController extends ABaseController {

    private static final Logger log = LoggerFactory.getLogger(VectorDebugController.class);

    @Resource
    @Lazy
    private MilvusService milvusService;

    @Resource
    @Lazy
    private UserPreferenceService userPreferenceService;

    @Resource
    @Lazy
    private MusicCreationMapper<MusicCreation, MusicCreationQuery> musicCreationMapper;

    @Resource
    @Lazy
    private MusicInfoMapper<MusicInfo, MusicInfoQuery> musicInfoMapper;

    @Resource
    @Lazy
    private MusicInfoActionMapper<MusicInfoAction, MusicInfoActionQuery> musicInfoActionMapper;

    @Resource
    @Lazy
    private UserPreferenceProfileMapper<UserPreferenceProfile, Object> profileMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    @RequestMapping("/status")
    public ResponseVO getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        try {
            // 1. MySQL Creation Count
            int mysqlCount = musicCreationMapper.selectCount(new MusicCreationQuery());
            status.put("mysqlCreationCount", mysqlCount);

            // 2. MySQL Preference Profiles
            List<UserPreferenceProfile> profiles = new ArrayList<>();
            // We fetch user preference profiles manually since there is no standard list query, or we can use a query with empty param
            // Let's get the list of active user IDs from music_creation to see if they have profiles
            List<MusicCreation> creations = musicCreationMapper.selectList(new MusicCreationQuery());
            Set<String> userIds = new HashSet<>();
            for (MusicCreation c : creations) {
                userIds.add(c.getUserId());
            }
            // Always check default user
            userIds.add("100000000000");

            List<Map<String, Object>> profileList = new ArrayList<>();
            for (String userId : userIds) {
                UserPreferenceProfile profile = profileMapper.selectByUserId(userId);
                if (profile != null) {
                    Map<String, Object> pMap = new LinkedHashMap<>();
                    pMap.put("userId", profile.getUserId());
                    pMap.put("preferenceText", profile.getPreferenceText());
                    pMap.put("lastBehaviorTime", profile.getLastBehaviorTime());
                    pMap.put("updateTime", profile.getUpdateTime());
                    pMap.put("lastActionId", profile.getLastActionId());
                    
                    byte[] vecBytes = profile.getPreferenceVector();
                    if (vecBytes != null) {
                        pMap.put("vectorLength", vecBytes.length);
                        float[] floatVec = profile.getVectorAsFloatArray();
                        pMap.put("vectorDimension", floatVec != null ? floatVec.length : 0);
                        if (floatVec != null && floatVec.length > 0) {
                            // Show first 5 dimensions as preview
                            List<Float> preview = new ArrayList<>();
                            for (int i = 0; i < Math.min(floatVec.length, 5); i++) {
                                preview.add(floatVec[i]);
                            }
                            pMap.put("vectorPreview", preview);
                        }
                    } else {
                        pMap.put("vectorLength", 0);
                    }
                    profileList.add(pMap);
                }
            }
            status.put("userPreferenceProfiles", profileList);
            status.put("polledUserIdsChecked", userIds);

            // 3. Milvus details
            status.put("embeddingModelClass", embeddingModel.getClass().getName());
            status.put("collectionName", "music_creation_vector");
            
            // Try to search some default vector to see if collection is loaded and queryable
            try {
                float[] dummyVector = new float[512];
                dummyVector[0] = 1.0f;
                List<MilvusService.MusicCreationSearchResult> searchRes = milvusService.searchSimilarCreations(dummyVector, 10);
                status.put("milvusConnectionOk", true);
                status.put("milvusSearchTestCount", searchRes != null ? searchRes.size() : 0);
            } catch (Exception e) {
                status.put("milvusConnectionOk", false);
                status.put("milvusError", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to query vector status", e);
            return getServerErrorResponseVO("Error: " + e.getMessage());
        }
        return getSuccessResponseVO(status);
    }

    @RequestMapping("/sync")
    public ResponseVO runSync() {
        try {
            log.info("Manual vector sync triggered via debug controller.");
            milvusService.syncExistingCreations();
            return getSuccessResponseVO("Creations synced from MySQL to Milvus successfully.");
        } catch (Exception e) {
            log.error("Failed to run manual sync", e);
            return getServerErrorResponseVO("Sync failed: " + e.getMessage());
        }
    }

    @RequestMapping("/mock-preference")
    public ResponseVO mockPreference(@RequestParam(defaultValue = "100000000000") String userId) {
        try {
            log.info("Mock preference action triggered for userId: {}", userId);

            // 1. Check if the user already has actions in music_info_action. If not, insert some mock actions from music_info table.
            MusicInfoActionQuery actionQuery = new MusicInfoActionQuery();
            actionQuery.setUserId(userId);
            int count = musicInfoActionMapper.selectCount(actionQuery);
            
            List<MusicInfo> musicList = musicInfoMapper.selectList(new MusicInfoQuery());
            if (musicList == null || musicList.isEmpty()) {
                return getServerErrorResponseVO("No music records found in music_info table. Please seed MySQL database first.");
            }

            int addedCount = 0;
            if (count == 0) {
                log.info("No existing actions for userId: {}. Seeding up to 5 mock liked actions.", userId);
                // Insert up to 5 liked actions
                int limit = Math.min(musicList.size(), 5);
                for (int i = 0; i < limit; i++) {
                    MusicInfo music = musicList.get(i);
                    // Check if already liked (just in case)
                    MusicInfoAction existingAction = musicInfoActionMapper.selectByMusicIdAndUserId(music.getMusicId(), userId);
                    if (existingAction == null) {
                        MusicInfoAction action = new MusicInfoAction();
                        action.setMusicId(music.getMusicId());
                        action.setUserId(userId);
                        action.setMusicUserId(music.getUserId());
                        action.setActionType(1); // 1 = Like/Good
                        musicInfoActionMapper.insert(action);
                        addedCount++;
                    }
                }
            } else {
                log.info("User {} already has {} actions. Merging or using existing.", userId, count);
            }

            // 2. Call userPreferenceService.updateUserProfile(userId) to trigger LLM preference summary and BGE vectorization
            log.info("Invoking updateUserProfile for userId: {}", userId);
            userPreferenceService.updateUserProfile(userId);

            UserPreferenceProfile profile = profileMapper.selectByUserId(userId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("message", "User preference profile mock actions added and profile updated.");
            result.put("mockActionsAdded", addedCount);
            if (profile != null) {
                result.put("preferenceText", profile.getPreferenceText());
                result.put("vectorLength", profile.getPreferenceVector() != null ? profile.getPreferenceVector().length : 0);
            }
            return getSuccessResponseVO(result);

        } catch (Exception e) {
            log.error("Failed to generate mock preference vector", e);
            return getServerErrorResponseVO("Failed: " + e.getMessage());
        }
    }

    @RequestMapping("/search")
    public ResponseVO testSearch(@RequestParam String query, @RequestParam(defaultValue = "5") Integer topK) {
        try {
            log.info("Testing semantic search for query: {}", query);
            // 1. Vectorize the query text using BGE small Chinese model
            float[] vector = embeddingModel.embed(query).content().vector();

            // 2. Search Milvus
            List<MilvusService.MusicCreationSearchResult> searchRes = milvusService.searchSimilarCreations(vector, topK);
            return getSuccessResponseVO(searchRes);
        } catch (Exception e) {
            log.error("Semantic search failed", e);
            return getServerErrorResponseVO("Search failed: " + e.getMessage());
        }
    }
}
