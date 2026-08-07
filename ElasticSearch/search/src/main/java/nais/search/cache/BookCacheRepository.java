package nais.search.cache;

import nais.search.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class BookCacheRepository {

    private final RedisTemplate<String, Book> redisTemplate;
    private final String keyPrefix;
    private static final Duration TTL = Duration.ofMinutes(30);

    public BookCacheRepository(
            RedisTemplate<String, Book> bookRedisTemplate,
            @Value("${app.redis.book-prefix}") String keyPrefix) {
        this.redisTemplate = bookRedisTemplate;
        this.keyPrefix = keyPrefix;
    }

    public void put(Book book) {
        redisTemplate.opsForValue().set(buildKey(book.getRecordId()), book, TTL);
    }

    public Optional<Book> get(String id) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(id)));
    }

    public void evict(String id) {
        redisTemplate.delete(buildKey(id));
    }

    private String buildKey(String id) {
        return keyPrefix + id;
    }
}
