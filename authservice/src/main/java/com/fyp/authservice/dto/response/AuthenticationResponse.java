package com.fyp.authservice.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
     String accessToken;
     String refreshToken;
     boolean isAuthenticated;
     long accessTokenExpires;
     //true/false -> ben kia tra ve ket qua cua thang nay thoi
     String role;
}
