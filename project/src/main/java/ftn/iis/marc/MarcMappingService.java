package ftn.iis.marc;

import ftn.iis.marc.dto.MarcFieldDto;
import ftn.iis.marc.dto.MarcRecordDto;
import ftn.iis.model.Knjiga;
import ftn.iis.repository.KnjigaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarcMappingService {

    private final KnjigaRepository knjigaRepository;

    public MarcMappingService(KnjigaRepository knjigaRepository) {
        this.knjigaRepository = knjigaRepository;
    }

    public Optional<MarcRecordDto> buildRecord(String isbn) {
        return knjigaRepository.findByIsbn(isbn)
                .filter(knjiga -> !knjiga.isDeleted())
                .map(this::toMarc);
    }

    private MarcRecordDto toMarc(Knjiga knjiga) {
        List<MarcFieldDto> fields = new ArrayList<>();

        fields.add(new MarcFieldDto("020", Map.of("a", knjiga.getIsbn())));

        if (knjiga.getAutor() != null && !knjiga.getAutor().isBlank()) {
            fields.add(new MarcFieldDto("100", Map.of("a", knjiga.getAutor())));
        }

        fields.add(new MarcFieldDto("245", Map.of("a", knjiga.getNaslov())));

        if (knjiga.getSinopsis() != null && !knjiga.getSinopsis().isBlank()) {
            fields.add(new MarcFieldDto("520", Map.of("a", knjiga.getSinopsis())));
        }

        if (knjiga.geteKnjiga() != null && knjiga.geteKnjiga().getBrojStranaEK() != null) {
            fields.add(new MarcFieldDto("300", Map.of("a", knjiga.geteKnjiga().getBrojStranaEK() + " str.")));
        }

        if (knjiga.getKatalog() != null && knjiga.getKatalog().getStandard() != null
                && !knjiga.getKatalog().getStandard().isBlank()) {
            fields.add(new MarcFieldDto("040", Map.of("e", knjiga.getKatalog().getStandard())));
        }

        MarcRecordDto record = new MarcRecordDto();
        record.setLeader(buildLeader(knjiga));
        record.setFields(fields);
        return record;
    }

    private String buildLeader(Knjiga knjiga) {
        char typeOfRecord = knjiga.geteKnjiga() != null ? 'm' : 'a';
        StringBuilder leader = new StringBuilder("_".repeat(24));
        leader.setCharAt(5, 'n');
        leader.setCharAt(6, typeOfRecord);
        leader.setCharAt(7, 'm');
        leader.replace(10, 12, "22");
        leader.replace(20, 24, "4500");
        return leader.toString();
    }
}
