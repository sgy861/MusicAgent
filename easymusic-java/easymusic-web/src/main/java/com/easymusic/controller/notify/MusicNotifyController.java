package com.easymusic.controller.notify;

import com.easymusic.controller.ABaseController;
import com.easymusic.service.MusicInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/musicNotify")
@Slf4j
public class MusicNotifyController extends ABaseController {

    @Resource
    private MusicInfoService musicInfoService;

    @RequestMapping("/tianpuyu/{musicType}")
    public String tianpuyueMusicCreateNotify(@PathVariable("musicType") Integer musicType,
                                             @RequestBody String responseJson) {
        log.info("音乐创建回到信息musicType:{},body:{}", musicType, responseJson);
        musicInfoService.musicCreateNotify(musicType, responseJson);
        return STATUC_SUCCESS;
    }
}
