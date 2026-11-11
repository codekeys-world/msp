package com.codekeys.courrier;

public record CourrierStatistiques(
        long total,
        long courriersArrives,
        long courriersDepart,
        long enAttente,
        long traites,
        long refuses
) {
}
