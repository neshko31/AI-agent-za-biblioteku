package ftn.iis.model;

import ftn.iis.model.id.BrojCasopisaId;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "broj_casopisa")
public class BrojCasopisa {

    @EmbeddedId
    private BrojCasopisaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("issn")
    @JoinColumn(name = "issn")
    private ElektronskiCasopis elektronskiCasopis;

    @Column(name = "volumen_bc")
    private Integer volumen;

    @Column(name = "datum_izdavanja_bc")
    private LocalDate datumIzdavanja;

    @Column(name = "putanja_dokumenta_bc")
    private String putanjaDokumenta;

    @OneToMany(mappedBy = "brojCasopisa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PristupBrojuCasopisa> pristupi = new ArrayList<>();

    public BrojCasopisa() {
    }

    public BrojCasopisa(BrojCasopisaId id, ElektronskiCasopis elektronskiCasopis, Integer volumen, LocalDate datumIzdavanja, String putanjaDokumenta) {
        this.id = id;
        this.elektronskiCasopis = elektronskiCasopis;
        this.volumen = volumen;
        this.datumIzdavanja = datumIzdavanja;
        this.putanjaDokumenta = putanjaDokumenta;
    }

    public BrojCasopisaId getId() {
        return id;
    }

    public void setId(BrojCasopisaId id) {
        this.id = id;
    }

    public ElektronskiCasopis getElektronskiCasopis() {
        return elektronskiCasopis;
    }

    public void setElektronskiCasopis(ElektronskiCasopis elektronskiCasopis) {
        this.elektronskiCasopis = elektronskiCasopis;
    }

    public Integer getVolumen() {
        return volumen;
    }

    public void setVolumen(Integer volumen) {
        this.volumen = volumen;
    }

    public LocalDate getDatumIzdavanja() {
        return datumIzdavanja;
    }

    public void setDatumIzdavanja(LocalDate datumIzdavanja) {
        this.datumIzdavanja = datumIzdavanja;
    }

    public String getPutanjaDokumenta() {
        return putanjaDokumenta;
    }

    public void setPutanjaDokumenta(String putanjaDokumenta) {
        this.putanjaDokumenta = putanjaDokumenta;
    }

    public List<PristupBrojuCasopisa> getPristupi() {
        return pristupi;
    }

    public void setPristupi(List<PristupBrojuCasopisa> pristupi) {
        this.pristupi = pristupi;
    }
}
