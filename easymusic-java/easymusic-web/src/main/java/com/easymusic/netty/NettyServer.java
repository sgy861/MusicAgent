package com.easymusic.netty;

import com.easymusic.entity.po.ImMessage;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.ImMessageService;
import com.easymusic.service.RecommendAgentService;
import com.easymusic.utils.JsonUtils;
import com.easymusic.config.RabbitConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Component("nettyServer")
@Slf4j
public class NettyServer implements ApplicationRunner {

    @Value("${netty.port:8099}")
    private int nettyPort;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChannelManager channelManager;

    @Resource
    private ImMessageService imMessageService;

    @Resource
    private RecommendAgentService recommendAgentService;

    @Resource
    private AmqpAdmin amqpAdmin;

    @Resource
    private ConnectionFactory connectionFactory;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture serverChannelFuture;
    private SimpleMessageListenerContainer rabbitListenerContainer;
    private String nodeAddress;

    @Override
    public void run(ApplicationArguments args) {
        // 在独立后台线程中启动 Netty，避免阻塞 Spring Boot 启动主线程
        new Thread(this::start, "netty-server-thread").start();
    }

    public void start() {
        try {
            // 1. 获取并初始化节点地址
            String ip = "127.0.0.1";
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                log.error("Failed to get local host address", e);
            }
            nodeAddress = ip + ":" + nettyPort;
            channelManager.initNodeAddress(nodeAddress);

            // 2. 清理由于节点崩溃等异常导致的 stale 路由 (集群自愈)
            channelManager.cleanNodeRoutes();

            // 3. 启动本节点的 RabbitMQ 动态队列消费者，用于跨节点消息接收投递
            startRabbitMqListener(nodeAddress);

            // 4. 初始化主从 Reactor EventLoop 线程组
            bossGroup = new NioEventLoopGroup(1); // 1个主 Reactor 线程用于 accept
            workerGroup = new NioEventLoopGroup();    // 默认核数*2个从 Reactor 线程用于处理 Read/Write

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP 参数调优 SO_BACKLOG：TCP 连接排队队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 启用 TCP 心跳机制 SO_KEEPALIVE
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 禁用 Nagle 算法，确保小包实时发送以降低时延
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 启用 PooledByteBufAllocator 内存池分配器以复用 ByteBuf 内存，避免频繁垃圾回收
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    // 装配 pipeline
                    .childHandler(new WebSocketChannelInitializer(redisComponent, channelManager, imMessageService, recommendAgentService));

            log.info("Starting Netty Server on port {}...", nettyPort);
            serverChannelFuture = bootstrap.bind(nettyPort).sync();
            log.info("Netty Server started successfully on port {}.", nettyPort);

            // 监听关闭通道事件
            serverChannelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.warn("Netty Server thread was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Failed to start Netty Server", e);
        } finally {
            destroy();
        }
    }

    private void startRabbitMqListener(String nodeAddress) {
        String queueName = "im.queue." + nodeAddress;

        // 1. 声明一个临时、排他且自动删除的队列（Netty 节点下线后，队列由 RabbitMQ 自动收回）
        Queue queue = new Queue(queueName, false, false, true);
        amqpAdmin.declareQueue(queue);

        // 2. 绑定 direct 交换机，路由键为当前节点地址，确保跨节点定位投递
        Binding directBinding = BindingBuilder.bind(queue)
                .to(new DirectExchange(RabbitConfig.IM_DIRECT_EXCHANGE))
                .with(nodeAddress);
        amqpAdmin.declareBinding(directBinding);

        // 3. 绑定 fanout 交换机，确保点评/评论消息能广播给本节点订阅同一 musicId 房间的客户端
        Binding fanoutBinding = BindingBuilder.bind(queue)
                .to(new FanoutExchange(RabbitConfig.IM_REVIEW_FANOUT_EXCHANGE));
        amqpAdmin.declareBinding(fanoutBinding);

        // 4. 配置 Spring AMQP 监听容器，进行手动 Ack 与消息状态流转
        rabbitListenerContainer = new SimpleMessageListenerContainer();
        rabbitListenerContainer.setConnectionFactory(connectionFactory);
        rabbitListenerContainer.setQueueNames(queueName);
        rabbitListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        rabbitListenerContainer.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            try {
                String json = new String(message.getBody(), StandardCharsets.UTF_8);
                ImMessage imMessage = JsonUtils.convertJson2Obj(json, ImMessage.class);

                log.info("MQ Consumer: Received msgType={} for receiver={}", imMessage.getMsgType(), imMessage.getReceiverId());

                if ("CHAT".equalsIgnoreCase(imMessage.getMsgType())) {
                    io.netty.channel.Channel wsChannel = channelManager.getUserChannel(imMessage.getReceiverId());
                    if (wsChannel != null && wsChannel.isActive()) {
                        // 本地有活跃的 WebSocket 通道，写入推送
                        wsChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(imMessage)));
                        log.info("Pushed CHAT message to user {} successfully.", imMessage.getReceiverId());

                        // 状态流转：流转为已读/已送达 (status=1)
                        ImMessage updateBean = new ImMessage();
                        updateBean.setStatus(1);
                        imMessageService.updateImMessageByMessageId(updateBean, imMessage.getMessageId());
                    } else {
                        log.info("User {} is offline on this node. Message stays in DB as unread.", imMessage.getReceiverId());
                    }
                } else if ("REVIEW".equalsIgnoreCase(imMessage.getMsgType())) {
                    // 点评消息：广播推送给订阅该 musicId 房间的所有本地用户
                    io.netty.channel.group.ChannelGroup group = channelManager.getRoomGroup(imMessage.getReceiverId());
                    if (group != null && !group.isEmpty()) {
                        group.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(imMessage)));
                        log.info("Broadcasted REVIEW message to room group {} size={}.", imMessage.getReceiverId(), group.size());
                    }
                }

                // 成功处理，进行手动确认
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("Failed to process MQ message in Netty server, sending nack.", e);
                // 抛弃无法处理的消息或设置拒绝重试，避免死循环毒包
                channel.basicNack(deliveryTag, false, false);
            }
        });

        rabbitListenerContainer.start();
        log.info("RabbitMQ dynamic listener started for node queue: {}", queueName);
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping Netty Server & releasing resources...");

        // 1. 停止 RabbitMQ 消费者
        if (rabbitListenerContainer != null && rabbitListenerContainer.isRunning()) {
            try {
                rabbitListenerContainer.stop();
                log.info("RabbitMQ listener container stopped.");
            } catch (Exception e) {
                log.error("Failed to stop RabbitMQ container", e);
            }
        }

        // 2. 注销 Redis 在线路由缓存
        if (channelManager != null) {
            channelManager.cleanNodeRoutes();
        }

        // 3. 关闭 Netty EventLoop
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        log.info("Netty Server shutdown complete.");
    }
}
