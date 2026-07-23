package com.umc.todayter.global.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/actuator/health")
public class HealthController {
    @GetMapping
    public ResponseEntity<String> healthCheck(){
        return  ResponseEntity.ok("Health OK");
    }
}
