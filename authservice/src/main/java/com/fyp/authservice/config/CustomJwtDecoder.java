package com.fyp.authservice.config;


import com.fyp.authservice.service.AuthenticationService;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.text.ParseException;


@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${spring.jwt.singerKey}")
    protected String SINGER_KEY  ;
    @Autowired
    private AuthenticationService authenticationService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;
    // Encrypt the singerkey, then decode it using
    // the Nimbus decodes and compare the signature with the OAuth2 one
    @Override
    public Jwt decode(String token) throws JwtException{
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return new Jwt(token,
                    signedJWT.getJWTClaimsSet().getIssueTime().toInstant(),
                    signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(),
                    signedJWT.getHeader().toJSONObject(),
                    signedJWT.getJWTClaimsSet().getClaims());
        } catch (ParseException e) {
            throw new JwtException("Invalid Token");
        }
    }

}
