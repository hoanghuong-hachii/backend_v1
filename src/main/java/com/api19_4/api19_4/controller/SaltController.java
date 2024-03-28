package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.models.Salt;
import com.api19_4.api19_4.services.SaltService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salts")
public class SaltController {

    @Autowired
    private SaltService saltService;

    @GetMapping("/{email}")
    public ResponseEntity<Salt> getSaltByEmail(@PathVariable String email) {
        Salt salt = saltService.findByEmail(email);
        if (salt != null) {
            return new ResponseEntity<>(salt, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Salt> createSalt(@RequestBody Salt salt) {
        Salt savedSalt = saltService.saveSalt(salt);
        System.out.println("Tạo salt ok");
        return new ResponseEntity<>(savedSalt, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllSalts() {
        saltService.deleteAllSalts();
        return new ResponseEntity<>("All salts deleted successfully", HttpStatus.OK);
    }
}
