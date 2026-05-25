package com.easymusic.controller;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.dto.MusicSettingDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.query.UserIntegralRecordQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.MusicCreationService;
import com.easymusic.service.MusicInfoService;
import com.easymusic.service.SysDictService;
import com.easymusic.service.UserIntegralRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/my")
@Validated
public class MyController extends ABaseController {

    @Resource
    private MusicCreationService musicCreationService;

    @Resource
    private MusicInfoService musicInfoService;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @Resource
    private SysDictService sysDictService;

    @RequestMapping("/loadSysDict")
    @GlobalInterceptor
    public ResponseVO loadSysDict() {
        return getSuccessResponseVO(sysDictService.getDictFromCache());
    }

    @RequestMapping("/createMusic")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO createMusic(@NotEmpty @Size(max = 500) String prompt,
                                  @Size(max = 1500) String lyrics,
                                  @NotNull Integer musicType,
                                  @NotEmpty String model,
                                  @Size(max = 200) String musicGener,
                                  @Size(max = 150) String musicEmotion,
                                  @Size(max = 5) String musicSex,
                                  @NotNull Integer modeType) {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo(null);
        MusicCreation creation = new MusicCreation();
        creation.setUserId(tokenUserInfoDTO.getUserId());
        creation.setMusicType(musicType);
        creation.setLyrics(lyrics);
        creation.setPrompt(prompt);
        creation.setModel(model);
        creation.setModeType(modeType);
        MusicSettingDTO musicSettingDTO = new MusicSettingDTO();
        musicSettingDTO.setMusicGener(musicGener);
        musicSettingDTO.setMusicEmotion(musicEmotion);
        musicSettingDTO.setMusicSex(musicSex);
        List<String> musicList = musicCreationService.createMusic(creation, musicSettingDTO);
        return getSuccessResponseVO(musicList);
    }

    @RequestMapping("/loadMyMusic")
    @GlobalInterceptor
    public ResponseVO loadMyMusic(Integer pageNo, Boolean queryLikeMusic) {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo(null);
        if (tokenUserInfoDTO == null) {
            return getSuccessResponseVO(new PaginationResultVO<>());
        }
        MusicInfoQuery musicInfoQuery = new MusicInfoQuery();
        musicInfoQuery.setPageNo(pageNo);
        musicInfoQuery.setOrderBy("create_time desc");
        musicInfoQuery.setQueryUser(true);
        musicInfoQuery.setCurrentUserId(tokenUserInfoDTO.getUserId());
        if (queryLikeMusic != null && queryLikeMusic) {
            musicInfoQuery.setQueryLikeMusic(queryLikeMusic == null ? false : queryLikeMusic);
            musicInfoQuery.setLikeUserId(getTokenUserInfo(null).getUserId());
        } else {
            musicInfoQuery.setUserId(getTokenUserInfo(null).getUserId());
        }
        PaginationResultVO resultVO = this.musicInfoService.findListByPage(musicInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/loadCreatingMusic")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadCreatingMusic(@NotEmpty String musicIds) {
        MusicInfoQuery musicInfoQuery = new MusicInfoQuery();
        musicInfoQuery.setUserId(getTokenUserInfo(null).getUserId());
        musicInfoQuery.setMusicIdList(Arrays.asList(musicIds.split(",")));
        List<MusicInfo> musicInfoList = musicInfoService.findListByParam(musicInfoQuery);
        return getSuccessResponseVO(musicInfoList);
    }

    @RequestMapping("/uploadMusicCover")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadMusicCover(@NotNull MultipartFile cover, @NotEmpty String musicId) {
        String resultCover = musicInfoService.updateCover(cover, getTokenUserInfo(null).getUserId(), musicId);
        return getSuccessResponseVO(resultCover);
    }

    @RequestMapping("/delMusic")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delMusic(@NotEmpty String musicId) {
        MusicInfoQuery musicInfoQuery = new MusicInfoQuery();
        musicInfoQuery.setMusicId(musicId);
        musicInfoQuery.setUserId(getTokenUserInfo(null).getUserId());
        musicInfoService.deleteByParam(musicInfoQuery);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/changeMusicTitle")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO changeMusicTitle(@NotEmpty String musicId, @Size(max = 30) @NotEmpty String musicTitle) {
        MusicInfo updateInfo = new MusicInfo();
        updateInfo.setMusicTitle(musicTitle);
        MusicInfoQuery musicInfoQuery = new MusicInfoQuery();
        musicInfoQuery.setMusicId(musicId);
        musicInfoQuery.setUserId(getTokenUserInfo(null).getUserId());
        musicInfoService.updateByParam(updateInfo, musicInfoQuery);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/integralRecords")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO integralRecords(Integer pageNo) {
        UserIntegralRecordQuery recordsQuery = new UserIntegralRecordQuery();
        recordsQuery.setUserId(getTokenUserInfo(null).getUserId());
        recordsQuery.setPageNo(pageNo);
        recordsQuery.setOrderBy("u.record_id desc");
        PaginationResultVO resultVO = userIntegralRecordService.findListByPage(recordsQuery);
        return getSuccessResponseVO(resultVO);
    }
}
