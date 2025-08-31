package com.fyp.authservice.repository;

import com.fyp.authservice.entity.InvalidatedToken;
import com.fyp.authservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

}
