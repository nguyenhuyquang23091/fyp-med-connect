package com.fyp.search_service.service;


import com.fyp.search_service.entity.UserEntity;
import com.fyp.search_service.mapper.UserCdcMapper;
import com.fyp.search_service.repository.UserSearchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSearchService {

    UserSearchRepository userSearchRepository;

    UserCdcMapper userCdcMapper;

    public void saveUser(Map<String, Object> userData){
            try {
                UserEntity user = userCdcMapper.toUserEntity(userData);
                userSearchRepository.save(user);
            } catch (Exception e) {
                log.error("Error indexing user: {}", userData.get("id"), e);
            }
    }

    public void deleteUser(String userId){
        try {

      userSearchRepository.deleteById(userId);
        }
        catch (Exception e){
            log.error("Error deleting user: {}",userId, e);
        }

    }


}
