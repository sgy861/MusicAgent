package com.easymusic.api;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.dto.MusicCreationResultDTO;
import com.easymusic.entity.enums.MusicTypeEnum;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.OKHttpUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("tianpuyueApi")
@Slf4j
public class MusicCreteApi4TianpuyueImpl implements MusicCreateApi {

    /**
     * 生成音乐
     */
    private String URL_CREATE_MUSIC = "/open-apis/v1/song/generate";
    /**
     * 查询音乐生成
     */
    private String URL_QUERY_MUSIC = "/open-apis/v1/song/query";

    /**
     * 生成纯音乐
     */
    private String URL_CREATE_PURE_MUSIC = "/open-apis/v1/instrumental/generate";

    private String URL_QUERY_PURE_MUSIC = "/open-apis/v1/instrumental/query";

    private String CALL_BACL_URL = "/api/musicNotify/tianpuyu/%d";

    private Integer STATUS_SUCCESS = 200000;

    @Resource
    private AppConfig appConfig;


    private Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", appConfig.getTianpuyueApiKey());
        header.put("Content-Type", "application/json; charset=utf-8");
        header.put("courseOrderId", appConfig.getTianpuyueApiCourseOrderId());
        return header;
    }

    @Override
    public List<String> createMusic(String model, String prompt, String lyrics) {
        Map<String, String> header = getHeader();
        Map<String, Object> params = new HashMap<>();
        params.put("prompt", prompt);
        params.put("lyrics", lyrics);
        params.put("model", model);
        params.put("callback_url", appConfig.getWebDomain() + String.format(CALL_BACL_URL, MusicTypeEnum.MUSIC.getType()));
        String jsonParams = JsonUtils.convertObj2Json(params);
        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_CREATE_MUSIC, header, jsonParams);
        List<String> itemList = (List<String>) JSONPath.eval(response, "$.data.item_ids");
        return itemList;
    }

    @Override
    public MusicCreationResultDTO musicQuery(String itemId) {
        Map<String, String> header = getHeader();
        Map<String, Object> params = new HashMap<>();
        params.put("item_ids", new String[]{itemId});
        String jsonParams = JsonUtils.convertObj2Json(params);
        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_QUERY_MUSIC, header, jsonParams);
        JSONObject jsonObject = (JSONObject) JSONPath.eval(response, "$.data.songs[0]");
        return getMusicResultDTO((Integer) JSONPath.eval(response, "$.status"), jsonObject, MusicTypeEnum.MUSIC);
    }

    private MusicCreationResultDTO getMusicResultDTO(Integer status, JSONObject jsonObject, MusicTypeEnum musicType) {
        if (status != null && !STATUS_SUCCESS.equals(status)) {
            MusicCreationResultDTO resultDTO = new MusicCreationResultDTO();
            resultDTO.setTaskId(jsonObject.getString("item_id"));
            resultDTO.setCreateSuccess(false);
            return resultDTO;
        }
        if (jsonObject == null) {
            return null;
        }
        List<MusicCreationResultDTO.Lyrics> lyricsList = new ArrayList<>();
        if (MusicTypeEnum.MUSIC == musicType) {
            if (jsonObject.get("lyrics_sections") == null) {
                return null;
            }
            lyricsList = JsonUtils.convertJsonArray2List(JsonUtils.convertObj2Json(jsonObject.get("lyrics_sections")), MusicCreationResultDTO.Lyrics.class);
        }
        MusicCreationResultDTO resultDTO = new MusicCreationResultDTO();
        resultDTO.setTaskId(jsonObject.getString("item_id"));
        resultDTO.setTitle(jsonObject.getString("title"));
        resultDTO.setAudioUrl(jsonObject.getString("audio_url"));
        resultDTO.setAudioHiUrl(jsonObject.getString("audio_hi_url"));
        resultDTO.setDuration(jsonObject.getIntValue("duration"));
        resultDTO.setLyricsList(lyricsList);
        resultDTO.setCreateSuccess(true);
        return resultDTO;
    }

    @Override
    public List<String> createPureMusic(String model, String prompt) {
        Map<String, String> header = getHeader();
        Map<String, Object> params = new HashMap<>();
        params.put("prompt", prompt);
        params.put("model", model);
        params.put("callback_url", appConfig.getWebDomain() + String.format(CALL_BACL_URL, MusicTypeEnum.PURE.getType()));
        String jsonParams = JsonUtils.convertObj2Json(params);
        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_CREATE_PURE_MUSIC, header, jsonParams);
        List<String> itemList = (List<String>) JSONPath.eval(response, "$.data.item_ids");
        return itemList;
    }

    @Override
    public MusicCreationResultDTO pureMusicQuery(String itemId) {
        Map<String, String> header = getHeader();
        Map<String, Object> params = new HashMap<>();
        params.put("item_ids", new String[]{itemId});
        String jsonParams = JsonUtils.convertObj2Json(params);
        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_QUERY_PURE_MUSIC, header, jsonParams);
        JSONObject jsonObject = (JSONObject) JSONPath.eval(response, "$.data.instrumentals[0]");
        return getMusicResultDTO((Integer) JSONPath.eval(response, "$.status"), jsonObject, MusicTypeEnum.PURE);
    }


    @Override
    public MusicCreationResultDTO createMusicNotify(Integer musicType, String responseBody) {
        MusicTypeEnum musicTypeEnum = MusicTypeEnum.getByType(musicType);
        if (MusicTypeEnum.MUSIC == musicTypeEnum) {
            JSONObject jsonObject = (JSONObject) JSONPath.eval(responseBody, "$.songs[0]");
            return getMusicResultDTO((Integer) JSONPath.eval(responseBody, "$.status"), jsonObject, musicTypeEnum);
        } else if (MusicTypeEnum.PURE == musicTypeEnum) {
            JSONObject jsonObject = (JSONObject) JSONPath.eval(responseBody, "$.instrumentals[0]");
            return getMusicResultDTO((Integer) JSONPath.eval(responseBody, "$.status"), jsonObject, musicTypeEnum);
        }
        return null;
    }
}
