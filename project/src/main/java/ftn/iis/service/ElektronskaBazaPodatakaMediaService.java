package ftn.iis.service;

import ftn.iis.model.ElektronskaBazaPodataka;
import ftn.iis.repository.ElektronskaBazaPodatakaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class ElektronskaBazaPodatakaMediaService {
    private final ElektronskaBazaPodatakaRepository bazaRepository;

    public ElektronskaBazaPodatakaMediaService(ElektronskaBazaPodatakaRepository bazaRepository) {
        this.bazaRepository = bazaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Path> getZipPath(Long id) {
        return bazaRepository.findById(id)
                .map(ElektronskaBazaPodataka::getPutanjaEbp)
                .flatMap(this::resolvePath);
    }

    private Optional<Path> resolvePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return Optional.empty();
        }

        Path path = Paths.get(rawPath).normalize().toAbsolutePath();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }

        return Optional.of(path);
    }
}
