package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.model.ActiveAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveAccountRepository extends JpaRepository<ActiveAccount, String> {
    ActiveAccount findByEmail(String email);
}
