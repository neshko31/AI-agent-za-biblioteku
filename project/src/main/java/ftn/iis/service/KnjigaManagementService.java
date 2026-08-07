package ftn.iis.service;

import ftn.iis.dto.NewBookWithMediaDto;
import ftn.iis.dto.UpdateBookWithMediaDto;
import ftn.iis.model.AudioKnjiga;
import ftn.iis.model.EKnjiga;
import ftn.iis.model.Knjiga;
import ftn.iis.repository.AudioKnjigaRepository;
import ftn.iis.repository.EKnjigaRepository;
import ftn.iis.repository.KnjigaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class KnjigaManagementService {

    private final KnjigaRepository knjigaRepository;
    private final EKnjigaRepository eKnjigaRepository;
    private final AudioKnjigaRepository audioKnjigaRepository;
    private final KatalogService katalogService;
    private final KnjigaFileStorageService fileStorageService;

    public KnjigaManagementService(KnjigaRepository knjigaRepository,
                                   EKnjigaRepository eKnjigaRepository,
                                   AudioKnjigaRepository audioKnjigaRepository,
                                   KatalogService katalogService,
                                   KnjigaFileStorageService fileStorageService) {
        this.knjigaRepository = knjigaRepository;
        this.eKnjigaRepository = eKnjigaRepository;
        this.audioKnjigaRepository = audioKnjigaRepository;
        this.katalogService = katalogService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(rollbackFor = Exception.class)
    public Knjiga createBookWithMedia(NewBookWithMediaDto dto,
                                      MultipartFile naslovna,
                                      MultipartFile pdf,
                                      MultipartFile mp3) throws IOException {
        if (knjigaRepository.findByIsbn(dto.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Knjiga sa ISBN " + dto.getIsbn() + " vec postoji");
        }

        List<String> createdFiles = new ArrayList<>();

        try {
            // 1. Kreiraj osnovnu knjigu (bez medija prvo)
            Knjiga knjiga = new Knjiga();
            knjiga.setIsbn(dto.getIsbn());
            knjiga.setNaslov(dto.getNaslov());
            knjiga.setAutor(dto.getAutor());
            knjiga.setSinopsis(dto.getSinopsis());
            knjiga.setKatalog(katalogService.getByKatId(dto.getKatId())
                    .orElseThrow(() -> new IllegalArgumentException("Katalog ne postoji")));
            knjiga.setDeleted(false);

            // 2. Čuvanje naslovne strane (ako postoji)
            if (naslovna != null && !naslovna.isEmpty()) {
                String path = fileStorageService.saveNaslovna(dto.getIsbn(), naslovna);
                createdFiles.add(path);
                knjiga.setPutanjaNaslovna(path);
            }

            // 3. Kreiranje EKnjiga (ako postoji PDF)
            if (pdf != null && !pdf.isEmpty()) {
                String path = fileStorageService.savePdf(dto.getIsbn(), pdf);
                createdFiles.add(path);

                EKnjiga ek = new EKnjiga();
                // KRITIČNO: @MapsId zahteva da ID bude eksplicitno postavljen
                ek.setIsbn(dto.getIsbn());
                ek.setKnjiga(knjiga);
                ek.setPutanjaEK(path);
                ek.setFormatEK("pdf");
                ek.setDatumDodavanjaEK(LocalDate.now());
                if (dto.getBrojStranaEK() != null) ek.setBrojStranaEK(dto.getBrojStranaEK());

                knjiga.seteKnjiga(ek);
            }

            // 4. Kreiranje AudioKnjiga (ako postoji MP3)
            if (mp3 != null && !mp3.isEmpty()) {
                String path = fileStorageService.saveMp3(dto.getIsbn(), mp3);
                createdFiles.add(path);

                AudioKnjiga ak = new AudioKnjiga();
                // KRITIČNO: @MapsId zahteva da ID bude eksplicitno postavljen
                ak.setIsbn(dto.getIsbn());
                ak.setKnjiga(knjiga);
                ak.setPutanjaAK(path);
                ak.setFormatAK("mp3");
                ak.setDatumDodavanjaAK(LocalDate.now());
                if (dto.getTrajanjeSekundeAK() != null) ak.setTrajanjeSekundeAK(dto.getTrajanjeSekundeAK());

                knjiga.setAudioKnjiga(ak);
            }

            // 5. Sačuvaj knjigu — cascade će sačuvati EKnjiga i AudioKnjiga
            // ali SAMO ako imaju postavljen isbn i knjigu
            return knjigaRepository.saveAndFlush(knjiga);

        } catch (Exception e) {
            cleanupFiles(createdFiles);
            throw e;
        }
    }

    public Knjiga deleteEBook(String isbn) {
        Knjiga knjiga = knjigaRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Knjiga ne postoji"));

        if (knjiga.isDeleted()) {
            throw new IllegalArgumentException("Knjiga je logicki obrisana");
        }

        EKnjiga eKnjiga = knjiga.geteKnjiga();
        if (eKnjiga == null) {
            throw new IllegalArgumentException("Knjiga nema e-knjigu");
        }

        knjiga.seteKnjiga(null);
        eKnjigaRepository.delete(eKnjiga);
        knjiga.izracunajTipKnjige();

        return knjigaRepository.saveAndFlush(knjiga);
    }

    public Knjiga deleteAudioBook(String isbn) {
        Knjiga knjiga = knjigaRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Knjiga ne postoji"));

        if (knjiga.isDeleted()) {
            throw new IllegalArgumentException("Knjiga je logicki obrisana");
        }

        AudioKnjiga audioKnjiga = knjiga.getAudioKnjiga();
        if (audioKnjiga == null) {
            throw new IllegalArgumentException("Knjiga nema audio knjigu");
        }

        knjiga.setAudioKnjiga(null);
        audioKnjigaRepository.delete(audioKnjiga);
        knjiga.izracunajTipKnjige();

        return knjigaRepository.saveAndFlush(knjiga);
    }

    @Transactional(rollbackFor = Exception.class)
    public Knjiga updateBookWithMedia(String isbn,
                                      UpdateBookWithMediaDto dto,
                                      MultipartFile naslovna,
                                      MultipartFile pdf,
                                      MultipartFile mp3) throws IOException {
        Knjiga knjiga = knjigaRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Knjiga ne postoji"));

        if (knjiga.isDeleted()) {
            throw new IllegalArgumentException("Knjiga je logicki obrisana");
        }

        List<String> createdFiles = new ArrayList<>();
        List<String> oldFilesToDelete = new ArrayList<>();

        try {
            // Ažuriranje osnovnih polja
            if (dto.getNaslov() != null) knjiga.setNaslov(dto.getNaslov());
            if (dto.getAutor() != null) knjiga.setAutor(dto.getAutor());
            if (dto.getSinopsis() != null) knjiga.setSinopsis(dto.getSinopsis());
            if (dto.getKatId() != null) {
                knjiga.setKatalog(katalogService.getByKatId(dto.getKatId())
                        .orElseThrow(() -> new IllegalArgumentException("Katalog ne postoji")));
            }

            // Ažuriranje metapodataka postojećih medija (bez zamene fajla)
            if (dto.getBrojStranaEK() != null && knjiga.geteKnjiga() != null) {
                knjiga.geteKnjiga().setBrojStranaEK(dto.getBrojStranaEK());
            }
            if (dto.getTrajanjeSekundeAK() != null && knjiga.getAudioKnjiga() != null) {
                knjiga.getAudioKnjiga().setTrajanjeSekundeAK(dto.getTrajanjeSekundeAK());
            }

            // Zamena / dodavanje naslovne
            if (naslovna != null && !naslovna.isEmpty()) {
                if (knjiga.getPutanjaNaslovna() != null) {
                    oldFilesToDelete.add(knjiga.getPutanjaNaslovna());
                }
                String path = fileStorageService.saveNaslovna(isbn, naslovna);
                createdFiles.add(path);
                knjiga.setPutanjaNaslovna(path);
            }

            // Zamena / dodavanje PDF-a
            if (pdf != null && !pdf.isEmpty()) {
                EKnjiga ek = knjiga.geteKnjiga();
                if (ek == null) {
                    ek = new EKnjiga();
                    ek.setIsbn(isbn);  // KRITIČNO za @MapsId
                    ek.setKnjiga(knjiga);
                    knjiga.seteKnjiga(ek);
                } else if (ek.getPutanjaEK() != null) {
                    oldFilesToDelete.add(ek.getPutanjaEK());
                }
                String path = fileStorageService.savePdf(isbn, pdf);
                createdFiles.add(path);
                ek.setPutanjaEK(path);
                ek.setFormatEK("pdf");
                ek.setDatumDodavanjaEK(LocalDate.now());
                if (dto.getBrojStranaEK() != null) ek.setBrojStranaEK(dto.getBrojStranaEK());
            }

            // Zamena / dodavanje MP3-a
            if (mp3 != null && !mp3.isEmpty()) {
                AudioKnjiga ak = knjiga.getAudioKnjiga();
                if (ak == null) {
                    ak = new AudioKnjiga();
                    ak.setIsbn(isbn);  // KRITIČNO za @MapsId
                    ak.setKnjiga(knjiga);
                    knjiga.setAudioKnjiga(ak);
                } else if (ak.getPutanjaAK() != null) {
                    oldFilesToDelete.add(ak.getPutanjaAK());
                }
                String path = fileStorageService.saveMp3(isbn, mp3);
                createdFiles.add(path);
                ak.setPutanjaAK(path);
                ak.setFormatAK("mp3");
                ak.setDatumDodavanjaAK(LocalDate.now());
                if (dto.getTrajanjeSekundeAK() != null) ak.setTrajanjeSekundeAK(dto.getTrajanjeSekundeAK());
            }

            // Ručno izračunaj tip knjige
            knjiga.izracunajTipKnjige();

            // Sačuvaj — cascade će obraditi nove veze
            Knjiga saved = knjigaRepository.saveAndFlush(knjiga);

            // Briši stare fajlove tek nakon uspešnog čuvanja
            for (String oldPath : oldFilesToDelete) {
                fileStorageService.deleteIfExists(oldPath);
            }

            return saved;
        } catch (Exception e) {
            cleanupFiles(createdFiles);
            throw e;
        }
    }

    private void cleanupFiles(List<String> paths) {
        for (String path : paths) {
            fileStorageService.deleteIfExists(path);
        }
    }
}