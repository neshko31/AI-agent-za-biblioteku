package ftn.iis.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DigitalnaKnjigaScheduler {

    private final PozajmicaService pozajmicaService;
    public DigitalnaKnjigaScheduler(PozajmicaService pozajmicaService) {
        this.pozajmicaService = pozajmicaService;
    }
    @Scheduled(cron = "0 0 0 * * *")
    public void expireDigitalLoans() {
        pozajmicaService.expireDigitalLoans();
    }
}
