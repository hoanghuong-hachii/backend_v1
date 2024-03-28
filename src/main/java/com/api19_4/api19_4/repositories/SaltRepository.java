package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Salt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaltRepository extends JpaRepository<Salt, String> {
    Salt findByEmail(String email);
}
