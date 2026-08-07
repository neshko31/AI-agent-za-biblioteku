package ftn.iis.client;

import ftn.iis.dto.KnjigeOdgovorDto;
import ftn.iis.dto.RecenzijeOdgovorDto;
import ftn.iis.dto.VektorskiUpitDto;
import ftn.iis.exception.VektorskiServisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
public class VektorskiServisClient {
    private static final Logger log = LoggerFactory.getLogger(VektorskiServisClient.class);

    private final RestTemplate restTemplate;
    private static final String baseUrl = "http://localhost:8000";

    public VektorskiServisClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public KnjigeOdgovorDto pitajAgentaKnjige(String poruka, String slikaBase64) {
        String url = baseUrl + "/api/v1/books/chat";
        VektorskiUpitDto upit = new VektorskiUpitDto(poruka);
        upit.setImageBase64(slikaBase64);
        return pozovi(url, upit, KnjigeOdgovorDto.class);
    }

    public RecenzijeOdgovorDto pitajAgentaRecenzije(String poruka) {
        String url = baseUrl + "/api/v1/reviews/chat";
        return pozovi(url, new VektorskiUpitDto(poruka), RecenzijeOdgovorDto.class);
    }

    private <T> T pozovi(String url, VektorskiUpitDto upit, Class<T> tipOdgovora) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<VektorskiUpitDto> zahtev = new HttpEntity<>(upit, headers);

        try {
            T odgovor = restTemplate.postForObject(url, zahtev, tipOdgovora);
            if (odgovor == null) {
                throw new VektorskiServisException("Vektorski servis je vratio prazan odgovor (" + url + ")");
            }
            return odgovor;
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            log.error("Vektorski servis je vratio gresku {} za {}: {}", status, url, e.getResponseBodyAsString());
            throw new VektorskiServisException(
                    "Vektorski servis je vratio gresku " + status.value() + " na " + url, e);
        } catch (ResourceAccessException e) {
            log.error("Vektorski servis nije dostupan ili je istekao timeout za {}: {}", url, e.getMessage());
            throw new VektorskiServisException(
                    "Vektorski servis nije dostupan (timeout ili konekcija odbijena) na " + url, e);
        }
    }
}