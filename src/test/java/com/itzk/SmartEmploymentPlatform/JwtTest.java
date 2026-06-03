package com.itzk.SmartEmploymentPlatform;

import com.itzk.SmartEmploymentPlatform.utils.JwtUtil;
import io.jsonwebtoken.*;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;

@SpringBootTest
@ActiveProfiles("test")
public class JwtTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${test.jwt.sample-token}")
    private String sampleToken;

    @Test
    public void testgetJwt() throws InterruptedException {

    Long userid = 1L;
    byte role = 1;
    JwtUtil jwtUtil = new JwtUtil();
    String token = jwtUtil.generateToken(userid,role);


    System.out.println(token);
        Claims claims = jwtUtil.parseToken(token);
        System.out.println(claims);
        String subject = claims.getSubject();
        Object o = claims.get("role");
        System.out.println(o);


    }

    @Test
    public void testgetJwt2(){

        String token = sampleToken;
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(token));

        Claims claims = Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token).getPayload();

        System.out.println(claims);

    }


}
