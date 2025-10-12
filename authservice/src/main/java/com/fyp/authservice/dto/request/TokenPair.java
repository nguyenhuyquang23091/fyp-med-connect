package com.fyp.authservice.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenPair {
    String accessToken;
    String refreshToken;
    long accessTokenExpiration; //In Second
    long refreshTokenExpiration; // In second


}
