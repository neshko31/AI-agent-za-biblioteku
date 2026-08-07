package ftn.iis.service;

import ftn.iis.dto.BudzetPoZanruResponseDto;
import ftn.iis.dto.PostaviBudzetDto;
import ftn.iis.dto.PreraspodelaBudzetaDto;
import ftn.iis.model.Budzet;
import ftn.iis.model.BudzetPoZanru;
import ftn.iis.model.Genre;
import ftn.iis.repository.BudzetPoZanruRepository;
import ftn.iis.repository.BudzetRepository;
import ftn.iis.repository.GenreRepository;
import ftn.iis.utils.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BudzetService {

    private final BudzetPoZanruRepository budzetPoZanruRepository;
    private final GenreRepository zanrRepository;
    private final JwtService jwtService;
    private final BudzetRepository budzetRepository;

    public BudzetService(BudzetPoZanruRepository budzetPoZanruRepository, GenreRepository zanrRepository, JwtService jwtService,
                         BudzetRepository budzetRepository) {
        this.budzetPoZanruRepository = budzetPoZanruRepository;
        this.zanrRepository = zanrRepository;
        this.jwtService = jwtService;
        this.budzetRepository = budzetRepository ;
    }

    @Transactional
    public void rezervisi(Long zanrId, Double iznos) {
        BudzetPoZanru budzet = budzetPoZanruRepository.findByZanrId(zanrId)
                .orElseThrow(() -> new RuntimeException("Budžet za žanr nije pronađen."));

        if (budzet.getDostupno() < iznos) {
            throw new RuntimeException("Nedovoljno dostupnih sredstava za rezervaciju.");
        }

        budzet.setRezervisano(budzet.getRezervisano() + iznos);
        budzetPoZanruRepository.save(budzet);
    }

    @Transactional
    public void oslobodiRezervaciju(Long zanrId, Double iznos) {
        BudzetPoZanru budzet = budzetPoZanruRepository.findByZanrId(zanrId)
                .orElseThrow(() -> new RuntimeException("Budžet za žanr nije pronađen."));

        double novoRezervisano = Math.max(0, budzet.getRezervisano() - iznos);
        budzet.setRezervisano(novoRezervisano);
        budzetPoZanruRepository.save(budzet);
    }

    public List<BudzetPoZanruResponseDto> getSviBudzetiPoZanrovima(String token) {
        proveriMenadzera(token);

        List<BudzetPoZanru> budzetiPoZanru = budzetPoZanruRepository.findAllByOrderByZanrNameAsc();
        List<BudzetPoZanruResponseDto> dtos = new ArrayList<>();
        for (BudzetPoZanru b : budzetiPoZanru) {
            BudzetPoZanruResponseDto dto = mapirajUDto(b);
            dtos.add(dto);
        }
        return dtos;
    }

    // Postavljanje ili ažuriranje budžeta za neki žanr
    @Transactional
    public BudzetPoZanruResponseDto postaviBudzet(String token, PostaviBudzetDto dto) {
        proveriMenadzera(token);

        // proveravam da li žanr postoji
        Genre zanr = zanrRepository.findById(dto.getZanrId()).
                orElseThrow(() -> new RuntimeException("Žanr nije pronađen."));

        // proveravam da li "krovni" budzet posotji
        Budzet krovniBudzet = budzetRepository.findById(dto.getBudzetId())
                .orElseThrow(() -> new RuntimeException("Krovni budžet nije pronađen."));

        // proveravam da li već postoji budžet za taj žanr, ako ne postoji kreiram novi
        BudzetPoZanru budzetPoZanru = budzetPoZanruRepository.findByZanrId(dto.getZanrId()).
                orElse(new BudzetPoZanru(zanr, 0.0, krovniBudzet));

        // Ne može se smanjiti ispod već potrošenog
        if (dto.getUkupanBudzet() < budzetPoZanru.getPotroseno()) {
            throw new RuntimeException("Ne možete postaviti budžet ispod već potrošenog iznosa ("
                    + budzetPoZanru.getPotroseno() + " RSD).");
        }

        // Suma svih ostalih budžeta po žanru (isključujući trenutni žanr) ne sme prevazići krovni budžet
        Double sumaOstalihBudzeta = 0.0;
        List<BudzetPoZanru> sviBudzeti = budzetPoZanruRepository.findAllByOrderByZanrNameAsc();

        for (BudzetPoZanru b : sviBudzeti) {
            if (!b.getZanr().getId().equals(dto.getZanrId())) {
                sumaOstalihBudzeta += b.getUkupanBudzet();
            }
        }

        if (sumaOstalihBudzeta + dto.getUkupanBudzet() > krovniBudzet.getUkupanIznos()) {
            double dostupno = krovniBudzet.getUkupanIznos() - sumaOstalihBudzeta;
            throw new RuntimeException(
                    "Prekoračen krovni budžet! Za ovaj žanr možete dodeliti najviše "
                            + dostupno + " RSD.");
        }

        budzetPoZanru.setUkupanBudzet(dto.getUkupanBudzet());
        budzetPoZanruRepository.save(budzetPoZanru);
        return mapirajUDto(budzetPoZanru);
    }

    // Rebudzetiranje ~~~
    @Transactional
    public List<BudzetPoZanruResponseDto> prerasporedi(String token, PreraspodelaBudzetaDto dto) {
        proveriMenadzera(token);

        if (dto.getIzvorZanrId().equals(dto.getOdredisteZanrId())) {
            throw new RuntimeException("Izvorni i odredišni žanr ne mogu biti isti.");
        }

        BudzetPoZanru izvor = budzetPoZanruRepository.findByZanrId(dto.getIzvorZanrId())
                .orElseThrow(() -> new RuntimeException("Izvorni žanr nema definisan budžet."));

        BudzetPoZanru odrediste = budzetPoZanruRepository.findByZanrId(dto.getOdredisteZanrId())
                .orElseThrow(() -> new RuntimeException("Odredišni žanr nema definisan budžet."));

        // Proveri da li izvorni žanr ima dovoljno DOSTUPNIH sredstava
        if (izvor.getDostupno() < dto.getIznos()) {
            throw new RuntimeException("Nedovoljno dostupnih sredstava u izvornom žanru. "
                    + "Dostupno: " + izvor.getDostupno() + " RSD.");
        }

        // Ako je sve okk >
        izvor.setUkupanBudzet(izvor.getUkupanBudzet() - dto.getIznos());
        odrediste.setUkupanBudzet(odrediste.getUkupanBudzet() + dto.getIznos());

        budzetPoZanruRepository.save(izvor);
        budzetPoZanruRepository.save(odrediste);

        // Samo vracam azurirane DTO
        List<BudzetPoZanruResponseDto> rezultat = new ArrayList<>();
        rezultat.add(mapirajUDto(izvor));
        rezultat.add(mapirajUDto(odrediste));
        return rezultat;
    }

    // Trošenje budzeta
    @Transactional
    public void potrosi(Long zanrId, Double iznos) {
        BudzetPoZanru budzet = budzetPoZanruRepository.findByZanrId(zanrId)
                .orElseThrow(() -> new RuntimeException("Budžet za žanr nije pronađen."));

        // Oslobodi rezervaciju i dodaj u potrošeno
        double novoRezervisano = Math.max(0, budzet.getRezervisano() - iznos);
        budzet.setRezervisano(novoRezervisano);

        // Provera da li se prevazilazi budzet samog zanra
        // Jer menadzer sad na frontu moze da izabere 10000 komada knjiga ako zeli :*
        if(budzet.getPotroseno() + iznos > budzet.getUkupanBudzet()){
            throw new RuntimeException("Prevazilazite budžet za žanr " + budzet.getZanr().getName() + " .");
        }

        budzet.setPotroseno(budzet.getPotroseno() + iznos);
        budzetPoZanruRepository.save(budzet);
    }


    // Pomocne funkcijice ~~~~
    private void proveriMenadzera(String token) {
        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("MENADZER")) {
            throw new RuntimeException("Samo menadžer može upravljati budžetom.");
        }
    }

    private BudzetPoZanruResponseDto mapirajUDto(BudzetPoZanru b) {
        BudzetPoZanruResponseDto dto = new BudzetPoZanruResponseDto();
        dto.setId(b.getId());
        dto.setZanrId(b.getZanr().getId());
        dto.setZanrNaziv(b.getZanr().getName());
        dto.setUkupanBudzet(b.getUkupanBudzet());
        dto.setPotroseno(b.getPotroseno());
        dto.setDostupno(b.getDostupno());
        dto.setBudzetId(b.getBudzet().getId());
        dto.setRezervisano(b.getRezervisano());
        return dto;
    }

    // Koriste servisi za predloge i preporuke naslova
    public boolean imaDovoljnoSredstava(Long zanrId, Double okvirnaCena) {
        BudzetPoZanru budzet = budzetPoZanruRepository.findByZanrId(zanrId)
                .orElse(null);
        if (budzet == null) return false;
        return budzet.getDostupno() >= okvirnaCena;
    }
}