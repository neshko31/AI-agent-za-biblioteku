package ftn.iis.service;
import ftn.iis.enums.StatusUgovora;
import ftn.iis.model.Ugovor;
import ftn.iis.repository.UgovorRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class UgovorScheduler {
    private final UgovorRepository ugovorRepository;

    public UgovorScheduler(UgovorRepository ugovorRepository){
        this.ugovorRepository = ugovorRepository;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void azurirajIstekleUgovore(){
        List<Ugovor> ugovori = ugovorRepository.findAllByStatus(StatusUgovora.AKTIVAN);
        LocalDate danas = LocalDate.now();
        for(Ugovor u : ugovori){
            if(u.getDatumIsteka().isBefore(danas)){
                u.setStatus(StatusUgovora.ISTEKAO);
                ugovorRepository.save(u);
            }
        }
    }
}
