package com.example.blacklist_service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlacklistService {

    @Autowired
    private BlacklistRepository blacklistRepository;

    public Blacklist addToBlacklist(BlacklistDTO blacklistDTO) {
        Blacklist blacklist = new Blacklist();
        blacklist.setDenomination(blacklistDTO.getDenomination());
        blacklist.setActivite(blacklistDTO.getActivite());
        blacklist.setStructureDemandeExclusion(blacklistDTO.getStructureDemandeExclusion());
        blacklist.setDateExclusion(blacklistDTO.getDateExclusion());
        blacklist.setMotifs(blacklistDTO.getMotifs());
        blacklist.setDureeExclusion(blacklistDTO.getDureeExclusion());
        return blacklistRepository.save(blacklist);
    }

    public List<Blacklist> getAllBlacklist() {
        return blacklistRepository.findAll();
    }

    public Blacklist getBlacklistById(Long id) {
        return blacklistRepository.findById(id).orElse(null);
    }

    public void deleteBlacklist(Long id) {
        blacklistRepository.deleteById(id);
    }
    public Blacklist updateBlacklist(Long id, BlacklistDTO dto) {
        Optional<Blacklist> optional = blacklistRepository.findById(id);
        if (optional.isPresent()) {
            Blacklist blacklist = optional.get();
            blacklist.setDenomination(dto.getDenomination());
            blacklist.setActivite(dto.getActivite());
            blacklist.setStructureDemandeExclusion(dto.getStructureDemandeExclusion());
            blacklist.setDateExclusion(dto.getDateExclusion());
            blacklist.setMotifs(dto.getMotifs());
            blacklist.setDureeExclusion(dto.getDureeExclusion());
            return blacklistRepository.save(blacklist);
        } else {
            throw new EntityNotFoundException("Blacklist non trouvée avec id: " + id);
        }
    }

}