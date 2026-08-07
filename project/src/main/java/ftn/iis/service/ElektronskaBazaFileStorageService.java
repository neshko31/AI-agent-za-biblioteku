package ftn.iis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class ElektronskaBazaFileStorageService {
    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    private static final Path BASE_DIR = PROJECT_ROOT.resolve(Paths.get("src", "main", "resources", "baze_podataka"));

    public String saveZip(MultipartFile file) throws IOException {
        String filename = buildFilename(file.getOriginalFilename());
        return saveFile(file, BASE_DIR, filename);
    }

    public void deleteIfExists(String path) {
        if (path == null || path.isBlank()) return;
        try {
            Path target = Paths.get(path);
            if (!target.isAbsolute()) {
                target = PROJECT_ROOT.resolve(target).normalize();
            }
            Files.deleteIfExists(target);
        } catch (IOException e) {
            System.err.println("Nije moguce obrisati fajl: " + path);
        }
    }

    private String buildFilename(String originalName) {
        String safeName = sanitizeFilename(originalName);
        if (safeName == null || safeName.isBlank()) {
            safeName = "baza.zip";
        }
        if (!safeName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            safeName = safeName + ".zip";
        }
        return UUID.randomUUID().toString() + "_" + safeName;
    }

    private String sanitizeFilename(String originalName) {
        if (originalName == null) {
            return null;
        }
        return Paths.get(originalName).getFileName().toString().replace(" ", "_");
    }

    private String saveFile(MultipartFile file, Path directory, String filename) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        Path target = directory.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return toRelativeProjectPath(target);
    }

    private String toRelativeProjectPath(Path target) {
        try {
            Path normalized = target.toAbsolutePath().normalize();
            Path relative = PROJECT_ROOT.relativize(normalized);
            String unixStyle = relative.toString().replace("\\", "/");
            return "./" + unixStyle;
        } catch (IllegalArgumentException e) {
            return target.toAbsolutePath().toString();
        }
    }
}
