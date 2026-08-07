package ftn.iis.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class PristupBrojuCasopisaId implements Serializable {

    @Column(name = "jmbg_clana", length = 13)
    private String jmbgClana;

    @Column(name = "issn_casopisa", length = 8)
    private String issn;

    @Column(name = "broj_izdanja")
    private Integer brojIzdanja;

    @Column(name = "datum_pristupanja_pbc")
    private LocalDate datumPristupanja;

    public PristupBrojuCasopisaId() {
    }

    public PristupBrojuCasopisaId(String jmbgClana, String issn, Integer brojIzdanja, LocalDate datumPristupanja) {
        this.jmbgClana = jmbgClana;
        this.issn = issn;
        this.brojIzdanja = brojIzdanja;
        this.datumPristupanja = datumPristupanja;
    }

    public String getJmbgClana() {
        return jmbgClana;
    }

    public void setJmbgClana(String jmbgClana) {
        this.jmbgClana = jmbgClana;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public Integer getBrojIzd() {
        return brojIzdanja;
    }

    public void setBrojIzd(Integer brojIzdanja) {
        this.brojIzdanja = brojIzdanja;
    }

    public LocalDate getDatumPristupanja() {
        return datumPristupanja;
    }

    public void setDatumPristupanja(LocalDate datumPristupanja) {
        this.datumPristupanja = datumPristupanja;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PristupBrojuCasopisaId that)) return false;
        return Objects.equals(jmbgClana, that.jmbgClana) && Objects.equals(issn, that.issn) && Objects.equals(brojIzdanja, that.brojIzdanja) && Objects.equals(datumPristupanja, that.datumPristupanja);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jmbgClana, issn, brojIzdanja, datumPristupanja);
    }
}