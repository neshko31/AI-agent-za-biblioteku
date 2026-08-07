package ftn.iis.model;

import ftn.iis.enums.StatusSistemskePreporuke;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sistemska_preporuka")
public class SistemskaPreporuka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "isbn")
    private FizickaKnjiga fizickaKnjiga;

    private Integer brojPozajmica;

    private Integer trenutniBrojPrimeraka;

    private String predlog;

    private LocalDateTime datumGenerisanja;

    @Enumerated(EnumType.STRING)
    private StatusSistemskePreporuke status;

    @Column(name = "okvirna_cena")
    private Double okvirnaCena = 0.00;

    public SistemskaPreporuka(){}

    public StatusSistemskePreporuke getStatus() {
        return status;
    }

    public void setStatus(StatusSistemskePreporuke status) {
        this.status = status;
    }

    public void setDatumGenerisanja(LocalDateTime datumGenerisanja) {
        this.datumGenerisanja = datumGenerisanja;
    }

    public LocalDateTime getDatumGenerisanja() {
        return datumGenerisanja;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FizickaKnjiga getFizickaKnjiga() {
        return fizickaKnjiga;
    }

    public void setFizickaKnjiga(FizickaKnjiga fizickaKnjiga) {
        this.fizickaKnjiga = fizickaKnjiga;
    }

    public Integer getBrojPozajmica() {
        return brojPozajmica;
    }

    public void setBrojPozajmica(Integer brojPozajmica) {
        this.brojPozajmica = brojPozajmica;
    }

    public Integer getTrenutniBrojPrimeraka() {
        return trenutniBrojPrimeraka;
    }

    public void setTrenutniBrojPrimeraka(Integer trenutniBrojPrimeraka) {
        this.trenutniBrojPrimeraka = trenutniBrojPrimeraka;
    }

    public void setPredlog(String predlog) {
        this.predlog = predlog;
    }

    public String getPredlog() {
        return predlog;
    }

    public Double getOkvirnaCena() { return okvirnaCena != null ? okvirnaCena : 0.0; }
    public void setOkvirnaCena(Double okvirnaCena) { this.okvirnaCena = okvirnaCena; }
}