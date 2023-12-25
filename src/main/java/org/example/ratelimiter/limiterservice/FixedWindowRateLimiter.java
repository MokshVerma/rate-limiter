package org.example.ratelimiter.limiterservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ratelimiter.Constants;
import org.example.ratelimiter.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FixedWindowRateLimiter implements RateLimiterService {

    private final RedisRepository redisRepository;

    @Value("${rate-limiter.fixed-window.allowed-request-count}")
    private Long windowSize;

    @Value("${rate-limiter.fixed-window.time-frame}")
    private Integer timeFrame;

    public boolean isRateLimited(String identifier) {
        LocalTime time = LocalTime.now();
        boolean isRateLimited = true;

        String key = createKey(identifier, getMinute(time).toString());
        Integer counter = redisRepository.getValue(key, Integer.class);
        if (null == counter || counter < windowSize) {
            redisRepository.incrementValue(key, timeFrame * Constants.MINUTES_TO_SECONDS);
            isRateLimited = false;
        }
        return isRateLimited;
    }

    private String createKey(String ...keyIdentifiers) {
        return Constants.CACHE_PREFIX + ":" + String.join(":", keyIdentifiers);
    }

    private Integer getMinute(LocalTime time) {
        int minute = time.getMinute();
        if (minute % timeFrame != 0) {
            minute = minute - (minute % timeFrame);
        }
        return minute;
    }

}
