package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.service.EmailService;
import com.api19_4.api19_4.service.OTPService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
@RequestMapping(path = "/api/testOTP")
public class OTPController {
	

@Autowired
public OTPService otpService;
@Autowired
public EmailService emailService;

	@GetMapping("/generateOtp")
	public String generateOTP(@RequestParam("email") String email) throws MessagingException {

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String username = auth.getName();
			int otp = otpService.generateOTP(email);

			emailService.sendOtpMessage(
					"otpt01720@gmail.com",
					"OTP - OfficeOrder",
					"Mã OTP của bạn là: "+ otp +
							", " +"mã có thời hạn 1 phút");

			return "success";
		}
	@GetMapping(value ="/validateOtp")
	public String validateOtp(@RequestParam("email") String email,@RequestParam("otpnum") int otpnum){

			String SUCCESS = "Xác minh thành công";
			String FAIL = "OTP không hợp lệ, Vui lòng thử lại!";

			if(otpnum >= 0){
			  int serverOtp = otpService.getOtp(email);
				if(serverOtp > 0){
				  if(otpnum == serverOtp){
					  otpService.clearOTP(email);
					  return SUCCESS;
					}
					else {
						return FAIL;
					   }
				   }else {
				  return FAIL;
				   }
				 }else {
					return FAIL;
			 }
		  }
	}