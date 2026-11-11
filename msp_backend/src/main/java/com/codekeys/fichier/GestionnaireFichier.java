package com.codekeys.fichier;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionnaireFichier {
    @Value("${application.file.upload}")
    private String fileUploadPath;

    public String saveFile(@NonNull String entite, @NonNull MultipartFile sourceFile, @NonNull String slug) {
        final String fileUploadSubPath = entite + "/" + slug;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    private String uploadFile(@NonNull MultipartFile sourceFile, String fileUploadSubPath) {
        Path baseDir = Path.of(fileUploadPath);
        Path targetFolder = baseDir.resolve(fileUploadSubPath);
        try {
            Files.createDirectories(targetFolder);
        } catch (IOException e) {
            log.error("Erreur survenue lors de la création du dossier {}", targetFolder);
            return null;
        }
        final String fileExtension = this.getFileExtension(sourceFile.getOriginalFilename());
        String fileName = System.currentTimeMillis() + "." + fileExtension;
        Path targetPath = targetFolder.resolve(fileName);
        try{
            Files.write(targetPath, sourceFile.getBytes());
            log.info("Successfully uploaded file {}", targetPath);
        } catch (IOException e) {
            log.error("Erreur lors de l'upload du fichier {}", targetPath, e);
        }
        return targetPath.toString();
    }

    public static void supprimerFichier(String chemin) {
        if(chemin != null) {
            try {
                Files.deleteIfExists(Path.of(chemin));
            }catch (IOException e) {
                log.error("Erreur supprimer la chemin {}", chemin);
            }
        }
    }

    private String getFileExtension(@Nullable String originalFilename) {
        if(originalFilename == null || originalFilename.isEmpty()) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if(lastDotIndex == -1) {
            return "";
        }
        return originalFilename.substring(lastDotIndex + 1).toLowerCase();
    }
}
