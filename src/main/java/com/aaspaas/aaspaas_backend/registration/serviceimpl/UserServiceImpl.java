// package com.aaspaas.aaspaas_backend.registration.serviceimpl;

// import com.aaspaas.aaspaas_backend.registration.dto.LoginRequest;
// import com.aaspaas.aaspaas_backend.registration.dto.SignupRequest;
// import com.aaspaas.aaspaas_backend.registration.dto.UserResponse; // or its actual location
// import com.aaspaas.aaspaas_backend.registration.service.UserService;
// import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
// import com.aaspaas.aaspaas_backend.user.entity.User; // verify your actual UserRepository package
// import java.sql.Timestamp;
// import java.time.Instant;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// @Service
// public class UserServiceImpl implements UserService {
    
//     private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    
//     @Autowired
//     private UserRepository userRepository;
    
//     @Override
//     public UserResponse signup(SignupRequest request) {
//         UserResponse response = new UserResponse();
        
//         try {
//             log.info("📝 Signup attempt for email: {}", request.getEmail());
            
//             // Check if email already exists
//             if (userRepository.existsByEmail(request.getEmail())) {
//                 log.warn("⚠️ Email already exists: {}", request.getEmail());
//                 response.setStatus("0");
//                 response.setMessage("Email already registered!");
//                 return response;
//             }
            
//             // Check if username already exists
//             if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
//                 log.warn("⚠️ Username already exists: {}", request.getUsername());
//                 response.setStatus("0");
//                 response.setMessage("Username already exists!");
//                 return response;
//             }
            
//             // Create new user
//             User user = new User();
//             user.setName(request.getName());
//             user.setUsername(request.getUsername());
//             user.setEmail(request.getEmail());
//             user.setPassword(request.getPassword()); // In production, hash the password!
//             user.setRole(request.getRole() != null ? request.getRole() : "USER");
//             user.setIsactive(true);
//             user.setIsverified(false);
//             user.setIsdelete(false);
//             user.setCreatedat(Timestamp.from(Instant.now()));
//             user.setVersion(0);
            
//             User savedUser = userRepository.save(user);
//             log.info("✅ User registered - ID: {}, Email: {}, Username: {}", 
//                      savedUser.getId(), savedUser.getEmail(), savedUser.getUsername());
            
//             response.setStatus("1");
//             response.setMessage("User registered successfully!");
//             response.setData(savedUser);
            
//         } catch (Exception e) {
//             log.error("❌ Error during signup: ", e);
//             response.setStatus("0");
//             response.setMessage("Signup failed: " + e.getMessage());
//         }
        
//         return response;
//     }
    
//     @Override
//     public UserResponse login(LoginRequest request) {
//         UserResponse response = new UserResponse();
        
//         try {
//             String emailOrUsername = request.getEmailOrUsername();
//             log.info("🔐 Login attempt with: {}", emailOrUsername);
            
//             // Try to find user by email OR username
//             User user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername)
//                                      .orElse(null);
            
//             if (user == null) {
//                 log.warn("❌ User not found: {}", emailOrUsername);
//                 response.setStatus("0");
//                 response.setMessage("User not found! Please check your email or username.");
//                 return response;
//             }
            
//             log.info("👤 User found - ID: {}, Name: {}, Email: {}, Username: {}", 
//                      user.getId(), user.getName(), user.getEmail(), user.getUsername());
            
//             // Check if user is deleted
//             if (user.getIsdelete() != null && user.getIsdelete()) {
//                 log.warn("⚠️ User account is deleted: {}", emailOrUsername);
//                 response.setStatus("0");
//                 response.setMessage("User account is inactive!");
//                 return response;
//             }
            
//             // Check if user is active
//             if (user.getIsactive() != null && !user.getIsactive()) {
//                 log.warn("⚠️ User account is not active: {}", emailOrUsername);
//                 response.setStatus("0");
//                 response.setMessage("User account is inactive!");
//                 return response;
//             }
            
//             // Verify password (In production, use password hashing!)
//             log.info("🔑 Verifying password...");
//             if (!user.getPassword().equals(request.getPassword())) {
//                 log.warn("❌ Invalid password for: {}", emailOrUsername);
//                 response.setStatus("0");
//                 response.setMessage("Invalid password!");
//                 return response;
//             }
            
//             log.info("✅ Login successful for: {} ({})", user.getName(), user.getEmail());
            
//             response.setStatus("1");
//             response.setMessage("Login successful! Welcome " + user.getName());
//             response.setData(user);
            
//         } catch (Exception e) {
//             log.error("❌ Error during login: ", e);
//             response.setStatus("0");
//             response.setMessage("Login failed: " + e.getMessage());
//         }
        
//         return response;
//     }
    
//     @Override
//     public UserResponse logout(Long userId) {
//         UserResponse response = new UserResponse();
        
//         try {
//             User user = userRepository.findById(userId).orElse(null);
            
//             if (user == null) {
//                 response.setStatus("0");
//                 response.setMessage("User not found!");
//                 return response;
//             }
            
//             log.info("👋 User logged out: {}", user.getEmail());
//             response.setStatus("1");
//             response.setMessage("Logout successful!");
            
//         } catch (Exception e) {
//             log.error("❌ Error during logout: ", e);
//             response.setStatus("0");
//             response.setMessage("Logout failed: " + e.getMessage());
//         }
        
//         return response;
//     }
// }