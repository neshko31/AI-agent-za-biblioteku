package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fizicka_knjiga")
public class FizickaKnjiga {
    @Id
    private String isbn;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "isbn")
    @JsonIgnore
    private Knjiga knjiga;
    @OneToMany(mappedBy = "fizickaKnjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("fizicka-primerak")
    @JsonIgnore
    private List<PrimerakKnjige> primerci = new ArrayList<>();

    @OneToMany(mappedBy = "fizickaKnjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("fizicka-rezervacija")
    private List<Rezervacija> rezervacije = new ArrayList<>();


    public FizickaKnjiga() {
    }

    public FizickaKnjiga(Knjiga knjiga) {
        this.knjiga = knjiga;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Knjiga getKnjiga() {
        return knjiga;
    }

    public void setKnjiga(Knjiga knjiga) {
        this.knjiga = knjiga;
    }

    public List<PrimerakKnjige> getPrimerci() {
        return primerci;
    }

    public void setPrimerci(List<PrimerakKnjige> primerci) {
        this.primerci = primerci;
    }

    public List<Rezervacija> getRezervacije() {
        return rezervacije;
    }

    public void setRezervacije(List<Rezervacija> rezervacije) {
        this.rezervacije = rezervacije;
    }
}
