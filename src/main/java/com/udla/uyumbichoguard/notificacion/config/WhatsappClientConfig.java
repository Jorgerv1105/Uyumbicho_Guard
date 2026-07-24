package com.udla.uyumbichoguard.notificacion.config;

import com.udla.uyumbichoguard.notificacion.WhatsappProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WhatsappClientConfig {

    private final WhatsappProperties whatsappProperties;

    @Bean
    public RestClient whatsappRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        String urlBase = "%s/%s/%s".formatted(
                whatsappProperties.getBaseUrl(),
                whatsappProperties.getApiVersion(),
                whatsappProperties.getPhoneNumberId()
        );

        return RestClient.builder()
                .baseUrl(urlBase)
                .defaultHeader("Authorization", "Bearer " + whatsappProperties.getAccessToken())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }
}