package ftn.iis.model;

import ftn.iis.enums.NacinUplate;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "kazna")
public class Kazna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_k")
    private Long idK;

    @Column(name = "broj_dana_prekoracenja")
    private Integer brojDanaPrekoracenja; // ako je null onda je izgubljena gradja, ako nije onda je prekoracenje

    // iznos koji se naplacuje tog dana (100 * dana ili 2000 za izgubljenu)
    @Column(name = "iznos_k", nullable = false)
    private Integer iznosK;

    @Column(name = "dat_nastanka", nullable = false)
    private LocalDate datNastanka;

    @Column(name = "placena", nullable = false)
    private boolean placena = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "nacin_placanja")
    private NacinUplate nacinPlacanja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jmbg_clana", nullable = false)
    private User clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_p_fk", nullable = false)
    private Pozajmica pozajmica;

    public Kazna() {
    }

    public Long getIdK() {
        return idK;
    }

    public void setIdK(Long idK) {
        this.idK = idK;
    }

    public Integer getBrojDanaPrekoracenja() {
        return brojDanaPrekoracenja;
    }

    public void setBrojDanaPrekoracenja(Integer brojDanaPrekoracenja) {
        this.brojDanaPrekoracenja = brojDanaPrekoracenja;
    }

    public Integer getIznosK() {
        return iznosK;
    }

    public void setIznosK(Integer iznosK) {
        this.iznosK = iznosK;
    }

    public LocalDate getDatNastanka() {
        return datNastanka;
    }

    public void setDatNastanka(LocalDate datNastanka) {
        this.datNastanka = datNastanka;
    }

    public boolean isPlacena() {
        return placena;
    }

    public void setPlacena(boolean placena) {
        this.placena = placena;
    }

    public NacinUplate getNacinPlacanja() {
        return nacinPlacanja;
    }

    public void setNacinPlacanja(NacinUplate nacinPlacanja) {
        this.nacinPlacanja = nacinPlacanja;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public Pozajmica getPozajmica() {
        return pozajmica;
    }

    public void setPozajmica(Pozajmica pozajmica) {
        this.pozajmica = pozajmica;
    }
}
