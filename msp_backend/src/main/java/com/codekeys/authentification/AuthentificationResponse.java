package com.codekeys.authentification;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthentificationResponse {
    private String token;
    private String refreshToken;
}
