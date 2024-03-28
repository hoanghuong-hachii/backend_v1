package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.model.PdfFile;
import com.api19_4.api19_4.repositories.PdfFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v10/pdf")
public class PdfController {
    private static final Path CURRENT_FOLDER = Paths.get(System.getProperty("user.dir"));
    @Autowired
    private PdfFileRepository pdfFileRepository;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String fileName = timeStamp + ".pdf";

        Path staticPath = Paths.get("static");
        Path imagePath = Paths.get("images");
        if (!Files.exists(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath))) {
            Files.createDirectories(CURRENT_FOLDER.resolve(staticPath).resolve(imagePath));
        }
        Path files = CURRENT_FOLDER.resolve(staticPath).resolve(imagePath).resolve(fileName);
        try (OutputStream os = Files.newOutputStream(files)) {
            os.write(file.getBytes());
        }

        PdfFile pdfFile = new PdfFile();
        pdfFile.setFileName(fileName);
        pdfFileRepository.save(pdfFile);
        return ResponseEntity.status(200).body(fileName);
    }

    @GetMapping("/all")
    public List<PdfFile> getAllPdfFiles() {
        return pdfFileRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePdfFile(@PathVariable Long id) {
        Optional<PdfFile> optionalPdfFile = pdfFileRepository.findById(id);

        if (optionalPdfFile.isPresent()) {
            PdfFile pdfFile = optionalPdfFile.get();

            // Delete the file from the static/files directory
            Path filePath = Path.of("static/files").resolve(pdfFile.getFileName());
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                // Handle exception if needed
                return ResponseEntity.status(500).body("Error deleting file");
            }

            // Delete the record from the database
            pdfFileRepository.delete(pdfFile);

            return ResponseEntity.ok("File deleted successfully");
        } else {
            return ResponseEntity.status(404).body("File not found");
        }
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<?> deleteAllPdfFiles() {
        // Delete all files from the static/files directory
        Path filesDirectory = Path.of("static/files");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(filesDirectory)) {
            for (Path filePath : directoryStream) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Handle exception if needed
            return ResponseEntity.status(500).body("Error deleting files");
        }

        // Delete all records from the database
        pdfFileRepository.deleteAll();

        return ResponseEntity.ok("All files deleted successfully");
    }
}
