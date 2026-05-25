package com.easymusic.consumer;

import com.easymusic.api.MusicCreateApi;
import com.easymusic.config.RabbitConfig;
import com.easymusic.entity.dto.MusicCreationResultDTO;
import com.easymusic.entity.dto.MusicTaskDTO;
import com.easymusic.entity.enums.MusicStatusEnum;
import com.easymusic.entity.enums.MusicTypeEnum;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.service.MusicInfoService;
import com.easymusic.spring.SpringContext;
import com.easymusic.utils.JsonUtils;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class MusicQueryConsumer {

    @Resource
    private MusicInfoService musicInfoService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.MUSIC_QUERY_CHECK_QUEUE)
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("Received query check message: {}", body);
            MusicTaskDTO taskDto = JsonUtils.convertJson2Obj(body, MusicTaskDTO.class);

            // Check database status
            MusicInfo musicInfo = musicInfoService.getMusicInfoByMusicId(taskDto.getMusicId());
            if (musicInfo == null) {
                log.warn("Music info not found for musicId: {}, ack message", taskDto.getMusicId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (!MusicStatusEnum.CREATING.getStatus().equals(musicInfo.getMusicStatus())) {
                log.info("Music {} status is already {}, no need to check further", taskDto.getMusicId(), musicInfo.getMusicStatus());
                channel.basicAck(deliveryTag, false);
                return;
            }

            // Query AI API
            MusicCreateApi musicCreateApi = (MusicCreateApi) SpringContext.getBean(taskDto.getApiCode());
            MusicCreationResultDTO resultDTO;
            if (MusicTypeEnum.MUSIC.getType().equals(taskDto.getMusicType())) {
                resultDTO = musicCreateApi.musicQuery(taskDto.getTaskId());
            } else {
                resultDTO = musicCreateApi.pureMusicQuery(taskDto.getTaskId());
            }

            if (resultDTO == null) {
                // Still in progress, republish a new delayed message
                log.info("Music {} is still generating, republishing delayed check message", taskDto.getMusicId());
                rabbitTemplate.convertAndSend(RabbitConfig.MUSIC_QUERY_DELAY_QUEUE, (Object) JsonUtils.convertObj2Json(taskDto));
            } else {
                // Done (success or fail), call business service
                log.info("Music {} generation completed (success: {}). Updating status.", taskDto.getMusicId(), resultDTO.getCreateSuccess());
                musicInfoService.musicCreated(resultDTO);
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error processing query check message", e);
            // Don't requeue to avoid infinite loop on error. The timeout dead letter queue will eventually clean up.
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
