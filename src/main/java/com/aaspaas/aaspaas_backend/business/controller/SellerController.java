package com.aaspaas.aaspaas_backend.business.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> dashboard() {

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Welcome to Seller Dashboard"
                )
        );
    }
}