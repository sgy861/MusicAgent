package com.easymusic.controller;

import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.MusicInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/music")
@Slf4j
public class MusicInfoController extends ABaseController {

    @Resource
    private MusicInfoService musicInfoService;

    @RequestMapping("/loadMusic")
    public ResponseVO loadMusic(MusicInfoQuery musicInfoQuery) {
        musicInfoQuery.setOrderBy("m.create_time desc");
        musicInfoQuery.setQueryUser(true);
        PaginationResultVO resultVO = musicInfoService.findListByPage(musicInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/changeMusicCommendType")
    public ResponseVO changeMusicCommendType(String musicId, Integer commendType) {
        MusicInfo musicInfo = new MusicInfo();
        musicInfo.setCommendType(commendType);
        musicInfoService.updateMusicInfoByMusicId(musicInfo, musicId);
        return getSuccessResponseVO(null);
    }
}
