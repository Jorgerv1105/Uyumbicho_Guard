package com.udla.uyumbichoguard.provider.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configura el RestClient (cliente HTTP moderno de Spring, reemplaza a
 * RestTemplate) usado exclusivamente por SriAntVehiculoProvider.
 * Solo se instancia cuando el perfil "sri" está activo, para no crear
 * un bean HTTP innecesario cuando se usa el Mock.
 */
@Configuration
@Profile("sri")
@RequiredArgsConstructor
public class RestClientConfig {

    private final SriProviderProperties sriProperties;

    @Bean
    public RestClient sriRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(sriProperties.getTimeoutConexionMs()));
        factory.setReadTimeout(Duration.ofMillis(sriProperties.getTimeoutLecturaMs()));

        return RestClient.builder()
                .baseUrl(sriProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + sriProperties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }
}