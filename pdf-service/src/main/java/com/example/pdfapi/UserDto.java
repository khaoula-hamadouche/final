package com.example.pdfapi;

import java.util.List;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private List<RoleDto> roles;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<RoleDto> getRoles() { return roles; }
    public void setRoles(List<RoleDto> roles) { this.roles = roles; }
}
