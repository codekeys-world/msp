package com.codekeys.execption.handler;

import com.codekeys.common.ApiResponse;
import com.codekeys.execption.OperationNotPermitException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(MethodArgumentNotValidException exception){
        List<String> errors = new ArrayList<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            var errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .message("Les données saisies sont invalides ! ")
                                .developerMessage("Erreur survenue lors de la validation des données.")
                                .status(HttpStatus.BAD_REQUEST)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .erreurs(errors)
                                .build()
                );
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(BadCredentialsException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .message("Le nom d'utilisateur ou le mot de passe est incorrect.")
                                .developerMessage("Nom d'utilisateur ou le mot de passe est incorrect.")
                                .status(HttpStatus.BAD_REQUEST)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .erreur(exception.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(value = DisabledException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(DisabledException exception){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        ApiResponse.builder()
                                .message("Votre compte a été désactivé. Veuillez contacter un administrateur pour réactiver votre accès.")
                                .developerMessage("Tentative de connexion avec un compte désactivé.")
                                .status(HttpStatus.FORBIDDEN)
                                .statusCode(HttpStatus.FORBIDDEN.value())
                                .erreur("Votre compte a été désactivé.")
                                .build()
                );
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(EntityNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .message(exception.getMessage())
                                .developerMessage(exception.getMessage())
                                .status(HttpStatus.NOT_FOUND)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .erreur(exception.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(value = IOException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(IOException exception){
        log.info(exception.getStackTrace().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.builder()
                                .message("Erreur survenue lors de la manipulation du fichier.")
                                .developerMessage("Erreur survenue lors de la manipulation du fichier.")
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .erreur(exception.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(value = OperationNotPermitException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(OperationNotPermitException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                  ApiResponse.builder()
                          .message(exception.getMessage())
                          .developerMessage(exception.getMessage())
                          .status(HttpStatus.BAD_REQUEST)
                          .statusCode(HttpStatus.BAD_REQUEST.value())
                          .erreur(exception.getMessage())
                          .build()
                );
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(Exception exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.builder()
                                .message(exception.getMessage())
                                .developerMessage(exception.getMessage())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .erreur(exception.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(value = ExpiredJwtException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(ExpiredJwtException exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.builder()
                                .message("Votre session est expiré. Veuillez vous reconnecter.")
                                .developerMessage(exception.getMessage())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .erreur("Votre session est expiré. Veuillez vous reconnecter.")
                                .build()
                );
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(DataIntegrityViolationException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .message("L'entité renseigné existe déja")
                                .developerMessage("Cette entité existe déja !")
                                .status(HttpStatus.BAD_REQUEST)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .erreur("Les informations renseignées existent déja !")
                                .build()
                );
    }

    /*@ExceptionHandler(value = DuplicateKeyException.class)
    public ResponseEntity<@NonNull ApiResponse> handleException(DuplicateKeyException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .message("Une entité du même type existe déja")
                                .developerMessage("Cette entité existe déja !")
                                .status(HttpStatus.BAD_REQUEST)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .erreur("Une entité du même type existe déja")
                                .build()
                );
    }*/


}
