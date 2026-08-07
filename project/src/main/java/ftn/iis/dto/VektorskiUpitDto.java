package ftn.iis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VektorskiUpitDto {
    @JsonProperty("message")
    private String poruka;

    @JsonProperty("image_base64")
    private String imageBase64;

    public VektorskiUpitDto(String poruka, String imageBase64) {
        this.poruka = poruka;
        this.imageBase64 = imageBase64;
    }

    public VektorskiUpitDto(String poruka) {
        this.poruka = poruka;
    }

    public VektorskiUpitDto() {
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
