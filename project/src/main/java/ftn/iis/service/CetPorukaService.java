package ftn.iis.service;

import ftn.iis.client.VektorskiServisClient;
import ftn.iis.dto.*;
import ftn.iis.enums.TipAgentaCS;
import ftn.iis.enums.TipCP;
import ftn.iis.model.CetPoruka;
import ftn.iis.model.CetSesija;
import ftn.iis.repository.CetPorukaRepository;
import ftn.iis.repository.CetSesijaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CetPorukaService {
    private static final Logger log = LoggerFactory.getLogger(CetPorukaService.class);
    private final ObjectMapper objectMapper;

    private final CetPorukaRepository cetPorukaRepository;
    private final CetSesijaRepository cetSesijaRepository;
    private final VektorskiServisClient vektorskiServisClient;

    public CetPorukaService(ObjectMapper objectMapper, CetPorukaRepository cetPorukaRepository, CetSesijaRepository cetSesijaRepository, VektorskiServisClient vektorskiServisClient) {
        this.objectMapper = objectMapper;
        this.cetPorukaRepository = cetPorukaRepository;
        this.cetSesijaRepository = cetSesijaRepository;
        this.vektorskiServisClient = vektorskiServisClient;
    }

    public CetPoruka sacuvajPorukuClana(CetSesija cetSesija, String sadrzajPoruke, String slikaBase64) {
        CetPoruka korisnikovaPoruka = new CetPoruka();
        korisnikovaPoruka.setCetSesija(cetSesija);
        korisnikovaPoruka.setTipCP(TipCP.CLAN);
        korisnikovaPoruka.setDatumKreiranjaCP(LocalDateTime.now());
        korisnikovaPoruka.setSadrzajCP(sadrzajPoruke);
        korisnikovaPoruka.setSlikaBase64(slikaBase64);
        return cetPorukaRepository.saveAndFlush(korisnikovaPoruka);
    }

    public CetPoruka pozoviAgentaISacuvajOdgovor(CetSesija cetSesija, String sadrzajPoruke, String slikaBase64) {
        OdgovorAgentaInterno odgovorAgenta = pozoviAgentaIFormatirajOdgovor(cetSesija.getTipAgentaCS(), sadrzajPoruke, slikaBase64);

        CetPoruka porukaAgenta = new CetPoruka();
        porukaAgenta.setCetSesija(cetSesija);
        porukaAgenta.setTipCP(TipCP.AI_ASISTENT);
        porukaAgenta.setDatumKreiranjaCP(LocalDateTime.now());
        porukaAgenta.setSadrzajCP(odgovorAgenta.sadrzaj);
        porukaAgenta.setIzvoriCP(odgovorAgenta.izvoriJson);
        return cetPorukaRepository.saveAndFlush(porukaAgenta);
    }

    @Transactional(rollbackFor = Exception.class)
    public NovaCetPorukaOdgovorDto postNovaCetPoruka(String jmbg, Long idCetSesije, String sadrzajPoruke, String slikaBase64) {
        CetSesija cetSesija = cetSesijaRepository.findById(idCetSesije)
                .orElseThrow(() -> new NoSuchElementException("Cet sesija ne postoji: " + idCetSesije));

        if (!Objects.equals(cetSesija.getClan().getJmbg(), jmbg)) {
            throw new NoSuchElementException("Cet sesija ne postoji: " + idCetSesije);
        }

        // Slika je podržana samo za agenta za knjige (recenzije nemaju cover
        // embedding, pa slika tamo nema svrhu na strani vektorskog servisa).
        if (slikaBase64 != null && cetSesija.getTipAgentaCS() != TipAgentaCS.AGENT_KNJIGE) {
            throw new IllegalArgumentException("Slika je podržana samo za asistenta za knjige.");
        }

        // Arhivirana sesija ne prima nove poruke
        if (cetSesija.getArhivirano()) {
            throw new IllegalStateException("Nije moguće slati poruke u arhiviranoj čet sesiji. Molimo vas da najpre vratite sesiju iz arhive.");
        }

        // 1. Sacuvaj poruku clana
        CetPoruka korisnikovaPoruka = sacuvajPorukuClana(cetSesija, sadrzajPoruke, slikaBase64);

        // 2. Pozovi agenta (isti tip koji je vec postavljen u sesiji) i sacuvaj odgovor.
        //    Ako poziv ne uspe, izuzetak se propagira i ceo metod se rollback-uje
        //    (rollbackFor = Exception.class) - korisnikova poruka iz koraka 1 se ne cuva.
        CetPoruka porukaAgenta = pozoviAgentaISacuvajOdgovor(cetSesija, sadrzajPoruke, slikaBase64);

        // 3. Azuriraj datum azuriranja sesije
        cetSesija.setDatumAzuriranjaCS(LocalDateTime.now());
        cetSesijaRepository.saveAndFlush(cetSesija);

        return new NovaCetPorukaOdgovorDto(
                CetPorukaDto.fromCetPoruka(korisnikovaPoruka),
                CetPorukaDto.fromCetPoruka(porukaAgenta)
        );
    }

    private OdgovorAgentaInterno pozoviAgentaIFormatirajOdgovor(TipAgentaCS tipAgentaCS, String poruka, String slikaBase64) {
        switch (tipAgentaCS) {
            case AGENT_KNJIGE:
                KnjigeOdgovorDto knjigeOdgovor = vektorskiServisClient.pitajAgentaKnjige(poruka, slikaBase64);
                List<IzvorKnjigeDto> izvoriKnjige = knjigeOdgovor.getContextBooks() == null
                        ? List.of()
                        : knjigeOdgovor.getContextBooks().stream()
                        .map(IzvorKnjigeDto::fromKontekstKnjiga)
                        .collect(Collectors.toList());
                return new OdgovorAgentaInterno(knjigeOdgovor.getResponse(), serijalizujIzvore(izvoriKnjige));
            case AGENT_RECENZIJE:
                RecenzijeOdgovorDto recenzijeOdgovor = vektorskiServisClient.pitajAgentaRecenzije(poruka);
                List<IzvorRecenzijeDto> izvoriRecenzije = recenzijeOdgovor.getContextReviews() == null
                        ? List.of()
                        : recenzijeOdgovor.getContextReviews().stream()
                        .map(IzvorRecenzijeDto::fromKontekstRecenzija)
                        .collect(Collectors.toList());
                return new OdgovorAgentaInterno(recenzijeOdgovor.getResponse(), serijalizujIzvore(izvoriRecenzije));
            default:
                throw new IllegalArgumentException("Nepoznat tip agenta: " + tipAgentaCS);
        }
    }

    private String serijalizujIzvore(List<?> izvori) {
        if (izvori == null || izvori.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(izvori);
        } catch (Exception e) {
            log.error("Neuspesna serijalizacija izvora cet poruke u JSON: {}", e.getMessage());
            return null;
        }
    }

    private record OdgovorAgentaInterno(String sadrzaj, String izvoriJson) {}

    @Transactional(rollbackFor = Exception.class)
    public CetSesijaDetaljnoDto azurirajCetPoruku(String jmbg, Long idCetPoruke, String noviSadrzaj, SlikaIzmena slikaIzmena) {
        // 1. Pronadji poruku koja se azurira
        CetPoruka poruka = cetPorukaRepository.findById(idCetPoruke).orElseThrow(() -> new NoSuchElementException("Poruka ne postoji: " + idCetPoruke));

        CetSesija originalnaSesija = poruka.getCetSesija();

        // Provera da li je sama cet sesija arhivirana, jer ako jeste ne mozemo od nje dalje praviti grane
        if (originalnaSesija.getArhivirano()) {
            throw new IllegalStateException("Nije moguće editovati poruku u arhiviranoj čet sesiji. Vratite sesiju iz arhive.");
        }

        // 2. Proveri da li poruka pripada korisniku
        if (!Objects.equals(originalnaSesija.getClan().getJmbg(), jmbg)) {
            throw new NoSuchElementException("Poruka ne postoji: " + idCetPoruke);
        }

        // 3. Samo poruke tipa CLAN mogu da se edituju
        if (poruka.getTipCP() != TipCP.CLAN) {
            throw new IllegalArgumentException("Moguće je editovati samo svoje poruke, ne i poruke agenta.");
        }

        // Slika je podržana samo za agenta za knjige - isto pravilo kao kod slanja nove poruke
        if (slikaIzmena.imaNovuSliku() && originalnaSesija.getTipAgentaCS() != TipAgentaCS.AGENT_KNJIGE) {
            throw new IllegalArgumentException("Slika je podržana samo za asistenta za knjige.");
        }

        // 4. Pronadji sve poruke originalne sesije, sortirane po datumu
        List<CetPoruka> svePoruke = originalnaSesija.getPoruke().stream().sorted(Comparator.comparing(CetPoruka::getDatumKreiranjaCP)).collect(Collectors.toList());

        // 5. Pronadji indeks editovane poruke
        int indeksEditovanePoruke = -1;
        for (int i = 0; i < svePoruke.size(); i++) {
            if (svePoruke.get(i).getId().equals(idCetPoruke)) {
                indeksEditovanePoruke = i;
                break;
            }
        }
        if (indeksEditovanePoruke == -1) {
            throw new NoSuchElementException("Poruka ne postoji u sesiji.");
        }

        // 6. Odredi verziju nove grane (max postojeca verzija + 1)
        //    Ako editujemo granu, roditeljska sesija za novu granu je i dalje originalna
        CetSesija roditeljskaSesija = originalnaSesija;
        CetSesija korenStabla = pronadjiKorenStabla(originalnaSesija);

        int novaVerzija = pronadjiSveGraneUStablu(korenStabla).stream().mapToInt(CetSesija::getVerzija).max().orElse(korenStabla.getVerzija()) + 1;

        // 7. Kreiraj novu sesiju (granu)
        CetSesija novaSesija = new CetSesija();
        novaSesija.setClan(originalnaSesija.getClan());
        novaSesija.setTipAgentaCS(originalnaSesija.getTipAgentaCS());
        novaSesija.setDatumKreiranjaCS(LocalDateTime.now());
        novaSesija.setDatumAzuriranjaCS(LocalDateTime.now());
        novaSesija.setArhivirano(false);
        novaSesija.setVerzija(novaVerzija);
        novaSesija.setRoditeljskaSesija(roditeljskaSesija);
        novaSesija.setIndeksPorukeRacvanja(indeksEditovanePoruke);
        novaSesija.setImaGrane(false);
        novaSesija = cetSesijaRepository.saveAndFlush(novaSesija);

        // 8. Kopiraj sve poruke pre izmenjene poruke u novu sesiju
        List<CetPoruka> porukeNoveSesije = new ArrayList<>();
        for (int i = 0; i < indeksEditovanePoruke; i++) {
            CetPoruka original = svePoruke.get(i);
            CetPoruka kopija = new CetPoruka();
            kopija.setCetSesija(novaSesija);
            kopija.setTipCP(original.getTipCP());
            kopija.setDatumKreiranjaCP(original.getDatumKreiranjaCP());
            kopija.setSadrzajCP(original.getSadrzajCP());
            kopija.setIzvoriCP(original.getIzvoriCP());
            kopija.setSlikaBase64(original.getSlikaBase64());
            porukeNoveSesije.add(cetPorukaRepository.saveAndFlush(kopija));
        }

        // 9. Sacuvaj novu (izmenjenu) poruku korisnika i pozovi agenta
        String finalnaSlikaBase64 = slikaIzmena.resolveZa(poruka.getSlikaBase64());
        porukeNoveSesije.add(sacuvajPorukuClana(novaSesija, noviSadrzaj, finalnaSlikaBase64));
        porukeNoveSesije.add(pozoviAgentaISacuvajOdgovor(novaSesija, noviSadrzaj, finalnaSlikaBase64));

        // 10. Postavi naslov i finalizuj novu sesiju
        novaSesija.setNaslovCS("Ćaskanje " + roditeljskaSesija.getId() + " (v" + novaVerzija + ")");
        novaSesija.setDatumAzuriranjaCS(LocalDateTime.now());
        novaSesija = cetSesijaRepository.saveAndFlush(novaSesija);

        // 11. Oznaci roditeljsku sesiju da ima grane
        roditeljskaSesija.setImaGrane(true);
        cetSesijaRepository.saveAndFlush(roditeljskaSesija);

        novaSesija.getPoruke().clear();
        novaSesija.getPoruke().addAll(porukeNoveSesije);
        return CetSesijaDetaljnoDto.fromCetSesija(novaSesija);
    }

    private CetSesija pronadjiKorenStabla(CetSesija sesija) {
        CetSesija trenutna = sesija;
        while (trenutna.getRoditeljskaSesija() != null) {
            trenutna = trenutna.getRoditeljskaSesija();
        }
        return trenutna;
    }

    private List<CetSesija> pronadjiSveGraneUStablu(CetSesija koren) {
        List<CetSesija> sve = new ArrayList<>();
        Deque<CetSesija> redZaObradu = new ArrayDeque<>();
        redZaObradu.add(koren);

        while (!redZaObradu.isEmpty()) {
            CetSesija trenutna = redZaObradu.poll();
            List<CetSesija> deca = cetSesijaRepository.findByRoditeljskaSesija(trenutna);
            sve.addAll(deca);
            redZaObradu.addAll(deca);
        }
        return sve;
    }
}
