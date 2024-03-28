package com.api19_4.api19_4.controllerJWT;

import com.api19_4.api19_4.DTOJWT.JwtResponse;
import com.api19_4.api19_4.DTOJWT.RefreshTokenRequest;
//import com.api19_4.api19_4.entity.UserInfo;
import com.api19_4.api19_4.repositories.ProductRepository;
import com.api19_4.api19_4.serviceJWT.JwtService;

import com.api19_4.api19_4.serviceJWT.ProductServiceJWT;
import com.api19_4.api19_4.serviceJWT.RefreshTokenService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "http://127.0.0.1:5000")
@RestController
@RequestMapping("/api/v1/RefreshToken")
public class RefreshToken {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ProductRepository repository;
    @Autowired
    private ProductServiceJWT service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;




}

//    @PostMapping("/signUp")
//    public String addNewUser(@RequestBody UserDto userInfo) {
//        return service.addUser(userInfo);
//    }

//    @GetMapping("/all")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public    List<ProductDto> getAllProducts() {
//        List<com.api19_4.api19_4.models.Product> products = repository.findAll();
//
//        // Convert the list of Product entities to a list of ProductDTO objects using ModelMapper
//        List<ProductDto> productDtos = products.stream()
//                .map(product -> {
//                    ProductDto productDto = modelMapper.map(product, ProductDto.class);
//                    productDto.setImageAvatar("http://localhost:8080/" + product.getImageAvatar());
//                    productDto.setImageQR("http://localhost:8080/" + product.getImageQR());
//                    return productDto;
//                })
//                .collect(Collectors.toList());
//        return new ResponseEntity<>(productDtos, HttpStatus.OK).getBody();
//        //return productDTOs;
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('ROLE_USER')")
//    public Product getProductById(@PathVariable int id) {
//        return service.getProduct(id);
//    }
//
//
//    @PostMapping("/login")
//    public JwtResponse authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
//        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
//        if (authentication.isAuthenticated()) {
//            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequest.getUsername());
//            return JwtResponse.builder()
//                    .accessToken(jwtService.generateToken(authRequest.getUsername()))
//                    .token(refreshToken.getToken()).build();
//        } else {
//            throw new UsernameNotFoundException("invalid user request !");
//        }
//    }