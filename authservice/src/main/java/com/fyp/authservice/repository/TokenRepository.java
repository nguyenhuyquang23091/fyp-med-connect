package com.fyp.authservice.repository;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;


@Repository
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenRepository {
    StringRedisTemplate stringRedisTemplate;

    static final String ACCESS_TOKEN_PREFIX = "user:access:";
    static final String REFRESH_TOKEN_KEY_PREFIX = "user:refresh:";




    //prefixes for token blacklisting
    static final String ACCESS_BLACKLIST_PREFIX = "blacklist:access:";
    static final String REFRESH_BLACKLIST_PREFIX = "blacklist:refresh:";



    @NonFinal
    @Value("${spring.jwt.expired-duration}")
    long jwtExpiration;

    @NonFinal
    @Value("${spring.jwt.refreshable-duration}")
    long jwtRefreshExpiration;

    public void storeToken(String userId, String accessToken, String refreshToken, long accessTokenExpiration, long refreshTokenExpiration){
        String accessKey = ACCESS_TOKEN_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(accessKey, accessToken);
        stringRedisTemplate.expire(accessKey, accessTokenExpiration, TimeUnit.SECONDS);

        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(refreshKey, refreshToken);
        stringRedisTemplate.expire(refreshKey, refreshTokenExpiration, TimeUnit.SECONDS);
    }

    String getAccessToken(String userId){
        String accessKey = ACCESS_TOKEN_PREFIX + userId;
        return getToken(accessKey);
    }

    public String getRefreshToken(String userId){
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + userId;
        log.info("Getting refresh Key for user {}", refreshKey);
        return getToken(refreshKey);
    }

    private String getToken(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void removeAllToken(String userId){
        String accessToken = getAccessToken(userId);
        String refreshToken = getRefreshToken(userId);

        String accessKey   = ACCESS_TOKEN_PREFIX + userId;
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + userId;

        stringRedisTemplate.delete(accessKey);
        stringRedisTemplate.delete(refreshKey);

        if(accessToken != null){
            blackListAccessToken(accessToken, jwtExpiration);
        }

        if(refreshToken != null){
            blackListRefreshToken(refreshToken, jwtRefreshExpiration);
        }
    }

    public void removeAccessToken(String userId ){
            String accessToken = getAccessToken(userId);
            String key = ACCESS_TOKEN_PREFIX + userId;
            stringRedisTemplate.delete(key);
            if(accessToken != null){
                blackListAccessToken(accessToken, jwtExpiration);
            }

    }

    private void blackListAccessToken(String accessToken, long jwtExpiration){
        String key = ACCESS_BLACKLIST_PREFIX  + accessToken;
        stringRedisTemplate.opsForValue().set(key, "blacklisted");
        stringRedisTemplate.expire(key, jwtExpiration, TimeUnit.SECONDS);
    }

    private void blackListRefreshToken(String refreshToken, long jwtRefreshExpiration){
        String key = REFRESH_BLACKLIST_PREFIX + refreshToken;
        stringRedisTemplate.opsForValue().set(key, "blacklisted");
        stringRedisTemplate.expire(key, jwtRefreshExpiration, TimeUnit.SECONDS);
    }

    public boolean isAccessTokenBlackListed(String accessToken){
        String key  = ACCESS_BLACKLIST_PREFIX + accessToken;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public boolean isRefreshTokenBlackListed(String refreshToken){
        String key = REFRESH_BLACKLIST_PREFIX + refreshToken;
        return  Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
