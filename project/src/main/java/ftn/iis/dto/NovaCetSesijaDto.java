package ftn.iis.dto;

import ftn.iis.enums.TipAgentaCS;


public class NovaCetSesijaDto {
    private TipAgentaCS tipAgentaCS;
    private String sadrzajPoruke;

    public NovaCetSesijaDto(TipAgentaCS tipAgentaCS, String sadrzajPoruke) {
        this.tipAgentaCS = tipAgentaCS;
        this.sadrzajPoruke = sadrzajPoruke;
    }

    public NovaCetSesijaDto() {
    }

    public TipAgentaCS getTipAgentaCS() {
        return tipAgentaCS;
    }

    public void setTipAgentaCS(TipAgentaCS tipAgentaCS) {
        this.tipAgentaCS = tipAgentaCS;
    }

    public String getSadrzajPoruke() {
        return sadrzajPoruke;
    }

    public void setSadrzajPoruke(String sadrzajPoruke) {
        this.sadrzajPoruke = sadrzajPoruke;
    }
}
