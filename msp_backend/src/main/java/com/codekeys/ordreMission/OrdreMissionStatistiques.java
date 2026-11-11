package com.codekeys.ordreMission;

public record OrdreMissionStatistiques(
        long total,
        long enAttenteValidation,
        long approuvees,
        long refusees,
        long enCours,
        long terminees
) {
}
