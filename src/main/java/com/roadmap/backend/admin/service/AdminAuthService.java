package com.roadmap.backend.admin.service;

import com.roadmap.backend.admin.config.JwtProvider;
import com.roadmap.backend.admin.dto.AdminLoginRequest;
import com.roadmap.backend.admin.dto.AdminLoginResponse;
import com.roadmap.backend.admin.entity.Admin;
import com.roadmap.backend.admin.exception.AdminAuthException;
import com.roadmap.backend.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final AdminRepository adminRepository;
    private final JwtProvider jwtProvider;

    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AdminAuthException("존재하지 않는 계정입니다."));

        if (!admin.getPassword().equals(request.getPassword())) {
            throw new AdminAuthException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(admin.getUsername(), ROLE_ADMIN);

        return AdminLoginResponse.builder()
                .success(true)
                .accessToken(accessToken)
                .build();
    }
}
