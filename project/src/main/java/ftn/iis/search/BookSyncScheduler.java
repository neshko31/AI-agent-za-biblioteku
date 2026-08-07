package ftn.iis.search;

import ftn.iis.model.Knjiga;
import ftn.iis.repository.KnjigaRepository;
import ftn.iis.search.dto.SearchBookDto;
import ftn.iis.search.dto.SearchBookRecordDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Component
public class BookSyncScheduler {

    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

    private final KnjigaRepository knjigaRepository;
    private final SearchSyncClient searchSyncClient;
    private final BookSyncMapper mapper;
    private final boolean syncEnabled;
    private final boolean ocrEnabled;

    public BookSyncScheduler(KnjigaRepository knjigaRepository,
                              SearchSyncClient searchSyncClient,
                              BookSyncMapper mapper,
                              @Value("${search.sync.enabled:true}") boolean syncEnabled,
                              @Value("${search.sync.ocr-enabled:true}") boolean ocrEnabled) {
        this.knjigaRepository = knjigaRepository;
        this.searchSyncClient = searchSyncClient;
        this.mapper = mapper;
        this.syncEnabled = syncEnabled;
        this.ocrEnabled = ocrEnabled;
    }

    @Scheduled(cron = "${search.sync.cron}")
    public void syncAll() {
        if (!syncEnabled) return;

        List<Knjiga> sve = knjigaRepository.findAll();
        for (Knjiga knjiga : sve) {
            try {
                syncOne(knjiga);
            } catch (Exception ignored) {
            }
        }
    }

    private void syncOne(Knjiga knjiga) {
        Optional<SearchBookRecordDto> existing = searchSyncClient.findByIsbn(knjiga.getIsbn());

        if (knjiga.isDeleted()) {
            existing.ifPresent(record -> searchSyncClient.delete(record.getRecordId()));
            return;
        }

        SearchBookDto dto = mapper.toSearchDto(knjiga);
        String recordId;

        if (existing.isPresent()) {
            recordId = existing.get().getRecordId();
            searchSyncClient.update(recordId, dto);
        } else {
            Optional<SearchBookRecordDto> created = searchSyncClient.create(dto);
            if (created.isEmpty()) return;
            recordId = created.get().getRecordId();
        }

        if (!ocrEnabled || knjiga.geteKnjiga() == null || knjiga.geteKnjiga().getPutanjaEK() == null) return;

        File pdf = PROJECT_ROOT.resolve(knjiga.geteKnjiga().getPutanjaEK()).toFile();
        if (pdf.exists() && pdf.isFile()) {
            searchSyncClient.requestOcr(recordId, pdf);
        }
    }
}
