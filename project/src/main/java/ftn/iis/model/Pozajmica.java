package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="pozajmica")
public class Pozajmica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_p")
    private Long idP;

    @Column(name = "dat_poz", nullable = false)
    private LocalDate datPoz;

    @Column(name = "dat_vrac")
    private LocalDate datVrac;

    @Column(name = "dat_oc_vrac", nullable = false)
    private LocalDate datOcVrac; //datum ocekivanog vracanja

    @Column(name = "status_poz", nullable = false)
    private Boolean statusPoz; //ako je true onda je aktivna, tacnije nije jos knjiga vracena, ako je false onda je pozajmica gotova

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pk_fk", nullable = false)
    @JsonManagedReference("knjiga-poz")
    private PrimerakKnjige primerakKnjige;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jmbg_clana", nullable = false)
    private User clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_r_fk")
    private Rezervacija rezervacija;

    @OneToMany(mappedBy = "pozajmica", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProduzenjePozajmice> produzenja = new ArrayList<>();

    public Pozajmica(){}

    public Long getIdP() {
        return idP;
    }

    public void setIdP(Long idP) {
        this.idP = idP;
    }

    public LocalDate getDatPoz() {
        return datPoz;
    }

    public void setDatPoz(LocalDate datPoz) {
        this.datPoz = datPoz;
    }

    public LocalDate getDatVrac() {
        return datVrac;
    }

    public void setDatVrac(LocalDate datVrac) {
        this.datVrac = datVrac;
    }

    public LocalDate getDatOcVrac() {
        return datOcVrac;
    }

    public void setDatOcVrac(LocalDate datOcVrac) {
        this.datOcVrac = datOcVrac;
    }

    public Boolean getStatusPoz() {
        return statusPoz;
    }

    public void setStatusPoz(Boolean statusPoz) {
        this.statusPoz = statusPoz;
    }

    public PrimerakKnjige getPrimerakKnjige() {
        return primerakKnjige;
    }

    public void setPrimerakKnjige(PrimerakKnjige primerakKnjige) {
        this.primerakKnjige = primerakKnjige;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public Rezervacija getRezervacija() {
        return rezervacija;
    }

    public void setRezervacija(Rezervacija rezervacija) {
        this.rezervacija = rezervacija;
    }

    public List<ProduzenjePozajmice> getProduzenja() {
        return produzenja;
    }

    public void setProduzenja(List<ProduzenjePozajmice> produzenja) {
        this.produzenja = produzenja;
    }
}
