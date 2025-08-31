package com.fyp.rag_chat_bot.configuration;


import com.fyp.rag_chat_bot.entity.SessionRedis;
import com.fyp.rag_chat_bot.services.ChatSessionService;
import com.fyp.rag_chat_bot.services.RagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfiguration {


    @Bean
    public RedisTemplate<?, ? > redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<?,?> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        return template;
    }

    @Component
    public class SessionExpiredEventListener{
        private final RagService ragService;
        private final ChatSessionService sessionService;
        public SessionExpiredEventListener(RagService ragService, ChatSessionService chatSessionService) {
            this.ragService = ragService;
            this.sessionService = chatSessionService;
        }
        
        @EventListener
        public void handleRedisKeyExpiredEvent(RedisKeyExpiredEvent<SessionRedis> event){
            SessionRedis expiredSession = (SessionRedis) event.getValue();
            String sessionId = expiredSession.getSessionId();
            String conversationID = expiredSession.getConversationId();
            ragService.deleteConversation(conversationID);
            sessionService.deleteSession(sessionId);
        }
    }

}
