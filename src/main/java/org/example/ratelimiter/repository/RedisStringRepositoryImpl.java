package org.example.ratelimiter.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ratelimiter.OpsDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class RedisStringRepositoryImpl implements RedisRepository {

    private static final ObjectMapper OBJECT_MAPPER;

    private final RedisTemplate<String, String> redisTemplate;

    static {
        OBJECT_MAPPER = new ObjectMapper();
    }

    @Override
    public boolean insertValue(String key, Object value, Long ttl) {
        boolean result = false;
        try {
            if (null != value) {
                String stringSerializedObj = OBJECT_MAPPER.writeValueAsString(value);
                redisTemplate.opsForValue().set(key, stringSerializedObj);
                if (ttl > 0) {
                    redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                }
                result = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public <T> T getValue(String key, Class<T> clazz) {
        T result = null;
        try {
            if (StringUtils.hasText(key)) {
                String res = redisTemplate.opsForValue().get(key);
                if(StringUtils.hasText(res)) {
                    result = OBJECT_MAPPER.readValue(res, clazz);
                } else {
                    throw new Exception(String.format("Value not found for key: %s", key));
                }
            }
        } catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean deleteValue(String key) {
        boolean result = false;
        try {
            if(StringUtils.hasText(key)) {
                redisTemplate.opsForValue().getAndDelete(key);
                result = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }


    @Override
    public void incrementValue(String key, Long ttl) {
        try {
            if (StringUtils.hasText(key)) {
                redisTemplate.execute(new SessionCallback<List<Object>>() {
                    @Override
                    public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
                        redisTemplate.multi();
                        redisTemplate.opsForValue().increment(key);
                        if (ttl > 0) {
                            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                        }
                        return redisTemplate.exec();
                    }
                });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    @Override
    public <T> List<T> get(String key, Class<T> clazz) {
        List<T> result = null;
        try {
            if(StringUtils.hasText(key)) {
                String list = String.valueOf(redisTemplate.opsForList().range(key, 0, -1));
                result = OBJECT_MAPPER.readValue(list, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean insertListElement(String key, Object value, Long ttl, OpsDirection direction) {
        boolean result = false;
        try {
            if(StringUtils.hasText(key) && null != value) {
                String stringSerializedObj = OBJECT_MAPPER.writeValueAsString(value);
                redisTemplate.multi();
                if(direction == OpsDirection.TAIL){
                    redisTemplate.opsForList().rightPush(key, stringSerializedObj);
                } else if (direction == OpsDirection.HEAD) {
                    redisTemplate.opsForList().leftPush(key, stringSerializedObj);
                }

                if(ttl > 0L) {
                    redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                }
                redisTemplate.exec();
                result = true;
            }
        } catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Long getListSize(String key) {
        Long size = null;
        try {
            if(StringUtils.hasText(key)) {
                size = redisTemplate.opsForList().size(key);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return size;
    }

    @Override
    public <T> T getAtIndex(String key, Integer index, Class<T> clazz) {
        T result = null;
        try {
            if (StringUtils.hasText(key)) {
                String res = redisTemplate.opsForList().index(key, index);
                if (StringUtils.hasText(res)) {
                    result = OBJECT_MAPPER.readValue(res, OBJECT_MAPPER.getTypeFactory().constructType(clazz));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public <T> T popListElement(String key, OpsDirection direction, Class<T> clazz) {
        T result = null;
        try {
            if(StringUtils.hasText(key)) {
                String res = null;
                if(direction == OpsDirection.HEAD) {
                    res = redisTemplate.opsForList().leftPop(key);
                } else if (direction == OpsDirection.TAIL) {
                    res = redisTemplate.opsForList().rightPop(key);
                }

                if(null != res) {
                    result = OBJECT_MAPPER.readValue(res, OBJECT_MAPPER.getTypeFactory().constructType(clazz));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean deleteList(String key) {
        boolean result = false;
        try {
            if(StringUtils.hasText(key)) {
                redisTemplate.delete(key);
                result = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }
}
