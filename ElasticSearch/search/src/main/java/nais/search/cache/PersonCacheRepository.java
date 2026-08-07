package nais.search.cache;

import nais.search.model.Person;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class PersonCacheRepository {

    private final RedisTemplate<String, Person> redisTemplate;
    private final String keyPrefix;
    private static final Duration TTL = Duration.ofMinutes(30);

    public PersonCacheRepository(
            RedisTemplate<String, Person> personRedisTemplate,
            @Value("${app.redis.person-prefix}") String keyPrefix) {
        this.redisTemplate = personRedisTemplate;
        this.keyPrefix = keyPrefix;
    }

    public void put(Person person) {
        redisTemplate.opsForValue().set(buildKey(person.getPersonId()), person, TTL);
    }

    public Optional<Person> get(String id) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(id)));
    }

    public void evict(String id) {
        redisTemplate.delete(buildKey(id));
    }

    private String buildKey(String id) {
        return keyPrefix + id;
    }
}
