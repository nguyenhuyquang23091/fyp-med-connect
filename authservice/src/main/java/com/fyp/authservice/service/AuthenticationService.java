package com.fyp.authservice.service;

import com.fyp.authservice.config.CustomJwtDecoder;
import com.fyp.authservice.dto.request.AuthenticationRequest;
import com.fyp.authservice.dto.request.IntrospectRequest;
import com.fyp.authservice.dto.request.LogOutRequest;
import com.fyp.authservice.dto.request.RefreshRequest;
import com.fyp.authservice.dto.request.TokenPair;
import com.fyp.authservice.dto.response.AuthenticationResponse;
import com.fyp.authservice.dto.response.IntrospectResponse;
import com.fyp.authservice.entity.User;
import com.fyp.authservice.exceptions.AppException;
import com.fyp.authservice.exceptions.ErrorCode;
import com.fyp.authservice.mapper.RoleMapper;
import com.fyp.authservice.repository.TokenRepository;
import com.fyp.authservice.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    private final RoleMapper roleMapper;
    //optimal way to inject instance
     UserRepository userRepository;
     TokenRepository redisTokenRepo;
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
       } catch (Exception e){
           isValid = false;
       }
        return IntrospectResponse.builder()
                .isValid(isValid)
                .build();
    }
     //dto auth request
    public AuthenticationResponse authenticate(AuthenticationRequest authRequest) throws ParseException, JOSEException {
        String role = null;
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        long accessTokenExpires;

        // Determine which credential to use (email or username)
        String loginCredential = authRequest.getEmail() != null ? authRequest.getEmail() : authRequest.getUsername();

        var user = userRepository.findByEmailOrUsername(loginCredential)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(authRequest.getPassword(), user.getPassword());

        log.info("Current user is authenticating is {}", user.getEmail() +  user.getUsername());
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        TokenPair token = createTokenPair(user);
        //store in redis TOken
        redisTokenRepo.storeToken(user.getId(),
                token.getAccessToken(), token.getRefreshToken(),
                token.getAccessTokenExpiration(),
                token.getRefreshTokenExpiration());

         SignedJWT signedJWT = verifyToken(token.getAccessToken(), false);
         JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
         String scope = jwtClaimsSet.getStringClaim("scope");

         // Extract role from verified token
         if(scope != null){
             String[] scopeParts = scope.split(" ");
             for (String scopePart : scopeParts){
                 if(scopePart.startsWith("ROLE_")){
                     role = scopePart.substring(5);
                     break;
                 }
             }
         }
        accessTokenExpires = jwtClaimsSet.getExpirationTime().getTime();

        return AuthenticationResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .isAuthenticated(true)
                .accessTokenExpires(accessTokenExpires)
                .role(role)
                .build();
    }


    private TokenPair createTokenPair(User user){
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        TokenPair tokenPair = TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiration(EXPIRED_DURATION)
                .refreshTokenExpiration(REFRESHABLE_DURATION)
                .build();
        return  tokenPair;
    }

    private String generateRefreshToken(User user){
        return generateToken(user.getId(), REFRESHABLE_DURATION, null);
    }

    private String generateAccessToken(User user){
        return generateToken(user.getId(), EXPIRED_DURATION, user);
    }

    private String generateToken(String userId, long expirationS, User user) {
        //header include the algorithm we will use//
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        //the content we will send in the token//
        JWTClaimsSet.Builder jwtClaimSet  = new JWTClaimsSet.Builder().
                subject(userId).
                issuer("fyp.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(expirationS, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString());
        if(user != null){
            jwtClaimSet.claim("scope", buildScope(user))
                    .claim("email", user.getEmail());
        }

        JWTClaimsSet jwtClaimsSet = jwtClaimSet.build();
        //Payload's constructor need jwtclaimset as parameter
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        //sign
        try {
            jwsObject.sign(new MACSigner(SINGER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

    public void logout(LogOutRequest request) throws  JOSEException, ParseException{
        try{
            var signToken = verifyToken(request.getToken(), false);
           var userId = signToken.getJWTClaimsSet().getSubject();
            redisTokenRepo.removeAllToken(userId);
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

        if(isRefresh){
            if(redisTokenRepo.isRefreshTokenBlackListed(token)){
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        } else if (!isRefresh){
            if (redisTokenRepo.isAccessTokenBlackListed(token)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }
        return signedJWT;
    }


    public AuthenticationResponse refreshToken(RefreshRequest request) throws  JOSEException, ParseException{
        var refreshToken = request.getToken();

        log.info("Received refresh token: {}", refreshToken);
        log.info("Token length: {}", refreshToken != null ? refreshToken.length() : "null");
        log.info("Token starts with: {}", refreshToken != null && refreshToken.length() > 10 ? refreshToken.substring(0, 10) : "N/A");

        var signedJWT  = verifyToken(refreshToken, true);
        var userId = signedJWT.getJWTClaimsSet().getSubject();

        String storedRefreshToken = redisTokenRepo.getRefreshToken(userId);

        log.info("Current user ID: {}", userId);
        log.info("Stored refresh token: {}", storedRefreshToken);
        log.info("Tokens match: {}", storedRefreshToken != null && storedRefreshToken.equals(refreshToken));

        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        //find and create new authentication object
        var user = userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.UNAUTHENTICATED));

        var newAccessToken = generateAccessToken(user);

        redisTokenRepo.removeAccessToken(userId);
        redisTokenRepo.storeToken(userId, newAccessToken, refreshToken,EXPIRED_DURATION, REFRESHABLE_DURATION);

        SignedJWT signedAccessToken =SignedJWT.parse(newAccessToken);
        long accessTokenExpires = signedAccessToken.getJWTClaimsSet().getExpirationTime().getTime();

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .accessTokenExpires(accessTokenExpires)
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
