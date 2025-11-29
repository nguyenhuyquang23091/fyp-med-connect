package com.fyp.search_service.mapper;

import com.fyp.search_service.dto.response.UserResponse;
import com.fyp.search_service.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSearchMapper {

    UserResponse toUserResponse(UserEntity userEntity);
}
