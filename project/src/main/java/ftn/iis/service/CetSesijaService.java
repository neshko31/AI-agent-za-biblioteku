package ftn.iis.service;

import ftn.iis.dto.*;
import ftn.iis.enums.TipAgentaCS;
import ftn.iis.model.CetPoruka;
import ftn.iis.model.CetSesija;
import ftn.iis.model.User;
import ftn.iis.repository.CetSesijaRepository;
import ftn.iis.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CetSesijaService {
    private final CetSesijaRepository cetSesijaRepository;
    private final CetPorukaService cetPorukaService;
    private final UserRepository userRepository;

    public CetSesijaService(CetSesijaRepository cetSesijaRepository, CetPorukaService cetPorukaService, UserRepository userRepository) {
        this.cetSesijaRepository = cetSesijaRepository;
        this.cetPorukaService = cetPorukaService;
        this.userRepository = userRepository;
    }

    public List<CetSesijaOsnovnoDto> ispisiSveCetSesije(String jmbg){
        List<CetSesija> cetSesije = cetSesijaRepository.findAll();
        List<CetSesija> retVal = new ArrayList<>();
        for (CetSesija cetSesija : cetSesije) {
            if (Objects.equals(cetSesija.getClan().getJmbg(), jmbg))
                retVal.add(cetSesija);
        }
        return retVal.stream().map(CetSesijaOsnovnoDto::fromCetSesija).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CetSesijaDetaljnoDto> getCetSesijaPoId(String jmbg, Long id) {
        return cetSesijaRepository.findById(id)
                .filter(cetSesija -> Objects.equals(cetSesija.getClan().getJmbg(), jmbg))
                .map(CetSesijaDetaljnoDto::fromCetSesija);
    }

    @Transactional(rollbackFor = Exception.class)
    public CetSesija postNovaCetSesija(String jmbg, NovaCetSesijaDto podaci, String slikaBase64) {
        // 0. Proveri postojanje korisnika
        User user = userRepository.findByJmbg(jmbg).orElseThrow();

        // Slika je podržana samo za agenta za knjige (recenzije nemaju cover
        // embedding, pa slika tamo nema svrhu na strani vektorskog servisa).
        if (slikaBase64 != null && podaci.getTipAgentaCS() != TipAgentaCS.AGENT_KNJIGE) {
            throw new IllegalArgumentException("Slika je podržana samo za asistenta za knjige.");
        }

        // 1. Kreiraj osnovnu cet sesiju prvo
        CetSesija cetSesija = new CetSesija();
        cetSesija.setClan(user);
        cetSesija.setDatumKreiranjaCS(LocalDateTime.now());
        cetSesija.setDatumAzuriranjaCS(LocalDateTime.now());
        cetSesija.setTipAgentaCS(podaci.getTipAgentaCS());
        cetSesija = cetSesijaRepository.saveAndFlush(cetSesija);

        // 2. Kreiraj samu poruku
        CetPoruka korisnikovaPoruka = cetPorukaService.sacuvajPorukuClana(cetSesija, podaci.getSadrzajPoruke(), slikaBase64);

        // 3. Pozovi odgovarajuci vektorski servis u zavisnosti od tipa agenta
        CetPoruka porukaAgenta = cetPorukaService.pozoviAgentaISacuvajOdgovor(cetSesija, podaci.getSadrzajPoruke(), slikaBase64);

        // 4. Dodaj nedostajuca polja
        cetSesija.getPoruke().add(korisnikovaPoruka);
        cetSesija.getPoruke().add(porukaAgenta);
        cetSesija.setNaslovCS("Ćaskanje " + cetSesija.getId());
        cetSesija.setDatumAzuriranjaCS(LocalDateTime.now());
        cetSesija = cetSesijaRepository.saveAndFlush(cetSesija);

        return cetSesija;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCetSesija(String jmbg, Long id) {
        CetSesija cetSesija = cetSesijaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Čet sesija ne postoji: " + id));

        if (!Objects.equals(cetSesija.getClan().getJmbg(), jmbg)) {
            throw new NoSuchElementException("Čet sesija ne postoji: " + id);
        }
        if (cetSesija.getImaGrane()) {
            throw new IllegalStateException("Nije moguće obrisati čet sesiju koja ima grane. Obrišite prvo sve grane.");
        }

        CetSesija roditeljskaSesija = cetSesija.getRoditeljskaSesija();

        cetSesijaRepository.delete(cetSesija);

        if (roditeljskaSesija != null) {
            List<CetSesija> preostaleGrane = cetSesijaRepository.findByRoditeljskaSesija(roditeljskaSesija);
            if (preostaleGrane.isEmpty()) {
                roditeljskaSesija.setImaGrane(false);
                cetSesijaRepository.saveAndFlush(roditeljskaSesija);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public CetSesijaDetaljnoDto arhivirajCetSesiju(String jmbg, Long id) {
        CetSesija cetSesija = cetSesijaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Čet sesija ne postoji: " + id));

        if (!Objects.equals(cetSesija.getClan().getJmbg(), jmbg)) {
            throw new NoSuchElementException("Čet sesija ne postoji: " + id);
        }
        if (cetSesija.getArhivirano()) {
            throw new IllegalStateException("Čet sesija je već arhivirana.");
        }
        if (cetSesija.getImaGrane()) {
            throw new IllegalStateException("Nije moguće arhivirati čet sesiju koja ima grane.");
        }

        cetSesija.setArhivirano(true);
        cetSesija.setDatumArhiviranjaCS(LocalDateTime.now());
        cetSesija = cetSesijaRepository.saveAndFlush(cetSesija);
        return CetSesijaDetaljnoDto.fromCetSesija(cetSesija);
    }

    @Transactional(rollbackFor = Exception.class)
    public CetSesijaDetaljnoDto vratiCetSesijuIzArhive(String jmbg, Long id) {
        CetSesija cetSesija = cetSesijaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Čet sesija ne postoji: " + id));

        if (!Objects.equals(cetSesija.getClan().getJmbg(), jmbg)) {
            throw new NoSuchElementException("Čet sesija ne postoji: " + id);
        }
        if (!cetSesija.getArhivirano()) {
            throw new IllegalStateException("Čet sesija nije arhivirana.");
        }

        cetSesija.setArhivirano(false);
        cetSesija.setDatumArhiviranjaCS(null);
        cetSesija.setDatumAzuriranjaCS(LocalDateTime.now());
        cetSesija = cetSesijaRepository.saveAndFlush(cetSesija);
        return CetSesijaDetaljnoDto.fromCetSesija(cetSesija);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void obrisiStareArhiviraneSesije() {
        LocalDateTime granica = LocalDateTime.now().minusDays(30);
        List<CetSesija> cetSesijeZaBrisanje = cetSesijaRepository.findByArhiviranoIsTrueAndDatumArhiviranjaCSBefore(granica);
        cetSesijaRepository.deleteAll(cetSesijeZaBrisanje);
    }
}
