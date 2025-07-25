package com.example.blacklist_service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blacklist")
public class BlacklistController {

    @Autowired
    private BlacklistService blacklistService;
    @Autowired
    private BlacklistRepository blacklistRepository;

    @PostMapping
    public ResponseEntity<Blacklist> addBlacklist(@RequestBody BlacklistDTO blacklistDTO) {
        Blacklist blacklist = blacklistService.addToBlacklist(blacklistDTO);
        return new ResponseEntity<>(blacklist, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Blacklist> getAllBlacklist() {
        return blacklistService.getAllBlacklist();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blacklist> getBlacklistById(@PathVariable Long id) {
        Blacklist blacklist = blacklistService.getBlacklistById(id);
        if (blacklist != null) {
            return new ResponseEntity<>(blacklist, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlacklist(@PathVariable Long id) {
        blacklistService.deleteBlacklist(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Blacklist> updateBlacklist(@PathVariable Long id, @RequestBody BlacklistDTO dto) {
        try {
            Blacklist updated = blacklistService.updateBlacklist(id, dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFournisseur(@RequestParam String nomFournisseur) {
        boolean existe = blacklistRepository.existsByDenominationIgnoreCase(nomFournisseur);
        return ResponseEntity.ok(existe);
    }


}