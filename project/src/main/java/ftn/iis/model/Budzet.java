package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budzet")
public class Budzet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "godina", nullable = false)
    private Integer godina;

    @Column(name = "ukupan_iznos", nullable = false)
    private Double ukupanIznos;

    // OrphanRemoval -> koristim jer je budzetPoZanru slab entitet
    // Ako se izbrise iz liste, brise se i iz baze
    @OneToMany(mappedBy = "budzet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("budzet-stavke")
    private List<BudzetPoZanru> budzetiPoZanru = new ArrayList<>();

    public Budzet() {}

    public Budzet(Integer godina, Double ukupanIznos) {
        this.godina = godina;
        this.ukupanIznos = ukupanIznos;
    }

    // Helper metode za bezbedno dodavanje i uklanjanje
    public void addBudzetPoZanru(BudzetPoZanru bpz) {
        budzetiPoZanru.add(bpz);
        bpz.setBudzet(this);
    }

    public void removeBudzetPoZanru(BudzetPoZanru bpz) {
        budzetiPoZanru.remove(bpz);
        bpz.setBudzet(null);
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGodina() {
        return godina;
    }
    public void setGodina(Integer godina) {
        this.godina = godina;
    }

    public Double getUkupanIznos() {
        return ukupanIznos;
    }
    public void setUkupanIznos(Double ukupanIznos) {
        this.ukupanIznos = ukupanIznos;
    }

    public List<BudzetPoZanru> getBudzetiPoZanru() {
        return budzetiPoZanru;
    }
    public void setBudzetiPoZanru(List<BudzetPoZanru> budzetiPoZanru) {
        this.budzetiPoZanru = budzetiPoZanru;
    }

}
