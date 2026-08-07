package ftn.iis.service;

import ftn.iis.dto.AktivnostiGodisnjiZbirDto;
import ftn.iis.dto.AktivnostiIzvestajResponseDto;
import ftn.iis.dto.AktivnostiMesecniRedDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class IzvestajAktivnostiService {

    private final JdbcTemplate jdbcTemplate;

    public IzvestajAktivnostiService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AktivnostiIzvestajResponseDto generisiIzvestajAktivnosti(int godina) {
        String sql = "SELECT * FROM izvestaj_aktivnosti_po_mesecima(?) ORDER BY mesec";

        List<AktivnostiMesecniRedDto> meseci = jdbcTemplate.query(sql, (rs, rowNum) ->
                new AktivnostiMesecniRedDto(
                        rs.getInt("mesec"),
                        rs.getLong("broj_citanja_eknjiga"),
                        rs.getLong("broj_slusanja_audio"),
                        rs.getLong("broj_preuzimanja_baza"),
                        rs.getLong("broj_cet_sesija"),
                        rs.getLong("ukupno_poruka_ka_ai"),
                        rs.getBigDecimal("prosek_poruka_po_sesiji")
                ), godina);

        AktivnostiGodisnjiZbirDto zbir = izracunajGodisnjiZbir(meseci);

        return new AktivnostiIzvestajResponseDto(godina, meseci, zbir);
    }

    private AktivnostiGodisnjiZbirDto izracunajGodisnjiZbir(List<AktivnostiMesecniRedDto> meseci) {
        long ukupnoCitanja = 0;
        long ukupnoSlusanja = 0;
        long ukupnoPreuzimanja = 0;
        long ukupnoSesija = 0;
        long ukupnoPoruka = 0;

        for (AktivnostiMesecniRedDto red : meseci) {
            ukupnoCitanja += red.getBrojCitanjaEKnjiga();
            ukupnoSlusanja += red.getBrojSlusanjaAudioKnjiga();
            ukupnoPreuzimanja += red.getBrojPreuzimanjaBaza();
            ukupnoSesija += red.getBrojCetSesija();
            ukupnoPoruka += red.getUkupnoPorukaKaAI();
        }

        BigDecimal prosekGodisnje = ukupnoSesija == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(ukupnoPoruka)
                .divide(BigDecimal.valueOf(ukupnoSesija), 2, RoundingMode.HALF_UP);

        return new AktivnostiGodisnjiZbirDto(
                ukupnoCitanja, ukupnoSlusanja, ukupnoPreuzimanja, ukupnoSesija, ukupnoPoruka, prosekGodisnje
        );
    }
}