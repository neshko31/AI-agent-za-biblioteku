package ftn.iis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class KnjigaFileStorageService {
    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    private static final Path BASE_DIR = PROJECT_ROOT.resolve(Paths.get("src", "main", "resources", "knjige"));
    private static final Path NASLOVNE_DIR = BASE_DIR.resolve("naslovne");
    private static final Path EKNJIGE_DIR = BASE_DIR.resolve("eknjige");
    private static final Path AUDIOKNJIGE_DIR = BASE_DIR.resolve("audioknjige");

    public String saveNaslovna(String isbn, MultipartFile file) throws IOException {
        String ext = getExtension(file.getOriginalFilename());
        return saveFile(file, NASLOVNE_DIR, isbn + "_naslovna." + ext);
    }

    public String savePdf(String isbn, MultipartFile file) throws IOException {
        return saveFile(file, EKNJIGE_DIR, isbn + ".pdf");
    }

    public String saveMp3(String isbn, MultipartFile file) throws IOException {
        return saveFile(file, AUDIOKNJIGE_DIR, isbn + ".mp3");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public void deleteIfExists(String path) {
        if (path == null) return;
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
