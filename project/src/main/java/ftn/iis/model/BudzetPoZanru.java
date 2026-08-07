package ftn.iis.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
@Table(name = "budzet_po_zanru")
public class BudzetPoZanru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zanr_id", nullable = false, unique = true)
    @JsonBackReference("zanr-budzet")
    private Genre zanr;

    @Column(name = "ukupan_budzet", nullable = false)
    private Double ukupanBudzet;

    @Column(name = "potroseno", nullable = false)
    private Double potroseno = 0.0;

    @Column(name = "rezervisano")
    private Double rezervisano = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budzet_id", nullable = false)
    @JsonBackReference("budzet-stavke")
    private Budzet budzet;

    public BudzetPoZanru() {}

    public BudzetPoZanru(Genre zanr, Double ukupanBudzet, Budzet budzet) {
        this.zanr = zanr;
        this.ukupanBudzet = ukupanBudzet;
        this.potroseno = 0.0;
        this.budzet = budzet;
        this.rezervisano = 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Genre getZanr() { return zanr; }
    public void setZanr(Genre zanr) { this.zanr = zanr; }
    public Budzet getBudzet() { return budzet; }
    public void setBudzet(Budzet budzet) { this.budzet = budzet; }
    public Double getUkupanBudzet() { return ukupanBudzet; }
    public void setUkupanBudzet(Double ukupanBudzet) { this.ukupanBudzet = ukupanBudzet; }
    public Double getPotroseno() { return potroseno; }
    public void setPotroseno(Double potroseno) { this.potroseno = potroseno; }

    public void setRezervisano(Double rezervisano) {
        this.rezervisano = rezervisano;
    }

    public Double getDostupno() {
        double rez = rezervisano != null ? rezervisano : 0.0;
        double pot = potroseno != null ? potroseno : 0.0;
        return ukupanBudzet - pot - rez;
    }

    public Double getRezervisano() {
        return rezervisano != null ? rezervisano : 0.0;
    }
}