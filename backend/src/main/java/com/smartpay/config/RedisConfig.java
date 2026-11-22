package com.smartpay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.Duration;

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.url:}")
    private String redisUrl;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.timeout:2000}")
    private long timeoutMs;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                    LettuceClientConfiguration.builder()
                            .commandTimeout(Duration.ofMillis(timeoutMs));

            RedisStandaloneConfiguration serverConfig;

            if (StringUtils.hasText(redisUrl)) {
                log.info("Configuring Redis using SPRING_DATA_REDIS_URL");
                URI uri = URI.create(redisUrl);
                boolean isTls = "rediss".equalsIgnoreCase(uri.getScheme());

                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : (isTls ? 6380 : 6379);

                serverConfig = new RedisStandaloneConfiguration(host, port);

                String userInfo = uri.getUserInfo();
                if (StringUtils.hasText(userInfo)) {
                    String[] parts = userInfo.split(":", 2);
                    if (parts.length == 2) {
                        serverConfig.setUsername(parts[0]);
                        serverConfig.setPassword(RedisPassword.of(parts[1]));
                    } else {
                        serverConfig.setPassword(RedisPassword.of(parts[0]));
                    }
                }

                if (isTls) {
                    clientConfigBuilder.useSsl();
                }
            } else {
                serverConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
                if (StringUtils.hasText(redisPassword)) {
                    serverConfig.setPassword(RedisPassword.of(redisPassword));
                }
            }

            LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfigBuilder.build());
            factory.setValidateConnection(false);
            return factory;
        } catch (Exception e) {
            log.warn("Failed to initialize Redis connection factory: {}. Falling back to default standalone.", e.getMessage());
            return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
