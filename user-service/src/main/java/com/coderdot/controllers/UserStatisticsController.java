package com.coderdot.controllers;

import com.coderdot.entities.UserStatisticsDTO;
import com.coderdot.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class UserStatisticsController {

    private final UserRepository userRepository;

    public UserStatisticsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users-by-role")
    public ResponseEntity<List<UserStatisticsDTO>> getUserCountByRole() {
        List<UserStatisticsDTO> stats = userRepository.countUsersByRole();
        return ResponseEntity.ok(stats);
    }
}
