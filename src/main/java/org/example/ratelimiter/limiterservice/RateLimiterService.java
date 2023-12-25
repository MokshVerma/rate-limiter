package org.example.ratelimiter.limiterservice;

public interface RateLimiterService {

    boolean isRateLimited(String identifier);

}
