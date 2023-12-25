package org.example.ratelimiter.repository;

import org.example.ratelimiter.OpsDirection;

import java.util.List;

public interface RedisListRepository {

    <T> List<T> get(String key, Class<T> clazz);

    boolean insertListElement(String key, Object value, Long ttl, OpsDirection direction);

    Long getListSize(String key);

    <T> T getAtIndex(String key, Integer index, Class<T> clazz);

    <T> T popListElement(String key, OpsDirection direction, Class<T> clazz);

    boolean deleteList(String key);

}
