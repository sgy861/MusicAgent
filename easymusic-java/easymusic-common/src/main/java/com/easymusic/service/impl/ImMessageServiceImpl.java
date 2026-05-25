package com.easymusic.service.impl;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.ImMessage;
import com.easymusic.entity.query.ImMessageQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.mappers.ImMessageMapper;
import com.easymusic.redis.RedisUtils;
import com.easymusic.service.ImMessageService;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.StringTools;
import com.easymusic.config.RabbitConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * IM消息 业务接口实现
 */
@Slf4j
@Service("imMessageService")
public class ImMessageServiceImpl implements ImMessageService {

    @Resource
    private ImMessageMapper<ImMessage, ImMessageQuery> imMessageMapper;

    @Resource
    private RedisUtils<String> redisUtils;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final String IM_ROUTE_KEY_PREFIX = "im:route:";

    @Override
    public List<ImMessage> findListByParam(ImMessageQuery param) {
        return this.imMessageMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(ImMessageQuery param) {
        return this.imMessageMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<ImMessage> findListByPage(ImMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ImMessage> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(ImMessage bean) {
        if (bean.getMessageId() == null) {
            bean.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
        Date date = new Date();
        bean.setCreateTime(date);
        bean.setUpdateTime(date);
        return this.imMessageMapper.insert(bean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateImMessageByMessageId(ImMessage bean, String messageId) {
        bean.setUpdateTime(new Date());
        return this.imMessageMapper.updateByMessageId(bean, messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteImMessageByMessageId(String messageId) {
        return this.imMessageMapper.deleteByMessageId(messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processSendMessage(ImMessage message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
        Date date = new Date();
        message.setCreateTime(date);
        message.setUpdateTime(date);

        if ("CHAT".equalsIgnoreCase(message.getMsgType())) {
            message.setStatus(0); // 未读/未送达
            this.imMessageMapper.insert(message);

            // 获取接收方路由
            String routeKey = IM_ROUTE_KEY_PREFIX + message.getReceiverId();
            String nodeAddress = redisUtils.get(routeKey);
            log.info("Processing CHAT message from {} to {}. Route in Redis: {}", message.getSenderId(), message.getReceiverId(), nodeAddress);

            if (nodeAddress != null) {
                // 在线，投递到对应节点的队列
                String msgJson = JsonUtils.convertObj2Json(message);
                rabbitTemplate.convertAndSend(RabbitConfig.IM_DIRECT_EXCHANGE, nodeAddress, msgJson);
                log.info("Sent CHAT message to RabbitMQ: exchange={}, routingKey={}", RabbitConfig.IM_DIRECT_EXCHANGE, nodeAddress);
            }
        } else if ("REVIEW".equalsIgnoreCase(message.getMsgType())) {
            message.setStatus(1); // 评论/点评是广播消息，默认状态设为已处理
            this.imMessageMapper.insert(message);

            // 广播到所有节点
            String msgJson = JsonUtils.convertObj2Json(message);
            rabbitTemplate.convertAndSend(RabbitConfig.IM_REVIEW_FANOUT_EXCHANGE, "", msgJson);
            log.info("Broadcasted REVIEW message to RabbitMQ: exchange={}", RabbitConfig.IM_REVIEW_FANOUT_EXCHANGE);
        }
    }

    @Override
    public void pushOfflineMessages(String userId) {
        ImMessageQuery query = new ImMessageQuery();
        query.setReceiverId(userId);
        query.setStatus(0); // 未读/未送达
        query.setMsgType("CHAT");
        query.setOrderBy("create_time asc");

        List<ImMessage> offlineMessages = this.imMessageMapper.selectList(query);
        if (offlineMessages == null || offlineMessages.isEmpty()) {
            return;
        }

        log.info("Found {} offline messages for user: {}", offlineMessages.size(), userId);
        String routeKey = IM_ROUTE_KEY_PREFIX + userId;
        String nodeAddress = redisUtils.get(routeKey);

        if (nodeAddress != null) {
            for (ImMessage msg : offlineMessages) {
                String msgJson = JsonUtils.convertObj2Json(msg);
                rabbitTemplate.convertAndSend(RabbitConfig.IM_DIRECT_EXCHANGE, nodeAddress, msgJson);
            }
            log.info("Pushed {} offline messages to RabbitMQ routing key: {}", offlineMessages.size(), nodeAddress);
        }
    }
}
