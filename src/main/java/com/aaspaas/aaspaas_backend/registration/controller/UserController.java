// package com.aaspaas.aaspaas_backend.registration.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import com.aaspaas.aaspaas_backend.registration.dto.LoginRequest;
// import com.aaspaas.aaspaas_backend.registration.dto.SignupRequest;
// import com.aaspaas.aaspaas_backend.registration.dto.UserResponse; // <-- Ensure this is present
// import com.aaspaas.aaspaas_backend.registration.service.UserService;
// @RestController
// @RequestMapping("/api")
// @CrossOrigin(originPatterns = "*", allowCredentials = "true") 
// public class UserController {

//     @Autowired
//     private UserService userService;

//     @PostMapping("/signup")
//     public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest request) {
//         UserResponse response = userService.signup(request);
//         return new ResponseEntity<>(response, HttpStatus.OK);
//     }

//     @PostMapping("/login")
//     public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
//         UserResponse response = userService.login(request);
//         return new ResponseEntity<>(response, HttpStatus.OK);
//     }

//     @PostMapping("/logout/{userId}")
//     public ResponseEntity<UserResponse> logout(@PathVariable Long userId) {
//         UserResponse response = userService.logout(userId);
//         return new ResponseEntity<>(response, HttpStatus.OK);
//     }
// }