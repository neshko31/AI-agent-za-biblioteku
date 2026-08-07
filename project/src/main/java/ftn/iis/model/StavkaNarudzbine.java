package ftn.iis.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stavka_narudzbine")
public class StavkaNarudzbine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "narudzbina_id", nullable = false)
    private Narudzbina narudzbina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", nullable = true) // knjiga nmr postojati u bp
    private FizickaKnjiga fizickaKnjiga;

    @Column(name = "kolicina", nullable = false)
    private Integer kolicina;

    @Column(name = "cena_po_komadu", nullable = false)
    private Double cenaPoKomadu;                        // okvirna cena sa popustom iz ugovora

    @Column(name = "ukupna_cena_stavke", nullable = false)
    private Double ukupnaCenaStavke;                    // kolicina * cenaPoKomadu

    @Column(name = "predlog_id")
    private Long predlogId;

    @Column(name = "preporuka_id")
    private Long preporukaId;

    // quick fix uveden za slucaj da je isbn == null
    @Column(name = "naslov")
    private String naslov;

    @Column(name = "autor")
    private String autor;

    public StavkaNarudzbine() {}

    // konsruktor koji ce se koristiti za sistemske preporuke i one korisnicke predloge
    // knjiga koje vec postoje u bazi
    public StavkaNarudzbine(Narudzbina narudzbina, FizickaKnjiga fizickaKnjiga, Integer kolicina,
                            Double okvirnaCena, Double popustProcenat) {
        this.narudzbina = narudzbina;
        this.fizickaKnjiga = fizickaKnjiga;
        this.kolicina = kolicina;
        this.cenaPoKomadu = okvirnaCena * (1 - popustProcenat / 100.0);     // ovde primenjujem popust
        this.ukupnaCenaStavke = this.cenaPoKomadu * kolicina;
        this.autor = fizickaKnjiga.getKnjiga().getAutor();
        this.naslov = fizickaKnjiga.getKnjiga().getNaslov();
    }

    // konstruktor koji ce se koristiti za one predloge knjiga koje ne postoje u bazi
    public StavkaNarudzbine(Narudzbina narudzbina, String naslov, String autor,
                            Long predlogId, Integer kolicina,
                            Double okvirnaCena, Double popustProcenat) {
        this.narudzbina = narudzbina;
        this.fizickaKnjiga = null;
        this.naslov = naslov;
        this.autor  = autor;
        this.predlogId = predlogId;
        this.kolicina = kolicina;
        this.cenaPoKomadu = okvirnaCena * (1 - popustProcenat / 100.0);
        this.ukupnaCenaStavke = this.cenaPoKomadu * kolicina;
    }

}