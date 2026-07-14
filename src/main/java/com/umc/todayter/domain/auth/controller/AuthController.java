package com.umc.todayter.domain.auth.controller;

import com.umc.todayter.global.apiPayload.response.ApiResponse;
import com.umc.todayter.global.apiPayload.response.SuccessCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.onSuccess("test", SuccessCode.OK));
    }
}
