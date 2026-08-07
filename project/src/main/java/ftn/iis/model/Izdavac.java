package ftn.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "izdavac")
public class Izdavac {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Dobavljac dobavljac;

    @OneToMany(mappedBy = "izdavac", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ElektronskiCasopis> elektronskiCasopisi = new ArrayList<>();

    @OneToMany(mappedBy = "izdavac", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ElektronskaBazaPodataka> elektronskeBazePodataka = new ArrayList<>();

    public Izdavac(){
    }

    public Izdavac(Dobavljac dobavljac){
        this.dobavljac = dobavljac;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dobavljac getDobavljac() {
        return dobavljac;
    }

    public void setDobavljac(Dobavljac dobavljac) {
        this.dobavljac = dobavljac;
    }

    public List<ElektronskiCasopis> getElektronskiCasopisi() {
        return elektronskiCasopisi;
    }

    public void setElektronskiCasopisi(List<ElektronskiCasopis> elektronskiCasopisi) {
        this.elektronskiCasopisi = elektronskiCasopisi;
    }

    public List<ElektronskaBazaPodataka> getElektronskeBazePodataka() {
        return elektronskeBazePodataka;
    }

    public void setElektronskeBazePodataka(List<ElektronskaBazaPodataka> elektronskeBazePodataka) {
        this.elektronskeBazePodataka = elektronskeBazePodataka;
    }
}
