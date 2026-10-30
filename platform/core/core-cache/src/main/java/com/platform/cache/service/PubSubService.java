package com.platform.cache.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PubSubService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;

    // ─────────────────────────────────────────────
    // PUBLISH - Send message to a channel
    // Used by: Chat module to broadcast messages
    // ─────────────────────────────────────────────
    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
        log.debug("PubSub PUBLISH → channel: {}", channel);
    }

    // ─────────────────────────────────────────────
    // PUBLISH to chat room
    // ─────────────────────────────────────────────
    public void publishToRoom(String roomId, Object message) {
        publish("chat:room:" + roomId, message);
    }

    // ─────────────────────────────────────────────
    // PUBLISH notification to user
    // ─────────────────────────────────────────────
    public void publishNotification(String userId, Object notification) {
        publish("notification:user:" + userId, notification);
    }

    // ─────────────────────────────────────────────
    // SUBSCRIBE - Listen to a channel
    // ─────────────────────────────────────────────
    public void subscribe(String channel, MessageListenerAdapter listener) {
        listenerContainer.addMessageListener(listener, new ChannelTopic(channel));
        log.debug("PubSub SUBSCRIBE → channel: {}", channel);
    }

    // ─────────────────────────────────────────────
    // UNSUBSCRIBE - Stop listening to a channel
    // ─────────────────────────────────────────────
    public void unsubscribe(String channel, MessageListenerAdapter listener) {
        listenerContainer.removeMessageListener(listener, new ChannelTopic(channel));
        log.debug("PubSub UNSUBSCRIBE → channel: {}", channel);
    }
}
