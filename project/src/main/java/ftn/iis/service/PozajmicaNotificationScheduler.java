package ftn.iis.service;

import ftn.iis.model.Obavestenje;
import ftn.iis.model.Pozajmica;
import ftn.iis.repository.ObavestenjeRepository;
import ftn.iis.repository.PozajmicaRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PozajmicaNotificationScheduler {

    private final PozajmicaRepository pozajmicaRepository;
    private final ObavestenjeRepository obavestenjeRepository;
    private final PozajmicaService pozajmicaService;
    private final KaznaService kaznaService;

    public PozajmicaNotificationScheduler(PozajmicaRepository pozajmicaRepository,
                                          ObavestenjeRepository obavestenjeRepository, PozajmicaService pozajmicaService, KaznaService kaznaService) {
        this.pozajmicaRepository = pozajmicaRepository;
        this.obavestenjeRepository = obavestenjeRepository;
        this.pozajmicaService=pozajmicaService;
        this.kaznaService= kaznaService;
    }
    @Scheduled(cron = "0 0 5 * * *")
    @Transactional
    public void sendReturnReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(2);
        List<Pozajmica> dueSoon = pozajmicaRepository.findPozajmiceDueOn(targetDate);

        for (Pozajmica p : dueSoon) {
            boolean alreadyNotified = obavestenjeRepository
                    .findByClan_JmbgOrderByDatKreiranDesc(p.getClan().getJmbg())
                    .stream()
                    .anyMatch(o -> o.getTipO().equals("VRACANJE") &&
                            o.getDatKreiran().equals(LocalDate.now()) &&
                            o.getTekstO().contains(p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov()));

            if (!alreadyNotified) {
                Obavestenje o = new Obavestenje();
                o.setTipO("VRACANJE");
                o.setTekstO("Podsetnik: knjiga '" +
                        p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov() +
                        "' treba da bude vraćena za 48 sati, do " +
                        String.format("%02d.%02d.%d", targetDate.getDayOfMonth(),
                                targetDate.getMonthValue(), targetDate.getYear()) + ".");
                o.setDatKreiran(LocalDate.now());
                o.setClan(p.getClan());
                obavestenjeRepository.save(o);
            }
        }
    }
    @Scheduled(cron = "0 0 0 * * *")
    public void expireDigitalLoans() {
        pozajmicaService.expireDigitalLoans();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processOverdue(){
        List<Pozajmica> overdue= pozajmicaService.findAllOverdueActivePozajmice();
        for(Pozajmica p: overdue){
            kaznaService.kreirajIliAzurirajKaznaPrekoracenje(p);
        }
    }

}
