package com.api19_4.api19_4.controller;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.twilio.example.ValidationExample.ACCOUNT_SID;
import static com.twilio.example.ValidationExample.AUTH_TOKEN;

@RestController
@RequestMapping(path = "api/phoneNumber")
@Slf4j
public class PhoneNumberVerificationController {


    @GetMapping(value = "/generateOTP")
    public ResponseEntity<String> generateOTP(){

        Twilio.init("AC8ab3aa96591270b11f538e214607fcff", "9b3a327d061f44477f942b100a0d95bc");

        try {
            Verification verification = Verification.creator(
                            "VAe0b32261a62374c81d64e53c336e8af2", // this is your verification sid
                            "+12019320758", //this is your Twilio verified recipient phone number
                            "sms") // this is your channel type
                    .create();

            System.out.println(verification.getStatus());
        }
         catch (Exception e) {
        log.error("Error during OTP verification", e);
        return new ResponseEntity<>("Verification failed. " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
        log.info("OTP has been successfully generated, and awaits your verification {}", LocalDateTime.now());

        return new ResponseEntity<>("Your OTP has been sent to your verified phone number", HttpStatus.OK);
    }
    @GetMapping("/verifyOTP")
    public ResponseEntity<?> verifyUserOTP() throws Exception {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        try {

            VerificationCheck verificationCheck = VerificationCheck.creator(
                            "AC90c0ef163e7a68a2cf73b6f5d90dc921")
                    .setTo("+84383696281")
                    .setCode("486578")
                    .create();

            System.out.println(verificationCheck.getStatus());

        } catch (Exception e) {
            return new ResponseEntity<>("Verification failed.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("This user's verification has been completed successfully", HttpStatus.OK);
    }

}
