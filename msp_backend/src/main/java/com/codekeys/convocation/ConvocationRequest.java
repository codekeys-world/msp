package com.codekeys.convocation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConvocationRequest {
    @NotBlank(message = "La description de la convocation est obligatoire.")
    private String description;
    @NotNull(message = "La date de la convocation est obligatoire.")
    private LocalDate dateConvocation;
    private MultipartFile documentConvocation;
}
