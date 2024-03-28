package com.api19_4.api19_4.services;

import com.api19_4.api19_4.model.TokenDevice;

public interface TokenDeviceService {

    void saveTokenDevice(TokenDevice tokenDevice);
    String getTokenByUserId(String idUser);
}
