package com.fyp.search_service.service;


import com.fyp.search_service.dto.request.UserSearchFilter;
import com.fyp.search_service.dto.response.PageResponse;
import com.fyp.search_service.dto.response.SearchSuggestion;
import com.fyp.search_service.dto.response.UserResponse;
import com.fyp.search_service.entity.UserEntity;
import com.fyp.search_service.exceptions.AppException;
import com.fyp.search_service.exceptions.ErrorCode;
import com.fyp.search_service.mapper.UserCdcMapper;
import com.fyp.search_service.repository.UserSearchRepository;
import com.fyp.search_service.search.ElasticSearchProxy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSearchService {

    UserSearchRepository userSearchRepository;
    UserCdcMapper userCdcMapper;
    ElasticSearchProxy elasticSearchProxy;

   
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
            log.error("Error deleting user from Elasticsearch: userId={}", userId, e);
            throw new AppException(ErrorCode.ELASTICSEARCH_DELETE_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> searchUsers(UserSearchFilter filter) {
        log.debug("Searching users with filter: {}", filter);
        return elasticSearchProxy.searchUsers(filter);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<SearchSuggestion> getUserSuggestions(String term, int limit) {
        log.debug("Getting user suggestions for term: {}, limit: {}", term, limit);
        return elasticSearchProxy.getUserSuggestions(term, limit);
    }

}
