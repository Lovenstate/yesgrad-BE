package com.yesgrad.service.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir:uploads/profiles}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public Mono<String> saveProfilePhoto(FilePart filePart) {
        String filename = UUID.randomUUID() + "_" + filePart.filename();
        Path filePath = Paths.get(uploadDir, filename);

        return filePart.transferTo(filePath)
                .then(Mono.just("/uploads/profiles/" + filename));

    }
}
