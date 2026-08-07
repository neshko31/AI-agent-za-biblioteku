package ftn.iis.marc.dto;

import java.util.Map;

public class MarcFieldDto {

    private String tag;
    private String indicator1 = " ";
    private String indicator2 = " ";
    private Map<String, String> subfields;

    public MarcFieldDto() {
    }

    public MarcFieldDto(String tag, Map<String, String> subfields) {
        this.tag = tag;
        this.subfields = subfields;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getIndicator1() {
        return indicator1;
    }

    public void setIndicator1(String indicator1) {
        this.indicator1 = indicator1;
    }

    public String getIndicator2() {
        return indicator2;
    }

    public void setIndicator2(String indicator2) {
        this.indicator2 = indicator2;
    }

    public Map<String, String> getSubfields() {
        return subfields;
    }

    public void setSubfields(Map<String, String> subfields) {
        this.subfields = subfields;
    }
}
