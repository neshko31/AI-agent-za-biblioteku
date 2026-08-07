package ftn.iis.dto;

public class BudzetPoZanruResponseDto {
    private Long id;
    private Long zanrId;
    private String zanrNaziv;
    private Double ukupanBudzet;
    private Double potroseno;
    private Double dostupno;
    private Double rezervisano;
    private Long budzetId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRezervisano(Double rezervisano) {
        this.rezervisano = rezervisano;
    }

    public Double getRezervisano() {
        return rezervisano;
    }

    public Long getBudzetId() {
        return budzetId;
    }

    public void setBudzetId(Long budzetId) {
        this.budzetId = budzetId;
    }

    public Long getZanrId() {
        return zanrId;
    }

    public void setZanrId(Long zanrId) {
        this.zanrId = zanrId;
    }

    public String getZanrNaziv() {
        return zanrNaziv;
    }

    public void setZanrNaziv(String zanrNaziv) {
        this.zanrNaziv = zanrNaziv;
    }

    public Double getUkupanBudzet() {
        return ukupanBudzet;
    }

    public void setUkupanBudzet(Double ukupanBudzet) {
        this.ukupanBudzet = ukupanBudzet;
    }

    public Double getPotroseno() {
        return potroseno;
    }

    public void setPotroseno(Double potroseno) {
        this.potroseno = potroseno;
    }

    public Double getDostupno() {
        return dostupno;
    }

    public void setDostupno(Double dostupno) {
        this.dostupno = dostupno;
    }

}