package com.codekeys.journalAction;

public record JournalActionStatistiques(
        long totalActions,
        long actionsAujourdhui,
        long totalUtilisateursDistincts,
        long totalSucces,
        long totalEchecs
) {
}
