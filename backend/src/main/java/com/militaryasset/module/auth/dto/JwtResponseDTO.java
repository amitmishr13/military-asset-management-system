package com.militaryasset.module.auth.dto;

import com.militaryasset.module.auth.entity.Role;

public class JwtResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private Long baseId;
    private String baseName;
    private String baseCode;

    public JwtResponseDTO() {}

    public JwtResponseDTO(String token, Long id, String username, String fullName, Role role, Long baseId, String baseName, String baseCode) {
        this.token = token;
        this.tokenType = "Bearer";
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.baseId = baseId;
        this.baseName = baseName;
        this.baseCode = baseCode;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getBaseId() { return baseId; }
    public void setBaseId(Long baseId) { this.baseId = baseId; }

    public String getBaseName() { return baseName; }
    public void setBaseName(String baseName) { this.baseName = baseName; }

    public String getBaseCode() { return baseCode; }
    public void setBaseCode(String baseCode) { this.baseCode = baseCode; }
}
