package com.codekeys.assemblee;

import java.time.LocalDate;

public record AssembleeResponse(String slug, String nom, LocalDate date, String lieu) {
    static AssembleeResponse from(Assemblee assemblee) {
        return new AssembleeResponse(assemblee.getSlug(), assemblee.getNom(), assemblee.getDate(), assemblee.getLieu());
    }
}
