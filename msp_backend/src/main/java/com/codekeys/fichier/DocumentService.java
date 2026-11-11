package com.codekeys.fichier;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;

    //File location
    @Value("${application.file.upload}")
    public String DIRECTORY; // = System.getProperty("user.home") + "/Downloads/uploads/";

    //Upload file
    public Document uploadFile(MultipartFile file, TypeDocument typeDocument, String sousDossier) throws IOException {
        Path baseDir = Path.of(this.DIRECTORY);
        Path sousRepertoire = baseDir.resolve(typeDocument.name()).resolve(sousDossier);
        Files.createDirectories(sousRepertoire);

        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        Path fileStorage = sousRepertoire.resolve(filename).toAbsolutePath().normalize();
        copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
        String detectedType = Files.probeContentType(fileStorage);
        String type = detectedType != null ? detectedType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        long taille = file.getSize();
        String chemin = fileStorage.toString();
        Document document = Document.builder()
                .slug(UUID.randomUUID().toString())
                .nom(filename)
                .chemin(chemin)
                .type(type)
                .taille(taille)
                .typeDocument(typeDocument)
                .build();
        return this.documentRepository.save(document);
    }

    //Download file
    public ResponseEntity<@NonNull Resource> downloadFile(String filename) throws IOException {
        Path filePath = Path.of(DIRECTORY).resolve(filename).toAbsolutePath().normalize();
        if(!Files.exists(filePath)) {
            throw new FileNotFoundException("Aucun fichier n'a été trouvé");
        }
        Resource resource = new UrlResource(filePath.toUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", filename);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
                .headers(httpHeaders)
                .body(resource);
    }

    public byte[] readFileFromLocation(String chemin){
        if(StringUtils.isEmpty(chemin)){
            return null;
        }
        try{
            Path filePath = Path.of(chemin);
            return Files.readAllBytes(filePath);
        }catch (IOException e){
            log.error("Aucun fichier n'a été trouvé dans ce dossier {}", chemin);
        }
        return null;
    }

    public void supprimerFichier(Document document) throws IOException {
        if (document != null && StringUtils.hasText(document.getChemin())) {
            Files.deleteIfExists(Path.of(document.getChemin()));
        }
    }

    private Document rechercherDocumentParChemin(String chemin){
        return this.documentRepository.findByChemin(chemin).orElseThrow(() -> new EntityNotFoundException("Aucun fichier n'a été trouvé"));
    }

}
