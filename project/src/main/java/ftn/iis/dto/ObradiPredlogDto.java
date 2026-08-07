package ftn.iis.dto;

import jakarta.validation.constraints.NotBlank;

public class ObradiPredlogDto {
    @NotBlank(message = "Status je obavezan.")
    private String status;
    private String obrazlozenje;      // obavezno ako ODBIJENO_BIBLIOTEKAR
    private Long zanrId;              // obavezno ako ODOBRENO_BIBLIOTEKAR
    private Double okvirnaCena;       // obavezno ako ODOBRENO_BIBLIOTEKAR

    public ObradiPredlogDto() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObrazlozenje() {
        return obrazlozenje;
    }

    public void setObrazlozenje(String obrazlozenje) {
        this.obrazlozenje = obrazlozenje;
    }

    public Double getOkvirnaCena() {
        return okvirnaCena;
    }

    public void setOkvirnaCena(Double okvirnaCena) {
        this.okvirnaCena = okvirnaCena;
    }

    public Long getZanrId() {
        return zanrId;
    }

    public void setZanrId(Long zanrId) {
        this.zanrId = zanrId;
    }
}
