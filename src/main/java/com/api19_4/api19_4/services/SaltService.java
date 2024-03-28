package com.api19_4.api19_4.services;

import com.api19_4.api19_4.models.Salt;
import com.api19_4.api19_4.repositories.SaltRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaltService {

    @Autowired
    private SaltRepository saltRepository;
    public Salt findByEmail(String email) {
        return saltRepository.findByEmail(email);
    }
    public Salt saveSalt(Salt salt) {
        return saltRepository.save(salt);
    }

    public void deleteAllSalts() {
        saltRepository.deleteAll();
    }
}
