package com.yesgrad.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {
    
    @Autowired
    private DatabaseClient databaseClient;
    
    @GetMapping("/test-db")
    public Mono<String> testDatabase() {
        return databaseClient.sql("SELECT 1 as test")
            .fetch()
            .first()
            .map(result -> "Database connection successful: " + result.toString())
            .onErrorReturn("Database connection failed");
    }
}