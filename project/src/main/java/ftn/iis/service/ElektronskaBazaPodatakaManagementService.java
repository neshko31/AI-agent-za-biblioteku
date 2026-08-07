package ftn.iis.service;

import ftn.iis.dto.AzurirajElektronskuBazuDto;
import ftn.iis.dto.NovaElektronskaBazaDto;
import ftn.iis.model.ElektronskaBazaPodataka;
import ftn.iis.model.Izdavac;
import ftn.iis.repository.ElektronskaBazaPodatakaRepository;
import ftn.iis.repository.IzdavacRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ElektronskaBazaPodatakaManagementService {
    private final ElektronskaBazaPodatakaRepository bazaRepository;
    private final IzdavacRepository izdavacRepository;
    private final ElektronskaBazaFileStorageService storageService;

    public ElektronskaBazaPodatakaManagementService(ElektronskaBazaPodatakaRepository bazaRepository,
                                                    IzdavacRepository izdavacRepository,
                                                    ElektronskaBazaFileStorageService storageService) {
        this.bazaRepository = bazaRepository;
        this.izdavacRepository = izdavacRepository;
        this.storageService = storageService;
    }

    @Transactional(rollbackFor = Exception.class)
    public ElektronskaBazaPodataka kreiraj(NovaElektronskaBazaDto dto, MultipartFile zip) throws IOException {
        String zipPath = storageService.saveZip(zip);
        try {
            ElektronskaBazaPodataka baza = new ElektronskaBazaPodataka();
            baza.setNaziv(dto.getNaziv());
            baza.setOblast(dto.getOblast());
            baza.setOpis(dto.getOpis());
            baza.setLicenca(dto.getLicenca());
            baza.setPutanjaEbp(zipPath);
            baza.setIzdavac(resolveIzdavac(dto.getIzdavacId()));
            return bazaRepository.saveAndFlush(baza);
        } catch (Exception e) {
            storageService.deleteIfExists(zipPath);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ElektronskaBazaPodataka azuriraj(Long id, AzurirajElektronskuBazuDto dto, MultipartFile zip) throws IOException {
        ElektronskaBazaPodataka baza = bazaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Baza ne postoji"));

        String oldPath = null;
        String newPath = null;

        try {
            if (dto.getNaziv() != null) baza.setNaziv(dto.getNaziv());
            if (dto.getOblast() != null) baza.setOblast(dto.getOblast());
            if (dto.getOpis() != null) baza.setOpis(dto.getOpis());
            if (dto.getLicenca() != null) baza.setLicenca(dto.getLicenca());
            if (dto.getIzdavacId() != null) baza.setIzdavac(resolveIzdavac(dto.getIzdavacId()));

            if (zip != null && !zip.isEmpty()) {
                oldPath = baza.getPutanjaEbp();
                newPath = storageService.saveZip(zip);
                baza.setPutanjaEbp(newPath);
            }

            ElektronskaBazaPodataka saved = bazaRepository.saveAndFlush(baza);
            if (oldPath != null && newPath != null) {
                storageService.deleteIfExists(oldPath);
            }
            return saved;
        } catch (Exception e) {
            if (newPath != null) {
                storageService.deleteIfExists(newPath);
            }
            throw e;
        }
    }

    @Transactional
    public void obrisi(Long id) {
        ElektronskaBazaPodataka baza = bazaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Baza ne postoji"));
        String path = baza.getPutanjaEbp();
        bazaRepository.delete(baza);
        storageService.deleteIfExists(path);
    }

    private Izdavac resolveIzdavac(Long izdavacId) {
        if (izdavacId != null) {
            return izdavacRepository.findById(izdavacId)
                    .orElseThrow(() -> new IllegalArgumentException("Izdavac ne postoji"));
        }
        return izdavacRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nema dostupnog izdavaca"));
    }
}
