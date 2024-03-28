package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.DTOJWT.JwtResponse;
import com.api19_4.api19_4.DTOJWT.RefreshTokenRequest;
import com.api19_4.api19_4.dto.LoginDto;
import com.api19_4.api19_4.dto.UserDto;

import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.ResponseObject;
import com.api19_4.api19_4.models.UserInfo;
import com.api19_4.api19_4.repositories.UserRepositories;
import com.api19_4.api19_4.serviceJWT.JwtService;
import com.api19_4.api19_4.serviceJWT.RefreshTokenService;
import com.api19_4.api19_4.services.UserService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RestController
@RequestMapping(path = "/api/v2/users/")

public class UserController {
    private static final Path CURRENT_FOLDER = Paths.get(System.getProperty("user.dir"));
    @Autowired
    private UserRepositories repositories;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping(path = "refreshToken")
    public JwtResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return refreshTokenService.findByToken(refreshTokenRequest.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(com.api19_4.api19_4.entity.RefreshToken::getUser)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo.getUserName());
                    return JwtResponse.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequest.getToken())
                            .expirationTime(new Date(System.currentTimeMillis()+1000*60*2).toInstant())
                            .build();
                }).orElseThrow(() -> new RuntimeException(
                        "Refresh token is not in database!"));
    }

    //======================================== search user================================
    @GetMapping("/roleAdmin")
    List<UserInfo> getAllUsers(){
        return repositories.findAll();
    }

    @GetMapping("/id")
    ResponseEntity<?> searchUserId(@RequestParam String idUser){
        Optional<UserInfo> userOptional = repositories.findById(idUser);
        if(userOptional.isPresent()){
            UserInfo user = userOptional.get();
            UserDto userDto = new UserDto();
            userDto.setIdUser(user.getIdUser());
            userDto.setUserName(user.getUserName());
            userDto.setPassword(user.getPassword());
            userDto.setEmail(user.getEmail());
            userDto.setPhoneNumber(user.getPhoneNumber());
            userDto.setGender(user.getGender());
            userDto.setAddress(user.getAddress());
            userDto.setAvatar(user.getAvatar());
            userDto.setBackground(user.getBackground());
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("Failed", "Account doesn't exist", "")
            );
        }

    }

    @GetMapping("roleAdmin/searchUser")
    public ResponseEntity<List<UserDto>> searchWarehouse(
            @RequestParam(required = false) String name
    ){
        List<UserInfo> userList;
        if(name != null && !name.isEmpty()){
            userList = repositories.findByUserNameContainingIgnoreCase(name);
        }else{
            userList = repositories.findAll();
        }
        List<UserDto> userDtos = userList.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }


    //======================================== upload file ================================
    @PostMapping("roleAdmin/upload-excel")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UserInfo> createUserFromExcel(@RequestParam("excelFile") MultipartFile excelFile) throws IOException {
        // Đọc dữ liệu từ tệp Excel vào danh sách sản phẩm
        List<UserInfo> userList = new ArrayList<>();
        int numberOfExistingUser = repositories.countAllUsers();
        if(numberOfExistingUser == 0){
            numberOfExistingUser = 1;
        }else {
            numberOfExistingUser += 1;
        }
        IDGenerator idGenerator = new IDGenerator("US", numberOfExistingUser);

        try (InputStream is = excelFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                UserInfo user = new UserInfo(idGenerator);
                user.setUserName(row.getCell(0).getStringCellValue());
                user.setPassword(row.getCell(1).getStringCellValue());
                user.setEmail(row.getCell(2).getStringCellValue());
                user.setPhoneNumber(row.getCell(3).getStringCellValue());
                user.setAddress(row.getCell(4).getStringCellValue());
                user.setGender(row.getCell(5).getStringCellValue());
                // Lưu sản phẩm vào danh sách
                userList.add(user);
            }
            return repositories.saveAll(userList);
        }
    }



    //======================================== register ================================
    @PostMapping(path = "/signupUser")
    public ResponseEntity<?> signupUser(@RequestBody UserDto userDto) {
        UserInfo foundUsers = repositories.findByUserName(userDto.getUserName().trim());
        if (foundUsers != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseObject("failed", "User name already taken", "")
            );
        }

        List<UserInfo> foundPhone = repositories.findByPhoneNumber(userDto.getPhoneNumber().trim());
        if (foundPhone.size() > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseObject("failed", "Phone number already taken", "")
            );
        }

        UserInfo foundUser = repositories.findByEmail(userDto.getEmail().trim());
        if (foundUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseObject("failed", "Email already taken", "")
            );
        }

        ResponseObject signupMessage = userService.addUser(userDto);
        return ResponseEntity.ok(signupMessage);
    }

    //======================================== login ================================
    @PostMapping(path = "login")

    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto){
        ResponseObject loginMessage = userService.loginUser(loginDto);

        return ResponseEntity.ok(loginMessage);
    }

    //======================================== delete ================================
    @DeleteMapping("roleAdmin/{id}")
    ResponseEntity<ResponseObject> deleteUser(@PathVariable String id){
        boolean exists = repositories.existsById(id);
        if(exists){
            repositories.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete user successfully", "")
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("failed", "Cannot find user to delete", "")
        );
    }

    //    ==================================Update User============================================
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<ResponseObject> updateUser(
            @PathVariable("id") String id,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value ="password", required = false) String password,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phoneNumber",required = false) String phoneNumber,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "avatar",required = false) MultipartFile avatar,
            @RequestParam(value = "background",required = false) MultipartFile background

    ) {
        try {
            Optional<UserInfo> optionalUser = repositories.findById(id);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseObject("fail ed", "User not found", "")
                );
            }


            UserInfo existingUser = optionalUser.get();

            if (userName != null) {
                existingUser.setUserName(userName);
            }
            if (password != null) {
                existingUser.setPassword(password);
            }
            if (email != null) {
                existingUser.setEmail(email);

            }
            if (phoneNumber != null) {
                existingUser.setPhoneNumber(phoneNumber);
            }
            if (address != null) {
                existingUser.setAddress(address);
            }

            if (gender != null) {
                existingUser.setGender(gender);
            }
            try {
                if (avatar != null && !avatar.isEmpty()) {
                    String originalFilename = avatar.getOriginalFilename();

                    System.out.println("Avatar: "+ originalFilename);
                    String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                    String newFilename = id + "_avatar_" + System.currentTimeMillis() + extension;

                    String currentImage = existingUser.getAvatar();
                    if (currentImage != null && !currentImage.isEmpty()) {
                        Path imagePath = Paths.get(currentImage);
                        Files.deleteIfExists(imagePath);
                    }

                    Path staticPath = Paths.get("static");
                    Path imagePath = Paths.get("images");
                    if (!Files.exists(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath))) {
                        Files.createDirectories(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath));
                    }

                    Path file = CURRENT_FOLDER.resolve(staticPath)
                            .resolve(imagePath).resolve(newFilename);

                    try (OutputStream os = Files.newOutputStream(file)) {
                        os.write(avatar.getBytes());
                    }

                    existingUser.setAvatar(imagePath.resolve(newFilename).toString());
                }

                if (background != null && !background.isEmpty()) {
                    String originalFilename = background.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                    String newFilename = id + "_background_" + System.currentTimeMillis() + extension;

                    String currentImage = existingUser.getAvatar();
                    if (currentImage != null && !currentImage.isEmpty()) {
                        Path imagePath = Paths.get(currentImage);
                        Files.deleteIfExists(imagePath);
                    }

                    Path staticPath = Paths.get("static");
                    Path imagePath = Paths.get("images");
                    if (!Files.exists(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath))) {
                        Files.createDirectories(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath));
                    }

                    Path file = CURRENT_FOLDER.resolve(staticPath)
                            .resolve(imagePath).resolve(newFilename);

                    try (OutputStream os = Files.newOutputStream(file)) {
                        os.write(background.getBytes());
                    }

                    existingUser.setBackground(imagePath.resolve(newFilename).toString());
                }


            } catch (IOException e) {
            }


            UserInfo updatedUser = repositories.save(existingUser);

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Update User successfully", null)
            );
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject("failed", "Error while updating User", e.getMessage())
            );
        }
    }

    @GetMapping("/getUsernameByEmail")
    public ResponseEntity<?> getUsernameByEmail(@RequestParam String email) {
        UserInfo user = repositories.findByEmail(email);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.OK).body(user.getUserName());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("failed", "User not found for email: " + email, "")
            );
        }
    }

    @GetMapping("/changePassword")
    public ResponseEntity<?> changePasswordByEmail(
            @RequestParam String email,
            @RequestParam String newPassword
    ) {
        UserInfo user = repositories.findByEmail(email);
        if (user != null) {
            user.setPassword(newPassword);
            repositories.save(user);

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Password changed successfully", null)
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("failed", "User not found for email: " + email, "")
            );
        }
    }

    @DeleteMapping("/deleteAllUsers")
    public ResponseEntity<ResponseObject> deleteAllUsers() {
        try {
            repositories.deleteAll();
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "All users deleted successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject("failed", "Error while deleting all users", e.getMessage())
            );
        }
    }

    @GetMapping("/getPasswordById")
    public ResponseEntity<?> getPasswordById(@RequestParam String idUser) {
        Optional<UserInfo> userOptional = repositories.findById(idUser);
        if (userOptional.isPresent()) {
            UserInfo user = userOptional.get();
            String password = user.getPassword();
            return ResponseEntity.status(HttpStatus.OK).body(password);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("failed", "User not found for id: " + idUser, "")
            );
        }
    }

}
