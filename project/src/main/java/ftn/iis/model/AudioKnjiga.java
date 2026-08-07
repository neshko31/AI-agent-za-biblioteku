package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "audio_knjiga")
public class AudioKnjiga {
    @Id
    private String isbn;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "isbn")
    @JsonIgnore
    private Knjiga knjiga;

    @Column(name = "trajanje_sek_ak")
    private Integer trajanjeSekundeAK;

    @Column(name = "format_ak", length = 20)
    private String formatAK;

    @Column(name = "datum_dodavanja_ak")
    private LocalDate datumDodavanjaAK;

    @Column(name = "putanja_ak")
    private String putanjaAK;

    @OneToMany(mappedBy = "audioKnjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SlusanjeAudioKnjige> slušanja = new ArrayList<>();

    public AudioKnjiga() {
    }

    public AudioKnjiga(Knjiga knjiga, Integer trajanjeSekundeAK, String formatAK, LocalDate datumDodavanjaAK, String putanjaAK) {
        this.knjiga = knjiga;
        this.trajanjeSekundeAK = trajanjeSekundeAK;
        this.formatAK = formatAK;
        this.datumDodavanjaAK = datumDodavanjaAK;
        this.putanjaAK = putanjaAK;
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

    public Integer getTrajanjeSekundeAK() {
        return trajanjeSekundeAK;
    }

    public void setTrajanjeSekundeAK(Integer trajanjeSekundeAK) {
        this.trajanjeSekundeAK = trajanjeSekundeAK;
    }

    public String getFormatAK() {
        return formatAK;
    }

    public void setFormatAK(String formatAK) {
        this.formatAK = formatAK;
    }

    public LocalDate getDatumDodavanjaAK() {
        return datumDodavanjaAK;
    }

    public void setDatumDodavanjaAK(LocalDate datumDodavanjaAK) {
        this.datumDodavanjaAK = datumDodavanjaAK;
    }

    public String getPutanjaAK() {
        return putanjaAK;
    }

    public void setPutanjaAK(String putanjaAK) {
        this.putanjaAK = putanjaAK;
    }
}
