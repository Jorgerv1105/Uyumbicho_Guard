package com.udla.uyumbichoguard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Habilita @Async para que el envío de WhatsApp (I/O de red hacia
 * Meta) NUNCA bloquee ni ponga en riesgo la transacción principal que
 * lo dispara (registrar un ingreso, generar una alerta). Si Meta está
 * lento o caído, la garita sigue funcionando con normalidad — el envío
 * de la notificación simplemente queda registrado como FALLIDO.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "whatsappTaskExecutor")
    public Executor whatsappTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("whatsapp-async-");
        executor.initialize();
        return executor;
    }
}