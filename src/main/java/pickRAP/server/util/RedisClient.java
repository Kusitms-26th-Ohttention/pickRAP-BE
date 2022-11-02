package pickRAP.server.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import pickRAP.server.config.security.jwt.TokenProvider;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisClient {

    private final RedisTemplate<String, String> redisTemplate;


    // 로그인 시, 발급
    public void setValues(String key, String data) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data, Duration.ofDays(7));
    }

    // 이메일 - 3분
    public void setEmail(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, key, Duration.ofMinutes(3));
    }


    public String getValues(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(key);
    }


    //로그아웃
    public void deleteValues(String key) {
        log.info("로그아웃");
        redisTemplate.delete(key);
    }
}
