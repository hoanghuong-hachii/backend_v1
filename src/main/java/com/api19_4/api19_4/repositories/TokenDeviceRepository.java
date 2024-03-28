package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.model.TokenDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenDeviceRepository extends JpaRepository<TokenDevice, String> {
    TokenDevice findByIdUser(String idUser);

}
