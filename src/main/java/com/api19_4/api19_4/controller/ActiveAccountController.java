package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.model.ActiveAccount;
import com.api19_4.api19_4.models.Salt;
import com.api19_4.api19_4.services.ActiveAccountService;
import com.api19_4.api19_4.services.SaltService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/active")
public class ActiveAccountController {

    @Autowired
    private ActiveAccountService activeAccountService;

    @GetMapping("/{email}")
    public ResponseEntity<ActiveAccount> getActiveAccountByEmail(@PathVariable String email) {
        ActiveAccount activeAccount = activeAccountService.findByEmail(email);
        if (activeAccount != null) {
            return new ResponseEntity<>(activeAccount, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<ActiveAccount> createActiveAccount(@RequestBody ActiveAccount activeAccount) {
        ActiveAccount savedActiveAccount = activeAccountService.saveSalt(activeAccount);
        System.out.println("Tạo active account ok");
        return new ResponseEntity<>(savedActiveAccount, HttpStatus.CREATED);
    }

    @PutMapping("/{email}")
    public ResponseEntity<ActiveAccount> updateActiveStatus(@PathVariable String email, @RequestParam boolean active) {
        ActiveAccount existingActiveAccount = activeAccountService.findByEmail(email);
        if (existingActiveAccount != null) {
            existingActiveAccount.setActive(active);
            ActiveAccount updatedActiveAccount = activeAccountService.saveSalt(existingActiveAccount);
            return new ResponseEntity<>(updatedActiveAccount, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
