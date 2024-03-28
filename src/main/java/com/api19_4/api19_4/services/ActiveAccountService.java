package com.api19_4.api19_4.services;

import com.api19_4.api19_4.model.ActiveAccount;
import com.api19_4.api19_4.models.Salt;
import com.api19_4.api19_4.repositories.ActiveAccountRepository;
import com.api19_4.api19_4.repositories.SaltRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActiveAccountService {

    @Autowired
    private ActiveAccountRepository saltRepository;
    public ActiveAccount findByEmail(String email) {
        return saltRepository.findByEmail(email);
    }
    public ActiveAccount saveSalt(ActiveAccount salt) {
        return saltRepository.save(salt);
    }
}
