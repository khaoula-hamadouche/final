package com.example.blacklist_service;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
    Optional<Blacklist> findByDenomination(String denomination);
    List<Blacklist> findByMotifsContaining(String motif);
    boolean existsByDenominationIgnoreCase(String denomination);

}
