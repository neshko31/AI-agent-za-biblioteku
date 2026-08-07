package ftn.iis.service;

import ftn.iis.dto.UgovorDetaljniDto;
import ftn.iis.dto.UgovorDto;
import ftn.iis.enums.StatusDobavljaca;
import ftn.iis.enums.StatusUgovora;
import ftn.iis.exception.*;
import ftn.iis.model.Dobavljac;
import ftn.iis.model.Ugovor;
import ftn.iis.repository.DobavljacRepository;
import ftn.iis.repository.UgovorRepository;
import ftn.iis.utils.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UgovorService {
    private final UgovorRepository ugovorRepository;
    private final DobavljacRepository dobavljacRepository;
    private final JwtService jwtService;

    public UgovorService(UgovorRepository ugovorRepository, DobavljacRepository dobavljacRepository, JwtService jwtService){
        this.ugovorRepository = ugovorRepository;
        this.dobavljacRepository = dobavljacRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public UgovorDetaljniDto kreirajUgovor(String token, UgovorDto dto) {
        String role = jwtService.extractRole(token);
        if (!role.equalsIgnoreCase("MENADZER")) {
            throw new NonManagerCreatingContractException();
        }

        // 1. Proveravam da li dobavljač postoji
        Dobavljac dobavljac = dobavljacRepository.findById(dto.getDobavljacId())
                .orElseThrow(() -> new NoSupplierFound());

        // 2. Ugovor samo sa aktivnim dobavljačem
        if (dobavljac.getStatus() != StatusDobavljaca.AKTIVAN) {
            throw new SupplierNotActiveException();
        }

        // 3. Proveri da li već postoji aktivan ugovor
        if (ugovorRepository.existsByDobavljacIdAndStatus(dto.getDobavljacId(), StatusUgovora.AKTIVAN)) {
            throw new ActiveContractAlreadyExistsException();
        }

        // 4. Datum validacija
        if (dto.getDatumIsteka().isBefore(dto.getDatumPocetka())) {
            throw new InvalidContractDateException();
        }

        // 5. Proveravam popust
        if(dto.getPopust() < 0 || dto.getPopust() > 100){
            throw new InvalidContractSaleException();
        }

        // 6. Proveravam jel br dana za isporuku pozitivan
        if(dto.getRokIsporuke() < 0){
            throw new NegativeDeliveryException();
        }

        // 5. Kreiram i čuvam
        Ugovor ugovor = new Ugovor(
                dobavljac,
                dto.getPopust(),
                dto.getDatumPocetka(),
                dto.getDatumIsteka(),
                dto.getDatumPotpisa(),
                dto.getRokIsporuke()
        );
        ugovor = ugovorRepository.save(ugovor);

        return mapirajUDto(ugovor);
    }

    // Odlučila sam se da napravim posebnu pomocnu metodu nakon što sam
    // primetila kod dobavljača koliko puta sam ponavljala sledeći postupak
    private UgovorDetaljniDto mapirajUDto(Ugovor ugovor) {
        UgovorDetaljniDto dto = new UgovorDetaljniDto();
        dto.setId(ugovor.getId());
        dto.setDobavljacId(ugovor.getDobavljac().getId());
        dto.setNazivDobavljaca(ugovor.getDobavljac().getNaziv());
        dto.setPopust(ugovor.getPopust());
        dto.setDatumPocetka(ugovor.getDatumPocetka());
        dto.setDatumIsteka(ugovor.getDatumIsteka());
        dto.setDatumPotpisa(ugovor.getDatumPotpisa());
        dto.setRokIsporuke(ugovor.getRokIsporuke());
        dto.setStatus(ugovor.getStatus());
        return dto;
    }

    public List<UgovorDto> ispisiSveZaDobavljaca(String token, Long id) {
        String role = jwtService.extractRole(token);
        if (!role.equalsIgnoreCase("MENADZER")) {
            throw new NonManagerCreatingContractException();
        }

        Dobavljac dobavljac = dobavljacRepository.findById(id)
                .orElseThrow(() -> new NoSupplierFound());

        List<Ugovor> osnovni = ugovorRepository.findAllByDobavljacId(id);
        List<UgovorDto> rezultati = new ArrayList<>();

        for(Ugovor u : osnovni){
            UgovorDto ugovordto = new UgovorDto(u);
            rezultati.add(ugovordto);
        }

        return rezultati;
    }


    @Transactional
    public UgovorDetaljniDto raskiniUgovor(String token, Long id) {
        String role = jwtService.extractRole(token);
        if (!role.equalsIgnoreCase("MENADZER")) {
            throw new NonManagerCreatingContractException();
        }
        Ugovor ugovor = ugovorRepository.findById(id)
                .orElseThrow(() -> new NoContractFoundException());

        // Mogu da raskinem samo aktivan ugovor
        if (ugovor.getStatus() != StatusUgovora.AKTIVAN){
            throw new ContractNotActiveException();
        }

        ugovor.setStatus(StatusUgovora.RASKINUT);
        ugovor = ugovorRepository.save(ugovor);

        return mapirajUDto(ugovor);
    }
}
