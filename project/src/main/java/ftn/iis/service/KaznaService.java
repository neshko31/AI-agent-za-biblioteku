package ftn.iis.service;

import ftn.iis.dto.KaznaDto;
import ftn.iis.enums.NacinUplate;
import ftn.iis.model.Kazna;
import ftn.iis.model.Obavestenje;
import ftn.iis.model.Pozajmica;
import ftn.iis.model.User;
import ftn.iis.repository.KaznaRepository;
import ftn.iis.repository.ObavestenjeRepository;
import ftn.iis.repository.PozajmicaRepository;
import ftn.iis.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KaznaService {

    private static final int KAZNA_PO_DANU = 100;
    private static final int KAZNA_IZGUBLJENA = 2000;

    private final KaznaRepository kaznaRepository;
    private final PozajmicaRepository pozajmicaRepository;
    private final UserRepository userRepository;
    private final ObavestenjeRepository obavestenjeRepository;


    public KaznaService(KaznaRepository kaznaRepository, PozajmicaRepository pozajmicaRepository, UserRepository userRepository, ObavestenjeRepository obavestenjeRepository) {
        this.kaznaRepository = kaznaRepository;
        this.pozajmicaRepository = pozajmicaRepository;
        this.userRepository = userRepository;
        this.obavestenjeRepository = obavestenjeRepository;
    }

    public List<KaznaDto> getMojeKazne(String jmbg){
        List<Pozajmica> overdue = pozajmicaRepository.findOverduePozajmiceByJmbg(jmbg, LocalDate.now());
        for (Pozajmica p : overdue) {
            boolean hasPaidKazna = kaznaRepository.existsByPozajmica_IdPAndPlacenaTrueAndBrojDanaPrekoracenjaIsNotNull(p.getIdP());
            if(!hasPaidKazna) {
                kreirajIliAzurirajKaznaPrekoracenje(p);
            }
        }
        return kaznaRepository.findByClanJmbgOrderByDatNastankaDesc(jmbg).stream().map(KaznaDto::from).collect(Collectors.toList());
    }

    public void kreirajKaznaIzgubljena (Pozajmica pozajmica){
        if(kaznaRepository.findLostBookKaznaForPozajmica(pozajmica.getIdP()).isPresent()){
            return;
        }
        User clan= pozajmica.getClan();
        Kazna kazna= new Kazna();
        kazna.setPozajmica(pozajmica);
        kazna.setClan(clan);
        kazna.setBrojDanaPrekoracenja(null); //ovo koristim kao marker da je ovaj tip kazne
        kazna.setIznosK(KAZNA_IZGUBLJENA);
        kazna.setDatNastanka(LocalDate.now());
        kazna.setPlacena(false);
        kaznaRepository.save(kazna);



    }

    public void kreirajIliAzurirajKaznaPrekoracenje(Pozajmica pozajmica){
        LocalDate danas = LocalDate.now();
        LocalDate rokVracanja = pozajmica.getDatOcVrac();

        if (!danas.isAfter(rokVracanja)) {
            return; // Nije prekoracen rok
        }

        long danaPrek = ChronoUnit.DAYS.between(rokVracanja, danas);
        int iznos = (int) danaPrek * KAZNA_PO_DANU;

        var postojeceKaz = kaznaRepository.findActiveOverdueKaznaForPozajmica(pozajmica.getIdP());
        if (postojeceKaz.isPresent()){
            Kazna k= postojeceKaz.get();
            boolean prviDan= k.getBrojDanaPrekoracenja()==0;
            k.setBrojDanaPrekoracenja((int) danaPrek);
            k.setIznosK(iznos);
            kaznaRepository.save(k);

            if(prviDan) posaljiObavestenjePrekoracenje(pozajmica, iznos);


        }else {
            User clan = pozajmica.getClan();
            Kazna k = new Kazna();
            k.setPozajmica(pozajmica);
            k.setClan(clan);
            k.setBrojDanaPrekoracenja((int) danaPrek);
            k.setIznosK(iznos);
            k.setDatNastanka(danas);
            k.setPlacena(false);
            kaznaRepository.save(k);

            posaljiObavestenjePrekoracenje(pozajmica, iznos);

        }
    }

    private void posaljiObavestenjePrekoracenje(Pozajmica pozajmica, int iznos){
        String naslov = pozajmica.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov();
        Obavestenje o = new Obavestenje();
        o.setTipO("KAZNA");
        o.setTekstO("Prekoračili ste rok vraćanja knjige '" + naslov + "'. Trenutni iznos kazne: " + iznos + " dinara (100 din/dan).");
        o.setDatKreiran(LocalDate.now());
        o.setClan(pozajmica.getClan());
        obavestenjeRepository.save(o);
    }

    public KaznaDto platiKaznu(Long idK, String jmbg, NacinUplate nacinUplate){
        Kazna k=kaznaRepository.findById(idK).orElseThrow(()->new RuntimeException("Kazna nije pronadjena"));
        if (!k.getClan().getJmbg().equals(jmbg)) throw  new RuntimeException("Nemate pristup kazni");
        if (k.isPlacena()) {
            throw new RuntimeException("Kazna je već plaćena.");
        }
        k.setPlacena(true);
        k.setNacinPlacanja(nacinUplate);
        kaznaRepository.save(k);
        return KaznaDto.from(k);

    }
}
