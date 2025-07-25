package com.coderdot.entities;

public class UserStatisticsDTO {
    private String role;
    private Long count;

    public UserStatisticsDTO(String role, Long count) {
        this.role = role;
        this.count = count;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
