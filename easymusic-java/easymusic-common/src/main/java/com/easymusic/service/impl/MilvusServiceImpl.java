package com.easymusic.service.impl;

import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.query.MusicCreationQuery;
import com.easymusic.mappers.MusicCreationMapper;
import com.easymusic.service.MilvusService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.param.RpcStatus;
import io.milvus.grpc.MutationResult;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("milvusService")
public class MilvusServiceImpl implements MilvusService {

    private static final Logger log = LoggerFactory.getLogger(MilvusServiceImpl.class);

    private static final String COLLECTION_NAME = "music_creation_vector";

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private Integer port;

    @Resource
    @Lazy
    private MusicCreationMapper<MusicCreation, MusicCreationQuery> musicCreationMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    private MilvusServiceClient milvusClient;

    @PostConstruct
    public void init() {
        log.info("Initializing Milvus client connecting to {}:{}...", host, port);
        try {
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withHost(host)
                    .withPort(port)
                    .build();
            milvusClient = new MilvusServiceClient(connectParam);
            log.info("Milvus client initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize Milvus client", e);
        }
    }

    @Override
    public void initCollection() {
        if (milvusClient == null) {
            log.error("Milvus client not initialized, skipping collection initialization.");
            return;
        }

        try {
            log.info("Checking if Milvus collection '{}' exists...", COLLECTION_NAME);
            R<Boolean> hasCollection = milvusClient.hasCollection(
                    HasCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build()
            );

            if (hasCollection.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to check collection existence: {}", hasCollection.getMessage());
                return;
            }

            if (!hasCollection.getData()) {
                log.info("Milvus collection '{}' does not exist. Creating...", COLLECTION_NAME);

                FieldType fieldType1 = FieldType.newBuilder()
                        .withName("creation_id")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(50)
                        .withPrimaryKey(true)
                        .withAutoID(false)
                        .build();

                FieldType fieldType2 = FieldType.newBuilder()
                        .withName("user_id")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(50)
                        .build();

                FieldType fieldType3 = FieldType.newBuilder()
                        .withName("prompt")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(500)
                        .build();

                FieldType fieldType4 = FieldType.newBuilder()
                        .withName("settings")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(1000)
                        .build();

                FieldType fieldType5 = FieldType.newBuilder()
                        .withName("vector")
                        .withDataType(DataType.FloatVector)
                        .withDimension(512)
                        .build();

                CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                        .withCollectionName(COLLECTION_NAME)
                        .withDescription("Music creation prompt vectors")
                        .addFieldType(fieldType1)
                        .addFieldType(fieldType2)
                        .addFieldType(fieldType3)
                        .addFieldType(fieldType4)
                        .addFieldType(fieldType5)
                        .build();

                R<RpcStatus> createRes = milvusClient.createCollection(createParam);
                if (createRes.getStatus() != R.Status.Success.getCode()) {
                    log.error("Failed to create collection: {}", createRes.getMessage());
                    return;
                }

                log.info("Creating index for collection '{}'...", COLLECTION_NAME);
                CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                        .withCollectionName(COLLECTION_NAME)
                        .withFieldName("vector")
                        .withIndexName("idx_vector")
                        .withMetricType(MetricType.COSINE)
                        .withIndexType(IndexType.IVF_FLAT)
                        .withExtraParam("{\"nlist\":128}")
                        .build();

                R<RpcStatus> indexRes = milvusClient.createIndex(indexParam);
                if (indexRes.getStatus() != R.Status.Success.getCode()) {
                    log.error("Failed to create index: {}", indexRes.getMessage());
                    return;
                }
            }

            log.info("Loading collection '{}' into memory...", COLLECTION_NAME);
            R<RpcStatus> loadRes = milvusClient.loadCollection(
                    LoadCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build()
            );
            if (loadRes.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to load collection: {}", loadRes.getMessage());
            } else {
                log.info("Collection '{}' loaded successfully.", COLLECTION_NAME);
            }

        } catch (Exception e) {
            log.error("Error initializing Milvus collection", e);
        }
    }

