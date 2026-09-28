package com.militaryasset.module.base.controller;

import com.militaryasset.common.dto.ApiResponse;
import com.militaryasset.module.base.dto.BaseDTO;
import com.militaryasset.module.base.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bases")
public class BaseController {

    @Autowired
    private BaseRepository baseRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER')")
    public ResponseEntity<ApiResponse<List<BaseDTO>>> getAllBases() {
        List<BaseDTO> bases = baseRepository.findAll().stream()
                .map(BaseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Bases retrieved successfully", bases));
    }
}
