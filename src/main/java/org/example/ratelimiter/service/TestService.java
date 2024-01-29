package org.example.ratelimiter.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.ratelimiter.limiterservice.RateLimiterService;
import org.example.ratelimiter.repository.RedisStringRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestService {

    private final RateLimiterService rateLimiterService;

//    @PostConstruct
    public void test(){
        for(int i=0; i<210; i++) {
            System.out.println(rateLimiterService.isRateLimited("test"));
        }
    }

    public String isRateLimited(String identifier) {
        boolean isLimited = rateLimiterService.isRateLimited(identifier);
        if (isLimited) {
            return "You are rate-limited. Please try again later.";
        } else {
            return "API is working!";
        }
    }
}
