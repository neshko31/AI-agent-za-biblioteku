package ftn.iis.model;

import ftn.iis.enums.TipKC;
import jakarta.persistence.*;

@Entity
public class KategorijaClana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idkc")
    private Long idkc;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_kc", nullable = false)
    private TipKC tipKC;

    @Column(name = "cena_kc", nullable = false, precision = 10, scale = 2)
    private Long cenaKC;

    public Long getIdkc() {
        return idkc;
    }

    public void setIdkc(Long idkc) {
        this.idkc = idkc;
    }

    public TipKC getTipKC() {
        return tipKC;
    }

    public void setTipKC(TipKC tipKC) {
        this.tipKC = tipKC;
    }

    public Long getCenaKC() {
        return cenaKC;
    }

    public void setCenaKC(Long cenaKC) {
        this.cenaKC = cenaKC;
    }
}
