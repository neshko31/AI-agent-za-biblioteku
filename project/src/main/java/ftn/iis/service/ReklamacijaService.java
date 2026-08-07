package ftn.iis.service;

import ftn.iis.dto.KreirajReklamacijuDto;
import ftn.iis.dto.ReklamacijaResponseDto;
import ftn.iis.dto.ZatvoriReklamacijuDto;
import ftn.iis.enums.StatusNarudzbine;
import ftn.iis.enums.StatusReklamacije;
import ftn.iis.model.Narudzbina;
import ftn.iis.model.Reklamacija;
import ftn.iis.repository.NarudzbinaRepository;
import ftn.iis.repository.ReklamacijaRepository;
import ftn.iis.utils.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReklamacijaService {

    private final ReklamacijaRepository reklamacijaRepository;
    private final NarudzbinaRepository narudzbinaRepository;
    private final JwtService jwtService;

    public ReklamacijaService(ReklamacijaRepository reklamacijaRepository,
                              NarudzbinaRepository narudzbinaRepository,
                              JwtService jwtService) {
        this.reklamacijaRepository = reklamacijaRepository;
        this.narudzbinaRepository = narudzbinaRepository;
        this.jwtService = jwtService;
    }

    // Kreiranje reklamacije
    @Transactional
    public ReklamacijaResponseDto kreirajReklamaciju(String token, Long narudzbinId,
                                                     KreirajReklamacijuDto dto) {
        proveriMenadzera(token);

        Narudzbina narudzbina = narudzbinaRepository.findById(narudzbinId)
                .orElseThrow(() -> new RuntimeException("Narudžbina nije pronađena."));

        if (narudzbina.getStatus() != StatusNarudzbine.ISPORUCENA) {
            throw new RuntimeException("Reklamacija se može kreirati samo za isporučene narudžbine.");
        }

        boolean vecPostoji = reklamacijaRepository.findByNarudzbinaId(narudzbinId).isPresent();
        if (vecPostoji) {
            throw new RuntimeException("Za ovu narudžbinu već postoji reklamacija.");
        }

        Reklamacija reklamacija = new Reklamacija(narudzbina, dto.getRazlog(), dto.getNapomena());
        reklamacijaRepository.save(reklamacija);

        narudzbina.setStatus(StatusNarudzbine.REKLAMIRANA);
        narudzbinaRepository.save(narudzbina);

        return mapirajUDto(reklamacija);
    }

    // Pregled svih reklamacija
    @Transactional
    public List<ReklamacijaResponseDto> getSveReklamacije(String token) {
        proveriMenadzera(token);

        List<Reklamacija> reklamacije = reklamacijaRepository.findAllByOrderByDatumPodnosenjaDesc();
        List<ReklamacijaResponseDto> dtos = new ArrayList<>();
        for (Reklamacija r : reklamacije) {
            dtos.add(mapirajUDto(r));
        }
        return dtos;
    }

    // Zatvaranje reklamacije
    @Transactional
    public ReklamacijaResponseDto zatvoriReklamaciju(String token, Long id,
                                                     ZatvoriReklamacijuDto dto) {
        proveriMenadzera(token);

        Reklamacija reklamacija = reklamacijaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reklamacija nije pronađena."));

        if (reklamacija.getStatus() != StatusReklamacije.OTVORENA) {
            throw new RuntimeException("Reklamacija je već zatvorena.");
        }

        StatusReklamacije noviStatus = StatusReklamacije.valueOf(dto.getStatus().toUpperCase());
        reklamacija.setStatus(noviStatus);
        reklamacija.setDatumZatvaranja(LocalDate.now());
        reklamacijaRepository.save(reklamacija);

        return mapirajUDto(reklamacija);
    }

    private void proveriMenadzera(String token) {
        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("MENADZER")) {
            throw new RuntimeException("Samo menadžer može upravljati reklamacijama.");
        }
    }

    private ReklamacijaResponseDto mapirajUDto(Reklamacija r) {
        ReklamacijaResponseDto dto = new ReklamacijaResponseDto();
        dto.setId(r.getId());
        dto.setNarudzbinId(r.getNarudzbina().getId());
        dto.setDobavljacNaziv(r.getNarudzbina().getDobavljac().getNaziv());
        dto.setDatumPodnosenja(r.getDatumPodnosenja());
        dto.setRazlog(r.getRazlog());
        dto.setNapomena(r.getNapomena());
        dto.setStatus(r.getStatus());
        dto.setDatumZatvaranja(r.getDatumZatvaranja());
        return dto;
    }
}
