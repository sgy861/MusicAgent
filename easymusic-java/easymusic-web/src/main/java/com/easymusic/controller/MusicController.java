package com.easymusic.controller;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.enums.CommendTypeEnum;
import com.easymusic.entity.enums.MusicStatusEnum;
import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.po.MusicInfoAction;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.MusicCreationService;
import com.easymusic.service.MusicInfoActionService;
import com.easymusic.service.MusicInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/music")
public class MusicController extends ABaseController {

    @Resource
    private MusicInfoService musicInfoService;

    @Resource
    private MusicCreationService musicCreationService;

    @Resource
    private MusicInfoActionService musicInfoActionService;

    @RequestMapping("/loadCommendMusic")
    public ResponseVO loadCommendMusic() {

        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo(null);
        MusicInfoQuery musicInfoQuery = new MusicInfoQuery();
        musicInfoQuery.setQueryUser(true);
        musicInfoQuery.setCommendType(CommendTypeEnum.COMMEND.getType());
        musicInfoQuery.setCurrentUserId(tokenUserInfoDTO == null ? null : tokenUserInfoDTO.getUserId());
        musicInfoQuery.setOrderBy("m.create_time desc");
        return getSuccessResponseVO(musicInfoService.findListByParam(musicInfoQuery));
    }

    @RequestMapping("/loadLatestMusic")
    public ResponseVO loadLatestMusic(Integer pageNo, Integer indexType) {
        MusicInfoQuery musicInfoQuery = new MusicInfoQuery();
        musicInfoQuery.setQueryUser(true);
        musicInfoQuery.setCommendType(CommendTypeEnum.NOT_COMMEND.getType());
        musicInfoQuery.setMusicStatus(MusicStatusEnum.CREATED.getStatus());
        musicInfoQuery.setOrderBy("m.create_time desc");
        if (indexType != null) {
            musicInfoQuery.setPageSize(PageSize.SIZE12.getSize());
        } else {
            musicInfoQuery.setPageSize(PageSize.SIZE20.getSize());
        }
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo(null);
        musicInfoQuery.setCurrentUserId(tokenUserInfoDTO == null ? null : tokenUserInfoDTO.getUserId());
        musicInfoQuery.setPageNo(pageNo);
        return getSuccessResponseVO(musicInfoService.findListByPage(musicInfoQuery));
    }

    @RequestMapping("/musicDetail")
    public ResponseVO musicDetail(@NotEmpty String musicId) {
        MusicInfo musicInfo = musicInfoService.getMusicInfoByMusicId(musicId);
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo(null);
        if (tokenUserInfoDTO != null) {
            MusicInfoAction action = musicInfoActionService.getMusicInfoActionByMusicIdAndUserId(musicId, tokenUserInfoDTO.getUserId());
            musicInfo.setDoGood(action != null);
        }
        return getSuccessResponseVO(musicInfo);
    }


    @RequestMapping("/doGood")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO doGood(@NotEmpty String musicId) {
        musicInfoActionService.doGood(musicId, getTokenUserInfo(null).getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updatePlayCount")
    public ResponseVO updatePlayCount(@NotEmpty String musicId) {
        musicInfoService.updateMusicCount(musicId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getCreation")
    public ResponseVO getCreation(@NotEmpty String creationId) {
        MusicCreation musicCreation = musicCreationService.getMusicCreationByCreationId(creationId);
        return getSuccessResponseVO(musicCreation);
    }
}