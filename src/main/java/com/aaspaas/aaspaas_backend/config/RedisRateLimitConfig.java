package com.aaspaas.aaspaas_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisRateLimitConfig {

    @Bean
    public DefaultRedisScript<Long> rateLimitIncrementScript() {
        String luaScript = """
                local current = redis.call('INCR', KEYS[1])

                if current == 1 then
                    redis.call('PEXPIRE', KEYS[1], ARGV[1])
                end

                return current
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        return script;
    }
}