package com.fyp.authservice.controller;


import com.fyp.authservice.dto.request.*;
import com.fyp.authservice.dto.response.AuthenticationResponse;
import com.fyp.authservice.dto.response.IntrospectResponse;
import com.fyp.authservice.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.coyote.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest){
     var result =  authenticationService.authenticate(authenticationRequest);
     return ApiResponse.<AuthenticationResponse>builder()
             .result(result)
             .build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> validateValid(@RequestBody IntrospectRequest introspectRequest) throws JOSEException, ParseException {

        var result = authenticationService.introspect(introspectRequest);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogOutRequest request)
            throws JOSEException, ParseException {
       authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest refreshRequest) throws JOSEException, ParseException{
        var result =  authenticationService.refreshToken(refreshRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

}
