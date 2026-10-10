package com.aaspaas.aaspaas_backend.user.repository;

import com.aaspaas.aaspaas_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query(
        value = """
            select distinct u from User u left join u.roles r
            where (:search is null
                or lower(u.fullName) like lower(concat('%', :search, '%'))
                or lower(coalesce(u.email, '')) like lower(concat('%', :search, '%'))
                or u.phone like concat('%', :search, '%'))
              and (:status is null or upper(u.status) = :status)
              and (:roleName is null or upper(r.name) = :roleName)
            """,
        countQuery = """
            select count(distinct u.id) from User u left join u.roles r
            where (:search is null
                or lower(u.fullName) like lower(concat('%', :search, '%'))
                or lower(coalesce(u.email, '')) like lower(concat('%', :search, '%'))
                or u.phone like concat('%', :search, '%'))
              and (:status is null or upper(u.status) = :status)
              and (:roleName is null or upper(r.name) = :roleName)
            """
    )
    Page<User> searchForAdmin(
            @Param("search") String search,
            @Param("status") String status,
            @Param("roleName") String roleName,
            Pageable pageable);

    @Query("""
        select count(distinct u.id) from User u join u.roles r
        where upper(r.name) = 'ADMIN' and upper(u.status) = 'ACTIVE'
        """)
    long countActiveAdmins();
}
