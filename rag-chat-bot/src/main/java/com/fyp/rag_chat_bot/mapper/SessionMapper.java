package com.fyp.rag_chat_bot.mapper;

import com.fyp.rag_chat_bot.entity.SessionEntity;
import com.fyp.rag_chat_bot.entity.SessionRedis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    
    SessionMapper INSTANCE = Mappers.getMapper(SessionMapper.class);
    
    @Mapping(target = "expirationTime", constant = "100L")
    SessionRedis toSessionRedis(SessionEntity sessionEntity);
    
    @Mapping(target = "id", ignore = true)
    SessionEntity toSessionEntity(SessionRedis sessionRedis);
}