    @Override
    public void syncExistingCreations() {
        if (milvusClient == null) {
            log.error("Milvus client not initialized, skipping sync.");
            return;
        }

        try {
            log.info("Querying existing creation IDs from Milvus...");
            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withExpr("creation_id != \"\"")
                    .addOutField("creation_id")
                    .build();

            R<QueryResults> queryRes = milvusClient.query(queryParam);
            Set<String> existingIdsInMilvus = new HashSet<>();
            if (queryRes.getStatus() == R.Status.Success.getCode() && queryRes.getData() != null) {
                QueryResultsWrapper wrapper = new QueryResultsWrapper(queryRes.getData());
                List<QueryResultsWrapper.RowRecord> rows = wrapper.getRowRecords();
                for (QueryResultsWrapper.RowRecord row : rows) {
                    existingIdsInMilvus.add(row.get("creation_id").toString());
                }
            }

            log.info("Found {} records in Milvus.", existingIdsInMilvus.size());

            MusicCreationQuery query = new MusicCreationQuery();
            List<MusicCreation> allMySQLCreations = musicCreationMapper.selectList(query);
            log.info("Found {} creations in MySQL.", allMySQLCreations.size());

            int syncCount = 0;
            for (MusicCreation creation : allMySQLCreations) {
                if (!existingIdsInMilvus.contains(creation.getCreationId())) {
                    String prompt = creation.getPrompt();
                    if (prompt == null || prompt.trim().isEmpty()) {
                        continue;
                    }
                    // Generate vector
                    Embedding embedding = embeddingModel.embed(prompt).content();
                    float[] vector = embedding.vector();

                    insertCreation(
                            creation.getCreationId(),
                            creation.getUserId(),
                            prompt,
                            creation.getSettings() != null ? creation.getSettings() : "",
                            vector
                    );
                    syncCount++;
                }
            }

            log.info("Synced {} new creations from MySQL to Milvus.", syncCount);

        } catch (Exception e) {
            log.error("Error syncing creations from MySQL to Milvus", e);
        }
    }

    @Override
    public void insertCreation(String creationId, String userId, String prompt, String settings, float[] vector) {
        if (milvusClient == null) {
            log.error("Milvus client not initialized, skipping insert.");
            return;
        }

        try {
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("creation_id", Collections.singletonList(creationId)));
            fields.add(new InsertParam.Field("user_id", Collections.singletonList(userId)));
            fields.add(new InsertParam.Field("prompt", Collections.singletonList(prompt)));
            fields.add(new InsertParam.Field("settings", Collections.singletonList(settings)));

            List<List<Float>> vectors = new ArrayList<>();
            List<Float> vectorList = new ArrayList<>();
            for (float f : vector) {
                vectorList.add(f);
            }
            vectors.add(vectorList);
            fields.add(new InsertParam.Field("vector", vectors));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withFields(fields)
                    .build();

            R<MutationResult> res = milvusClient.insert(insertParam);
            if (res.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to insert creation vector into Milvus: {}", res.getMessage());
            } else {
                log.debug("Successfully inserted creation vector into Milvus for creationId: {}", creationId);
            }
        } catch (Exception e) {
            log.error("Failed to insert vector into Milvus", e);
        }
    }

    @Override
    public List<MusicCreationSearchResult> searchSimilarCreations(float[] vector, int topK) {
        List<MusicCreationSearchResult> results = new ArrayList<>();
        if (milvusClient == null) {
            log.error("Milvus client not initialized, skipping search.");
            return results;
        }

        try {
            List<Float> vectorList = new ArrayList<>();
            for (float f : vector) {
                vectorList.add(f);
            }

            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withMetricType(MetricType.COSINE)
                    .withOutFields(Arrays.asList("creation_id", "user_id", "prompt", "settings"))
                    .withTopK(topK)
                    .withVectors(Collections.singletonList(vectorList))
                    .withParams("{\"nprobe\":10}")
                    .build();

            R<SearchResults> searchResponse = milvusClient.search(searchParam);
            if (searchResponse.getStatus() != R.Status.Success.getCode()) {
                log.error("Milvus similarity search failed: {}", searchResponse.getMessage());
                return results;
            }

            SearchResultsWrapper wrapper = new SearchResultsWrapper(searchResponse.getData().getResults());
            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
            for (SearchResultsWrapper.IDScore score : scores) {
                MusicCreationSearchResult item = new MusicCreationSearchResult();
                item.setScore(score.getScore());
                item.setCreationId(score.getStrID());
                item.setUserId((String) score.get("user_id"));
                item.setPrompt((String) score.get("prompt"));
                item.setSettings((String) score.get("settings"));
                results.add(item);
            }
        } catch (Exception e) {
            log.error("Failed to query similar creations from Milvus", e);
        }
        return results;
    }
}
