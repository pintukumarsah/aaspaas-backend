// // package com.aaspaas.aaspaas_backend.registration.repository;

// // import java.util.Optional;
// // import org.springframework.data.jpa.repository.JpaRepository;
// // import org.springframework.stereotype.Repository;
// // import com.aaspaas.aaspaas_backend.registration.entity.User;

// // @Repository
// // public interface UserRepository extends JpaRepository<User, Long> {
// // 	Optional<User> findByEmail(String email);
// //     Optional<User> findByUsername(String username);
// //     Optional<User> findByEmailOrUsername(String email, String username); // NEW METHOD
// //     boolean existsByEmail(String email);
// //     boolean existsByUsername(String username);
// // }


// package com.aaspaas.aaspaas_backend.user.repository;

// import com.aaspaas.aaspaas_backend.user.entity.User;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// import java.util.Optional;

// @Repository
// public interface UserRepository extends JpaRepository<User, Long> {

//     boolean existsByUsername(String username);

//     boolean existsByEmail(String email);

//     Optional<User> findByEmailOrUsername(String email, String username);
// }