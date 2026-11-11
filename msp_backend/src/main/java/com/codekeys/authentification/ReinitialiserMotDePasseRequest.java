package com.codekeys.authentification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReinitialiserMotDePasseRequest {
    @NotBlank private String token;
    @NotBlank @Size(min = 8) private String password;
    @NotBlank @Size(min = 8) private String confirmationPassword;
}
