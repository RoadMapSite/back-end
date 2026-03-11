package com.roadmap.backend.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "관리자 로그인 응답")
public class AdminLoginResponse {

    @Schema(description = "성공 여부")
    private Boolean success;

    @Schema(description = "JWT 액세스 토큰")
    private String accessToken;
}
