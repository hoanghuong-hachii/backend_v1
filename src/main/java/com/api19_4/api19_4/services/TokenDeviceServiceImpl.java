package com.api19_4.api19_4.services;

import com.api19_4.api19_4.model.TokenDevice;
import com.api19_4.api19_4.repositories.TokenDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenDeviceServiceImpl implements TokenDeviceService {
    @Autowired
    private TokenDeviceRepository tokenDeviceRepository;

    @Override
    public void saveTokenDevice(TokenDevice tokenDevice) {
        // Tìm kiếm token theo idUser
        TokenDevice existingToken = tokenDeviceRepository.findByIdUser(tokenDevice.getIdUser());

        if (existingToken != null) {
            // Nếu đã có token cho idUser, thì cập nhật token mới
            existingToken.setTokenDevice(tokenDevice.getTokenDevice());
            tokenDeviceRepository.save(existingToken);
        } else {
            // Nếu chưa có token cho idUser, thì thêm mới
            tokenDeviceRepository.save(tokenDevice);
        }
    }

    @Override
    public String getTokenByUserId(String idUser) {
        TokenDevice tokenDevice = tokenDeviceRepository.findByIdUser(idUser);

        if (tokenDevice != null) {
            return tokenDevice.getTokenDevice();
        } else {
            return null; // or handle the case where no token is found for the user
        }
    }

}
