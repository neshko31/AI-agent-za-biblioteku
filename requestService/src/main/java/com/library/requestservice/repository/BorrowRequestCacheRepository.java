package com.library.requestservice.repository;
import com.library.requestservice.model.BorrowRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.Optional;
@Repository
public class BorrowRequestCacheRepository {
    private final RedisTemplate<String, BorrowRequest> redisTemplate;
    private final String keyPrefix;
    private static final Duration TTL = Duration.ofMinutes(30);
    public BorrowRequestCacheRepository(
            RedisTemplate<String, BorrowRequest> redisTemplate,
            @Value("${app.redis.request-prefix}") String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
    }
    public void put(BorrowRequest request) {
        redisTemplate.opsForValue().set(buildKey(request.getId()), request, TTL);
    }
    public Optional<BorrowRequest> get(String id) {
        BorrowRequest cached = redisTemplate.opsForValue().get(buildKey(id));
        return Optional.ofNullable(cached);
    }
    public void evict(String id) {
        redisTemplate.delete(buildKey(id));
    }
    private String buildKey(String id) {
        return keyPrefix + id;
    }
}
