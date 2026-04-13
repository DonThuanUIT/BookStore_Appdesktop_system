package com.bookstore.backend.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

//    @Bean
//    public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
//        return new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//    }
        @Bean
        public SecretKey jwtSecretKey() {
            String secret = "DayLaChuoiBiMatSieuCapVipProChoBookStore2026!@#";
            return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        }

   // @Bean
  //  public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
   //     return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
   // }

//    @Bean
//    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey, JwtProperties jwtProperties) {
//        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
//                .macAlgorithm(MacAlgorithm.HS256)
//                .build();
//        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));
//        return jwtDecoder;
//    }
}
