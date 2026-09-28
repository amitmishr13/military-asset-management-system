package com.militaryasset.security;

import com.militaryasset.module.auth.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("baseSecurityService")
public class BaseSecurityService {

    public boolean isAccessAllowedForBase(Long requestedBaseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return false;
        }

        Role role = principal.getRole();

        // Admin has unrestricted access to all bases
        if (role == Role.ADMIN) {
            return true;
        }

        // Logistics Officer has cross-base visibility for purchases & transfers
        if (role == Role.LOGISTICS_OFFICER) {
            return true;
        }

        // Base Commander is strictly restricted to their assigned baseId
        if (role == Role.BASE_COMMANDER) {
            if (requestedBaseId == null || principal.getBaseId() == null) {
                return false;
            }
            return requestedBaseId.equals(principal.getBaseId());
        }

        return false;
    }

    public boolean isTransferAllowed(Long sourceBaseId, Long destinationBaseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return false;
        }

        Role role = principal.getRole();

        if (role == Role.ADMIN || role == Role.LOGISTICS_OFFICER) {
            return true;
        }

        // Base Commander can only initiate a transfer if their assigned base is the SOURCE base
        if (role == Role.BASE_COMMANDER) {
            return sourceBaseId != null && sourceBaseId.equals(principal.getBaseId());
        }

        return false;
    }
}
