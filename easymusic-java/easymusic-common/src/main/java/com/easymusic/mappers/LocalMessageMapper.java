package com.easymusic.mappers;

import com.easymusic.entity.po.LocalMessage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface LocalMessageMapper {
    Integer insert(@Param("bean") LocalMessage message);
    Integer updateByMessageId(@Param("bean") LocalMessage message, @Param("messageId") String messageId);
    LocalMessage selectByMessageId(@Param("messageId") String messageId);
    List<LocalMessage> selectUnsentMessages(@Param("maxRetry") Integer maxRetry);
}
