package com.easymusic.consumer;

import com.easymusic.config.RabbitConfig;
import com.easymusic.entity.dto.MusicCreationResultDTO;
import com.easymusic.entity.dto.MusicTaskDTO;
import com.easymusic.entity.enums.MusicStatusEnum;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.service.MusicInfoService;
import com.easymusic.utils.JsonUtils;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class MusicTimeoutConsumer {

    @Resource
    private MusicInfoService musicInfoService;

    @RabbitListener(queues = RabbitConfig.MUSIC_TIMEOUT_DLQ_QUEUE)
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            log.warn("Received timeout DLQ message: {}", body);
            MusicTaskDTO taskDto = JsonUtils.convertJson2Obj(body, MusicTaskDTO.class);

            // Check if the music is still in CREATING status
            MusicInfo musicInfo = musicInfoService.getMusicInfoByMusicId(taskDto.getMusicId());
            if (musicInfo != null && MusicStatusEnum.CREATING.getStatus().equals(musicInfo.getMusicStatus())) {
                log.warn("Music task {} has timed out (exceeded 5 mins). Forcing failure and refunding user.", taskDto.getMusicId());
                
                // Construct a failed result to trigger refund
                MusicCreationResultDTO failResult = new MusicCreationResultDTO();
                failResult.setTaskId(taskDto.getTaskId());
                failResult.setCreateSuccess(false);
                
                musicInfoService.musicCreated(failResult);
            } else {
                log.info("Music task {} is not in CREATING status anymore, ignoring timeout.", taskDto.getMusicId());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error processing timeout DLQ message", e);
            // Ack to prevent infinite processing on error, as this is a DLQ cleanup
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
