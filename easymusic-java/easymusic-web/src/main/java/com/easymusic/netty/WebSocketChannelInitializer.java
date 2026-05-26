package com.easymusic.netty;

import com.easymusic.redis.RedisComponent;
import com.easymusic.service.ImMessageService;
import com.easymusic.service.RecommendAgentService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final RedisComponent redisComponent;
    private final ChannelManager channelManager;
    private final ImMessageService imMessageService;
    private final RecommendAgentService recommendAgentService;

    public WebSocketChannelInitializer(RedisComponent redisComponent, ChannelManager channelManager, ImMessageService imMessageService, RecommendAgentService recommendAgentService) {
        this.redisComponent = redisComponent;
        this.channelManager = channelManager;
        this.imMessageService = imMessageService;
        this.recommendAgentService = recommendAgentService;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 1. 空闲剔除处理器：60秒无任何入站数据时触发 READER_IDLE 事件
        ch.pipeline().addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
        
        // 2. HTTP 编解码器与聚合器
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(65536));
        
        // 3. 自定义握手鉴权处理器
        ch.pipeline().addLast(new HandshakeAuthHandler(redisComponent));
        
        // 4. WebSocket 协议升级处理器
        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
        
        // 5. 消息逻辑处理器
        ch.pipeline().addLast(new WebSocketHandler(channelManager, imMessageService, recommendAgentService));
    }
}
