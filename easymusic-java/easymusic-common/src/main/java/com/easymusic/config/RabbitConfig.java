package com.easymusic.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    // Exchange and queue names for Timeout DLQ
    public static final String MUSIC_TIMEOUT_DLX = "music.timeout.dlx";
    public static final String MUSIC_TIMEOUT_DLQ = "music.timeout.dlq";
    public static final String MUSIC_TIMEOUT_DLQ_QUEUE = "music.timeout.dlq.queue";
    public static final String MUSIC_TIMEOUT_DELAY_QUEUE = "music.timeout.delay.queue";

    // Exchange and queue names for Polling / Query Checks
    public static final String MUSIC_QUERY_DLX = "music.query.dlx";
    public static final String MUSIC_QUERY_CHECK = "music.query.check";
    public static final String MUSIC_QUERY_CHECK_QUEUE = "music.query.check.queue";
    public static final String MUSIC_QUERY_DELAY_QUEUE = "music.query.delay.queue";

    // Exchange and queue names for User Behavior Update
    public static final String USER_BEHAVIOR_DLX = "user.behavior.dlx";
    public static final String USER_BEHAVIOR_ROUTING_KEY = "user.behavior.update";
    public static final String USER_BEHAVIOR_UPDATE_QUEUE = "user.behavior.update.queue";
    public static final String USER_BEHAVIOR_DELAY_QUEUE = "user.behavior.delay.queue";

    // Exchange names for High-Concurrency IM WebSocket Engine
    public static final String IM_DIRECT_EXCHANGE = "im.direct.exchange";
    public static final String IM_REVIEW_FANOUT_EXCHANGE = "im.review.fanout.exchange";

    // AI Recommend Task Queue and Exchange
    public static final String AI_RECOMMEND_TASK_QUEUE = "ai.recommend.task.queue";
    public static final String AI_RECOMMEND_RESULT_EXCHANGE = "ai.recommend.result.exchange";

    // 1. Timeout Config
    @Bean
    public DirectExchange musicTimeoutDlx() {
        return new DirectExchange(MUSIC_TIMEOUT_DLX, true, false);
    }

    @Bean
    public Queue musicTimeoutDlqQueue() {
        return new Queue(MUSIC_TIMEOUT_DLQ_QUEUE, true);
    }

    @Bean
    public Binding musicTimeoutDlqBinding() {
        return BindingBuilder.bind(musicTimeoutDlqQueue()).to(musicTimeoutDlx()).with(MUSIC_TIMEOUT_DLQ);
    }

    @Bean
    public Queue musicTimeoutDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // Configure dead letter exchange
        arguments.put("x-dead-letter-exchange", MUSIC_TIMEOUT_DLX);
        // Configure dead letter routing key
        arguments.put("x-dead-letter-routing-key", MUSIC_TIMEOUT_DLQ);
        // Configure TTL for timeout check (5 minutes = 300000ms)
        arguments.put("x-message-ttl", 300000);
        return QueueBuilder.durable(MUSIC_TIMEOUT_DELAY_QUEUE).withArguments(arguments).build();
    }

    // 2. Query Polling Config
    @Bean
    public DirectExchange musicQueryDlx() {
        return new DirectExchange(MUSIC_QUERY_DLX, true, false);
    }

    @Bean
    public Queue musicQueryCheckQueue() {
        return new Queue(MUSIC_QUERY_CHECK_QUEUE, true);
    }

    @Bean
    public Binding musicQueryCheckBinding() {
        return BindingBuilder.bind(musicQueryCheckQueue()).to(musicQueryDlx()).with(MUSIC_QUERY_CHECK);
    }

    @Bean
    public Queue musicQueryDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", MUSIC_QUERY_DLX);
        arguments.put("x-dead-letter-routing-key", MUSIC_QUERY_CHECK);
        // Configure TTL for query interval (30 seconds = 30000ms)
        arguments.put("x-message-ttl", 30000);
        return QueueBuilder.durable(MUSIC_QUERY_DELAY_QUEUE).withArguments(arguments).build();
    }

    // 3. User Behavior Delay Update Config
    @Bean
    public DirectExchange userBehaviorDlx() {
        return new DirectExchange(USER_BEHAVIOR_DLX, true, false);
    }

    @Bean
    public Queue userBehaviorUpdateQueue() {
        return new Queue(USER_BEHAVIOR_UPDATE_QUEUE, true);
    }

    @Bean
    public Binding userBehaviorUpdateBinding() {
        return BindingBuilder.bind(userBehaviorUpdateQueue()).to(userBehaviorDlx()).with(USER_BEHAVIOR_ROUTING_KEY);
    }

    @Bean
    public Queue userBehaviorDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", USER_BEHAVIOR_DLX);
        arguments.put("x-dead-letter-routing-key", USER_BEHAVIOR_ROUTING_KEY);
        // Delay for 30 seconds = 30000ms
        arguments.put("x-message-ttl", 30000);
        return QueueBuilder.durable(USER_BEHAVIOR_DELAY_QUEUE).withArguments(arguments).build();
    }

    @Bean
    public DirectExchange imDirectExchange() {
        return new DirectExchange(IM_DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange imReviewFanoutExchange() {
        return new FanoutExchange(IM_REVIEW_FANOUT_EXCHANGE, true, false);
    }

    @Bean
    public Queue aiRecommendTaskQueue() {
        return new Queue(AI_RECOMMEND_TASK_QUEUE, true);
    }

    @Bean
    public DirectExchange aiRecommendResultExchange() {
        return new DirectExchange(AI_RECOMMEND_RESULT_EXCHANGE, true, false);
    }
}
