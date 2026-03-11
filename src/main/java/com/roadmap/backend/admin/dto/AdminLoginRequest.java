package com.roadmap.backend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 로그인 요청")
public class AdminLoginRequest {

    @NotBlank(message = "사용자명은 필수입니다.")
    @Schema(description = "관리자 사용자명", required = true, example = "admin")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "비밀번호", required = true, example = "password")
    private String password;
}
