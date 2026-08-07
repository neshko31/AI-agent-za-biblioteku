package ftn.iis.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="produzenje_pozajmice")
public class ProduzenjePozajmice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pp")
    private Long idPP;

    @Column(name = "kanal_pp")
    private String kanalPP;

    //  null=ceka, true=odobreno, false=odbijeno
    @Column(name = "status_pp")
    private Boolean statusPP;

    @Column(name = "razlog_odb")
    private String razlogOdb;

    @Column(name = "dat_kre_pp", nullable = false)
    private LocalDate datKrePP;

    // logicko brisanje : datKrePP + 10 dana
    @Column(name = "dat_obr_pp")
    private LocalDate datObrPP;

    @Column(name = "stari_dat_vrac")
    private LocalDate stariDatVrac;

    @Column(name = "novi_dat_vrac")
    private LocalDate noviDatVrac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_p_fk", nullable = false)
    private Pozajmica pozajmica;

    // Bibliotekar koji je odbio/odobrio ovo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jmbg_bibliotekar")
    private User bibliotekar;

    public  ProduzenjePozajmice(){}

    public Long getIdPP() {
        return idPP;
    }

    public void setIdPP(Long idPP) {
        this.idPP = idPP;
    }

    public String getKanalPP() {
        return kanalPP;
    }

    public void setKanalPP(String kanalPP) {
        this.kanalPP = kanalPP;
    }

    public Boolean getStatusPP() {
        return statusPP;
    }

    public void setStatusPP(Boolean statusPP) {
        this.statusPP = statusPP;
    }

    public String getRazlogOdb() {
        return razlogOdb;
    }

    public void setRazlogOdb(String razlogOdb) {
        this.razlogOdb = razlogOdb;
    }

    public LocalDate getDatKrePP() {
        return datKrePP;
    }

    public void setDatKrePP(LocalDate datKrePP) {
        this.datKrePP = datKrePP;
    }

    public LocalDate getDatObrPP() {
        return datObrPP;
    }

    public void setDatObrPP(LocalDate datObrPP) {
        this.datObrPP = datObrPP;
    }

    public LocalDate getStariDatVrac() {
        return stariDatVrac;
    }

    public void setStariDatVrac(LocalDate stariDatVrac) {
        this.stariDatVrac = stariDatVrac;
    }

    public LocalDate getNoviDatVrac() {
        return noviDatVrac;
    }

    public void setNoviDatVrac(LocalDate noviDatVrac) {
        this.noviDatVrac = noviDatVrac;
    }

    public Pozajmica getPozajmica() {
        return pozajmica;
    }

    public void setPozajmica(Pozajmica pozajmica) {
        this.pozajmica = pozajmica;
    }

    public User getBibliotekar() {
        return bibliotekar;
    }

    public void setBibliotekar(User bibliotekar) {
        this.bibliotekar = bibliotekar;
    }
}
