package com.yesgrad.service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir:uploads/profiles}")
    private String uploadDir;

    public Mono<String> saveProfilePhoto(FilePart filePart) {
        String filename = UUID.randomUUID() + "_" + filePart.filename();
        Path filePath = Paths.get(uploadDir, filename);

        return filePart.transferTo(filePath)
                .then(Mono.just("/uploads/profiles/" + filename));

    }
}
