package com.aaspaas.aaspaas_backend.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    // public FirebaseApp firebaseApp() throws IOException {

    //     if (!FirebaseApp.getApps().isEmpty()) {
    //         return FirebaseApp.getInstance();
    //     }

    //     // try (InputStream serviceAccount =
    //     //              new ClassPathResource("firebase-service-account.json").getInputStream()) {
    //     try (InputStream serviceAccount =
    //     new FileInputStream("firebase-service-account.json")) {

    //         FirebaseOptions options = FirebaseOptions.builder()
    //                 .setCredentials(GoogleCredentials.fromStream(serviceAccount))
    //                 .build();

    //         return FirebaseApp.initializeApp(options);
    //     }
    public FirebaseApp firebaseApp() throws IOException {

    if (!FirebaseApp.getApps().isEmpty()) {
        return FirebaseApp.getInstance();
    }

    try (InputStream serviceAccount =
            new FileInputStream("firebase-service-account.json")) {

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options);
    }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    
}