package org.example.ratelimiter.repository;

public interface RedisStringRepository {

    boolean insertValue(String key, Object value, Long ttl);

    <T> T getValue(String key, Class<T> clazz);

    boolean deleteValue(String key);

    void incrementValue(String key, Long ttl);

}
