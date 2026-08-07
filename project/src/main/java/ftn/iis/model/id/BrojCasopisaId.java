package ftn.iis.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BrojCasopisaId implements Serializable {

    @Column(name = "issn", length = 8)
    private String issn;

    @Column(name = "broj_izdanja")
    private Integer brojIzdanja;

    public BrojCasopisaId() {
    }

    public BrojCasopisaId(String issn, Integer brojIzd) {
        this.issn = issn;
        this.brojIzdanja = brojIzd;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public Integer getBrojIzdanja() {
        return brojIzdanja;
    }

    public void setBrojIzdanja(Integer brojIzdanja) {
        this.brojIzdanja = brojIzdanja;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrojCasopisaId that)) return false;
        return Objects.equals(issn, that.issn) && Objects.equals(brojIzdanja, that.brojIzdanja);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issn, brojIzdanja);
    }
}