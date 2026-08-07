package ftn.iis.dto;

import ftn.iis.enums.NacinUplate;
import jakarta.validation.constraints.NotNull;

public class Step3R {
    @NotNull
    private NacinUplate nacinUplate;

    //za online uplatu
    private String brojKartice;
    private String datumVazenja;
    private String cvv;

    public NacinUplate getNacinUplate() {
        return nacinUplate;
    }

    public void setNacinUplate(NacinUplate nacinUplate) {
        this.nacinUplate = nacinUplate;
    }

    public String getBrojKartice() {
        return brojKartice;
    }

    public void setBrojKartice(String brojKartice) {
        this.brojKartice = brojKartice;
    }

    public String getDatumVazenja() {
        return datumVazenja;
    }

    public void setDatumVazenja(String datumVazenja) {
        this.datumVazenja = datumVazenja;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
