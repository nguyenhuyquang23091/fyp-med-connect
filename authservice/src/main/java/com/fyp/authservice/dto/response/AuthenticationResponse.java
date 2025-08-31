package com.fyp.authservice.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
     String token;
     boolean isAuthenticated; //true/false -> ben kia tra ve ket qua cua thang nay thoi

}
