package com.fyp.authservice.repository;

import com.fyp.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    @Query("SELECT u from User u WHERE u.email = :emailOrUserName OR u.username = :emailOrUserName")
    Optional<User> findByEmailOrUsername(@Param("emailOrUserName") String emailOrUserName);

    @Query("SELECT u.id FROM User u JOIN u.roles r WHERE r.name= :roleName")
    Optional<List<String>> findUserIdByRole(@Param("roleName") String roleName);
}
