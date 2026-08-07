package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="rezervacija")
public class Rezervacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_r")
    private Long idR;

    @Column(name = "dat_r", nullable = false)
    private LocalDate datR;

    @Column(name = "rok_kupl_r")
    private LocalDate rokKupljR;

    @Column(name = "kanal_r")
    private String kanalR;

    // kad bi trebalo da bude dostupno
    @Column(name = "dat_isp_r")
    private LocalDate datIspR;

    // kad je zapravo postalo dostupno
    @Column(name = "dat_obav_r")
    private LocalDate datObavR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn_fizicka", nullable = false)
    @JsonBackReference("fizicka-rezervacija")
    private FizickaKnjiga fizickaKnjiga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jmbg_clana", nullable = false)
    private User clan;


    @OneToMany(mappedBy = "rezervacija", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pozajmica> pozajmiceIzRezervacije = new ArrayList<>();

    public Rezervacija(){}

    public Long getIdR() {
        return idR;
    }

    public void setIdR(Long idR) {
        this.idR = idR;
    }

    public LocalDate getDatR() {
        return datR;
    }

    public void setDatR(LocalDate datR) {
        this.datR = datR;
    }

    public LocalDate getRokKupljR() {
        return rokKupljR;
    }

    public void setRokKupljR(LocalDate rokKupljR) {
        this.rokKupljR = rokKupljR;
    }

    public String getKanalR() {
        return kanalR;
    }

    public void setKanalR(String kanalR) {
        this.kanalR = kanalR;
    }

    public LocalDate getDatIspR() {
        return datIspR;
    }

    public void setDatIspR(LocalDate datIspR) {
        this.datIspR = datIspR;
    }

    public LocalDate getDatObavR() {
        return datObavR;
    }

    public void setDatObavR(LocalDate datObavR) {
        this.datObavR = datObavR;
    }

    public FizickaKnjiga getFizickaKnjiga() {
        return fizickaKnjiga;
    }

    public void setFizickaKnjiga(FizickaKnjiga fizickaKnjiga) {
        this.fizickaKnjiga = fizickaKnjiga;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public List<Pozajmica> getPozajmiceIzRezervacije() {
        return pozajmiceIzRezervacije;
    }

    public void setPozajmiceIzRezervacije(List<Pozajmica> pozajmiceIzRezervacije) {
        this.pozajmiceIzRezervacije = pozajmiceIzRezervacije;
    }
}
