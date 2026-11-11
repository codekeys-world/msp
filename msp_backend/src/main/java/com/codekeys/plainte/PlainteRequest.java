package com.codekeys.plainte;

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
public class PlainteRequest {
    @NotNull(message = "La date de la plainte est obligatoire.")
    private LocalDate datePv;
    private String observation;
    private MultipartFile documentPlainte;
}
