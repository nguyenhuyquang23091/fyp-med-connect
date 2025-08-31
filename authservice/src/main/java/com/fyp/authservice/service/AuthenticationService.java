package com.fyp.authservice.service;

import com.fyp.authservice.dto.request.AuthenticationRequest;
import com.fyp.authservice.dto.request.IntrospectRequest;
import com.fyp.authservice.dto.request.LogOutRequest;
import com.fyp.authservice.dto.request.RefreshRequest;
import com.fyp.authservice.dto.response.AuthenticationResponse;
import com.fyp.authservice.dto.response.IntrospectResponse;
import com.fyp.authservice.entity.InvalidatedToken;
import com.fyp.authservice.entity.User;
import com.fyp.authservice.exceptions.AppException;
import com.fyp.authservice.exceptions.ErrorCode;
import com.fyp.authservice.repository.InvalidatedTokenRepository;
import com.fyp.authservice.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    //optimal way to inject instance
     UserRepository userRepository;
     InvalidatedTokenRepository tokenRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    //use @NonFinal to avoid being called in the constructor
     @NonFinal
     //@value annotation to read from YAML file
     @Value("${spring.jwt.singerKey}")
     protected String SINGER_KEY  ;
     //validate token
    @NonFinal
    @Value("${spring.jwt.expired-duration}")
    protected long EXPIRED_DURATION;

    @NonFinal
    @Value("${spring.jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) throws  JOSEException,
            ParseException {

        var token = introspectRequest.getToken();
        boolean isValid = true;

       try{
           verifyToken(token, false);
       } catch (AppException e){
           isValid = false;
       }
        return IntrospectResponse.builder()
                .isValid(isValid)
                .build();
    }
     //dto auth request
    public AuthenticationResponse authenticate(AuthenticationRequest authRequest){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        
        // Determine which credential to use (email or username)
        String loginCredential = authRequest.getEmail() != null ? authRequest.getEmail() : authRequest.getUsername();
        
        var user = userRepository.findByEmailOrUsername(loginCredential)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(authRequest.getPassword(), user.getPassword());
        log.info("Current user is authenticating is {}", user.getEmail() +  user.getUsername());
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .isAuthenticated(true)
                .build();
    }

    private String generateToken(User user) {
        //header include the algorithm we will use//
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        //the content we will send in the token//

        JWTClaimsSet jwtClaimsSet  = new JWTClaimsSet.Builder().
                subject(user.getId()).
                issuer("fyp.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(EXPIRED_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("email", user.getEmail())
                .build();
        //Payload's constructor need jwtclaimset as parameter
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        //sign
        try {
            jwsObject.sign(new MACSigner(SINGER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("can not create token");
            throw new RuntimeException(e);
        }
    }
    public void logout(LogOutRequest request) throws  JOSEException, ParseException{
        try{
            var signToken = verifyToken(request.getToken(), true);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            tokenRepository.save(invalidatedToken);
        } catch (AppException e){
        log.info("Token already expired");
        }


    }
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException{
        JWSVerifier verifier = new MACVerifier(SINGER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);


        Date expiryTime = (isRefresh) ?
                new Date(signedJWT.getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli() )
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if( !(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }
    public AuthenticationResponse refreshToken(RefreshRequest request) throws  JOSEException, ParseException{
        var signedJWT  = verifyToken(request.getToken(), true);
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var email = signedJWT.getJWTClaimsSet().getSubject();
        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByEmailOrUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .isAuthenticated(true)
                .build();
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
       if(!CollectionUtils.isEmpty(user.getRoles())){
        user.getRoles().forEach(role ->{ stringJoiner.add("ROLE_" + role.getName());
            role.getPermissions().forEach(permission -> {stringJoiner.add(permission.getName());
            });
        });
    }
        return stringJoiner.toString();
    };


}
