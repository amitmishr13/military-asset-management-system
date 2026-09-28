package com.militaryasset.module.base.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bases")
public class Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 150)
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Base() {}

    public Base(Long id, String code, String name, String location, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.location = location;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static BaseBuilder builder() {
        return new BaseBuilder();
    }

    public static class BaseBuilder {
        private Long id;
        private String code;
        private String name;
        private String location;
        private LocalDateTime createdAt;

        public BaseBuilder id(Long id) { this.id = id; return this; }
        public BaseBuilder code(String code) { this.code = code; return this; }
        public BaseBuilder name(String name) { this.name = name; return this; }
        public BaseBuilder location(String location) { this.location = location; return this; }
        public BaseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Base build() {
            return new Base(id, code, name, location, createdAt);
        }
    }
}
