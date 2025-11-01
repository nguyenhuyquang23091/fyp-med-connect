package com.fyp.search_service.service;


import com.fyp.search_service.entity.UserEntity;
import com.fyp.search_service.exceptions.AppException;
import com.fyp.search_service.exceptions.ErrorCode;
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
            String userId = userData.get("id") != null ? userData.get("id").toString() : "unknown";
            log.debug("Indexing user in Elasticsearch: userId={}", userId);

            UserEntity user = userCdcMapper.toUserEntity(userData);
            userSearchRepository.save(user);

            log.info("Successfully indexed user: userId={}", userId);
        } catch (IllegalArgumentException e) {
            // Reason: Mapping errors indicate invalid or malformed CDC event data
            log.error("Failed to deserialize user data: {}", userData.get("id"), e);
            throw new AppException(ErrorCode.CDC_EVENT_DESERIALIZATION_ERROR);
        } catch (Exception e) {
            // Reason: Repository save failures indicate Elasticsearch indexing issues
            log.error("Error indexing user in Elasticsearch: userId={}", userData.get("id"), e);
            throw new AppException(ErrorCode.ELASTICSEARCH_INDEX_ERROR);
        }
    }


    public void deleteUser(String userId){
        try {
            log.debug("Deleting user from Elasticsearch: userId={}", userId);

            userSearchRepository.deleteById(userId);

            log.info("Successfully deleted user from Elasticsearch: userId={}", userId);
        } catch (Exception e){
            // Reason: Repository delete failures indicate Elasticsearch deletion issues
            log.error("Error deleting user from Elasticsearch: userId={}", userId, e);
            throw new AppException(ErrorCode.ELASTICSEARCH_DELETE_ERROR);
        }
    }

}
