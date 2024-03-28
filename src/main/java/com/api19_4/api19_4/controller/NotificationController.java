package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.model.NotificationRequest;
import com.api19_4.api19_4.model.TokenDevice;
import com.api19_4.api19_4.services.TokenDeviceService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class NotificationController {
    @Autowired
    private TokenDeviceService tokenDeviceService;

    @PostMapping("/register-device")
    public ResponseEntity<String> registerDevice(@RequestBody TokenDevice tokenDevice) {

        tokenDeviceService.saveTokenDevice(tokenDevice);

        return ResponseEntity.ok("Device registered successfully.");
    }
    @PostMapping("/send-notification")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest notificationRequest) {
        Message message = Message.builder()
                .putData("title", notificationRequest.getTitle())
                .putData("body", notificationRequest.getBody())
                .setToken(notificationRequest.getDeviceToken())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            return ResponseEntity.ok("Notification sent successfully.");
        } catch (FirebaseMessagingException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send notification: " + e.getMessage());
        }
    }

    @GetMapping("/notification/token/{idUser}")
    public ResponseEntity<String> getTokenByUserId(@PathVariable("idUser") String idUser) {
        String token = tokenDeviceService.getTokenByUserId(idUser);
        return ResponseEntity.ok(token);
    }


}
