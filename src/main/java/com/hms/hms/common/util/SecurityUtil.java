package com.hms.hms.common.util;

import com.hms.hms.config.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtil {

    public static UUID getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        return user.getId();
    }


    public static UUID getCurrentBranchId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        return user.getBranchId();
    }

    public static String getCurrentEmail() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        return user.getEmail();
    }
}