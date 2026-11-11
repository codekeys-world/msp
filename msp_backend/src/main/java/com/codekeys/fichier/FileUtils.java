package com.codekeys.fichier;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtils {
    public static byte[] readFileFromLocation(String location){
        if(StringUtils.isEmpty(location)){
            return null;
        }
        try{
            Path filePath = new File(location).toPath();
            return Files.readAllBytes(filePath);
        }catch (IOException e){
            log.error("Aucun fichier n'a été trouvé dans ce dossier {}", location);
        }
        return null;
    }
}
