package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="primerak_knjige")
public class PrimerakKnjige {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pk")
    private Long idPK;
    @Column(name = "god_iz", nullable = false)
    private Integer godIz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn_fizicka", nullable = false)
    @JsonBackReference("fizicka-primerak")
    private FizickaKnjiga fizickaKnjiga;
    @OneToMany(mappedBy = "primerakKnjige", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pozajmica> pozajmice = new ArrayList<>();

    public  PrimerakKnjige(){}

    public Long getIdPK() {
        return idPK;
    }

    public void setIdPK(Long idPK) {
        this.idPK = idPK;
    }

    public Integer getGodIz() {
        return godIz;
    }

    public void setGodIz(Integer godIz) {
        this.godIz = godIz;
    }

    public FizickaKnjiga getFizickaKnjiga() {
        return fizickaKnjiga;
    }

    public void setFizickaKnjiga(FizickaKnjiga fizickaKnjiga) {
        this.fizickaKnjiga = fizickaKnjiga;
    }

    public List<Pozajmica> getPozajmice() {
        return pozajmice;
    }

    public void setPozajmice(List<Pozajmica> pozajmice) {
        this.pozajmice = pozajmice;
    }
}
