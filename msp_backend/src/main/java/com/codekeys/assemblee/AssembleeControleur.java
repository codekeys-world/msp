package com.codekeys.assemblee;

import com.codekeys.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/assemblees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
public class AssembleeControleur {
    private final AssembleeService service;

    @GetMapping
    public ResponseEntity<ApiResponse> lister(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size, @RequestParam(defaultValue = "") String q) {
        return reponse("Liste des assemblées chargée.", HttpStatus.OK, service.lister(page, size, q));
    }

    @GetMapping("/statistiques")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> statistiques() {
        return reponse("Statistiques des assemblées chargées.", HttpStatus.OK, Map.of("total", service.compter()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse> consulter(@PathVariable String slug) {
        return reponse("Assemblée chargée.", HttpStatus.OK, service.consulter(slug));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> creer(@RequestBody @Valid AssembleeRequest request) {
        return reponse("Assemblée enregistrée.", HttpStatus.CREATED, service.creer(request));
    }

    @PutMapping("/{slug}")
    public ResponseEntity<ApiResponse> modifier(@PathVariable String slug, @RequestBody @Valid AssembleeRequest request) {
        return reponse("Assemblée modifiée.", HttpStatus.OK, service.modifier(slug, request));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<ApiResponse> supprimer(@PathVariable String slug) {
        return reponse("Assemblée supprimée.", HttpStatus.OK, service.supprimer(slug));
    }

    private ResponseEntity<ApiResponse> reponse(String message, HttpStatus status, Object resultat) {
        return ResponseEntity.status(status).body(ApiResponse.builder().message(message).developerMessage(message)
                .status(status).statusCode(status.value()).donnees(Map.of("resultat", resultat)).build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse> paginationInvalide(IllegalArgumentException exception) {
        return reponse(exception.getMessage(), HttpStatus.BAD_REQUEST, false);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse> accesRefuse() {
        return reponse("Vous n’avez pas accès à la gestion des assemblées.", HttpStatus.FORBIDDEN, false);
    }
}
