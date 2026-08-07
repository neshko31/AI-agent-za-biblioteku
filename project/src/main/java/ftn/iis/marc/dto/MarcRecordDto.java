package ftn.iis.marc.dto;

import java.util.List;

public class MarcRecordDto {

    private String leader;
    private List<MarcFieldDto> fields;

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public List<MarcFieldDto> getFields() {
        return fields;
    }

    public void setFields(List<MarcFieldDto> fields) {
        this.fields = fields;
    }
}
