package com.aisalescrm.dto.response;

import com.aisalescrm.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    // User info embedded — avoids a second /me call on login
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
}