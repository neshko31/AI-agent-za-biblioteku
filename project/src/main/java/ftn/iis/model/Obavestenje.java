package ftn.iis.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="obavestenje")
public class Obavestenje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_o")
    private Long idO;

    // TipO: VRACANJE, REZERVACIJA_DOSTUPNA, ima jos...
    @Column(name = "tip_o", nullable = false)
    private String tipO;

    @Column(name = "tekst_o", nullable = false, columnDefinition = "TEXT")
    private String tekstO;

    @Column(name = "dat_kreiran", nullable = false)
    private LocalDate datKreiran;

    @Column(name = "procitano", nullable = false)
    private boolean procitano = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jmbg_clana", nullable = false)
    private User clan;

    public  Obavestenje(){}

    public Long getIdO() {
        return idO;
    }

    public void setIdO(Long idO) {
        this.idO = idO;
    }

    public String getTipO() {
        return tipO;
    }

    public void setTipO(String tipO) {
        this.tipO = tipO;
    }

    public String getTekstO() {
        return tekstO;
    }

    public void setTekstO(String tekstO) {
        this.tekstO = tekstO;
    }

    public LocalDate getDatKreiran() {
        return datKreiran;
    }

    public void setDatKreiran(LocalDate datKreiran) {
        this.datKreiran = datKreiran;
    }

    public boolean isProcitano() {
        return procitano;
    }

    public void setProcitano(boolean procitano) {
        this.procitano = procitano;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }
}
