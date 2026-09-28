package com.militaryasset.module.base.dto;

import com.militaryasset.module.base.entity.Base;

public class BaseDTO {
    private Long id;
    private String code;
    private String name;
    private String location;

    public BaseDTO() {}

    public BaseDTO(Base base) {
        this.id = base.getId();
        this.code = base.getCode();
        this.name = base.getName();
        this.location = base.getLocation();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getLocation() { return location; }
}
