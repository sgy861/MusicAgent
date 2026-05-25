package com.easymusic.service;

import com.easymusic.entity.po.ImMessage;
import com.easymusic.entity.query.ImMessageQuery;
import com.easymusic.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * IM消息 业务接口
 */
public interface ImMessageService {

    /**
     * 根据条件查询列表
     */
    List<ImMessage> findListByParam(ImMessageQuery param);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(ImMessageQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<ImMessage> findListByPage(ImMessageQuery param);

    /**
     * 新增
     */
    Integer add(ImMessage bean);

    /**
     * 根据MessageId修改
     */
    Integer updateImMessageByMessageId(ImMessage bean, String messageId);

    /**
     * 根据MessageId删除
     */
    Integer deleteImMessageByMessageId(String messageId);

    /**
     * 处理消息发送 (保存至数据库并分发路由)
     */
    void processSendMessage(ImMessage message);

    /**
     * 推送离线消息给登录用户
     */
    void pushOfflineMessages(String userId);
}
