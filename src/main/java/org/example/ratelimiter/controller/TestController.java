package org.example.ratelimiter.controller;

import org.example.ratelimiter.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    TestService testService;

    @GetMapping("/test")
    public String test(@RequestParam String id) {
        return testService.isRateLimited(id);
    }


}
