package com.api19_4.api19_4.services;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface IStorageService {
    public String storeFile(MultipartFile file);
    public Stream<Path> loadAll(); // load all file inside a folder
    public byte[] readFileContent(String fileName);
    public void deleteAllFiles();
}
