package ftn.iis.model;

import ftn.iis.enums.StatusReklamacije;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "reklamacija")
public class Reklamacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "narudzbina_id", nullable = false, unique = true)
    private Narudzbina narudzbina;

    @Column(name = "datum_podnosenja", nullable = false)
    private LocalDate datumPodnosenja;

    @Column(name = "razlog", nullable = false)
    private String razlog;

    @Column(name = "napomena")
    private String napomena;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusReklamacije status;

    @Column(name = "datum_zatvaranja")
    private LocalDate datumZatvaranja;

    public Reklamacija() {}

    public Reklamacija(Narudzbina narudzbina, String razlog, String napomena) {
        this.narudzbina = narudzbina;
        this.razlog = razlog;
        this.napomena = napomena;
        this.datumPodnosenja = LocalDate.now();
        this.status = StatusReklamacije.OTVORENA;
    }
}