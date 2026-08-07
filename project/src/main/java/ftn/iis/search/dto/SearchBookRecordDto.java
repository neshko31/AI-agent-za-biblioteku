package ftn.iis.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchBookRecordDto {

    private String recordId;
    private String textExcerpt;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getTextExcerpt() {
        return textExcerpt;
    }

    public void setTextExcerpt(String textExcerpt) {
        this.textExcerpt = textExcerpt;
    }
}
