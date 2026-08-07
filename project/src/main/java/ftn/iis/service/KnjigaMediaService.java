package ftn.iis.service;

import ftn.iis.model.AudioKnjiga;
import ftn.iis.model.EKnjiga;
import ftn.iis.model.Knjiga;
import ftn.iis.repository.AudioKnjigaRepository;
import ftn.iis.repository.EKnjigaRepository;
import ftn.iis.repository.KnjigaRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class KnjigaMediaService {
    private final KnjigaRepository knjigaRepository;
    private final EKnjigaRepository eKnjigaRepository;
    private final AudioKnjigaRepository audioKnjigaRepository;

    public KnjigaMediaService(KnjigaRepository knjigaRepository, EKnjigaRepository eKnjigaRepository, AudioKnjigaRepository audioKnjigaRepository) {
        this.knjigaRepository = knjigaRepository;
        this.eKnjigaRepository = eKnjigaRepository;
        this.audioKnjigaRepository = audioKnjigaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Path> getNaslovnaPath(String isbn) {
        return knjigaRepository.findByIsbn(isbn)
                .filter(k -> !k.isDeleted())
                .map(Knjiga::getPutanjaNaslovna)
                .flatMap(this::resolvePath);
    }

    @Transactional(readOnly = true)
    public Optional<Path> getPdfPath(String isbn) {
        return eKnjigaRepository.findById(isbn)
                .map(EKnjiga::getPutanjaEK)
                .flatMap(this::resolvePath);
    }

    @Transactional(readOnly = true)
    public Optional<Path> getAudioPath(String isbn) {
        return audioKnjigaRepository.findById(isbn)
                .map(AudioKnjiga::getPutanjaAK)
                .flatMap(this::resolvePath);
    }

    private Optional<Path> resolvePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return Optional.empty();
        }

        // Normalizuj putanju separatore
        String normalized = rawPath.replace("\\", "/").replace("\\\\", "/");

        // Izvuci relativni deo (bez "./src/main/resources/" prefiksa)
        String classpathRelative = normalized;
        for (String prefix : new String[]{"./src/main/resources/", "src/main/resources/"}) {
            if (classpathRelative.startsWith(prefix)) {
                classpathRelative = classpathRelative.substring(prefix.length());
                break;
            }
        }

        // 1. Proba classpath (radi i u IDE i u JAR)
        try {
            ClassPathResource cpr = new ClassPathResource(classpathRelative);
            if (cpr.exists()) {
                File f = cpr.getFile();
                if (f.isFile()) return Optional.of(f.toPath());
            }
        } catch (Exception ignored) {}

        // 2. Proba apsolutnu/relativnu putanju od CWD (kao sto je u bazi: ./src/main/resources/...)
        Path direct = Paths.get(normalized).normalize().toAbsolutePath();
        if (Files.exists(direct) && Files.isRegularFile(direct)) {
            return Optional.of(direct);
        }

        // 3. Proba od src/main/resources relativno od CWD
        Path fromResources = Paths.get("src", "main", "resources", classpathRelative).normalize().toAbsolutePath();
        if (Files.exists(fromResources) && Files.isRegularFile(fromResources)) {
            return Optional.of(fromResources);
        }

        // 4. Proba od project/src/main/resources (ako se pokree iz root-a projekta)
        Path fromProjectRoot = Paths.get("project", "src", "main", "resources", classpathRelative).normalize().toAbsolutePath();
        if (Files.exists(fromProjectRoot) && Files.isRegularFile(fromProjectRoot)) {
            return Optional.of(fromProjectRoot);
        }

        return Optional.empty();
    }
}
