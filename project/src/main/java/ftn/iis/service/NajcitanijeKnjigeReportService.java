package ftn.iis.service;

import ftn.iis.dto.NajcitanijaKnjigaDto;
import ftn.iis.exception.ZanrNotFoundException;
import ftn.iis.repository.GenreRepository;
import ftn.iis.repository.KnjigaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NajcitanijeKnjigeReportService {

    private final KnjigaRepository knjigaRepository;
    private final GenreRepository genreRepository;

    public NajcitanijeKnjigeReportService(KnjigaRepository knjigaRepository, GenreRepository genreRepository) {
        this.knjigaRepository = knjigaRepository;
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public List<NajcitanijaKnjigaDto> getNajcitanijeKnjigePoZanru(Long zanrId) {
        if (!genreRepository.existsById(zanrId)) {
            throw new ZanrNotFoundException(zanrId);
        }

        return knjigaRepository.findNajcitanijeKnjigePoZanru(zanrId)
                .stream()
                .map(NajcitanijaKnjigaDto::fromProjection)
                .toList();
    }
}
