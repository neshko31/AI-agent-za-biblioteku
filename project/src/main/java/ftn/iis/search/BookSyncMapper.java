package ftn.iis.search;

import ftn.iis.model.Knjiga;
import ftn.iis.search.dto.SearchBookDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BookSyncMapper {

    public SearchBookDto toSearchDto(Knjiga knjiga) {
        SearchBookDto dto = new SearchBookDto();
        dto.setIsbns(List.of(knjiga.getIsbn()));
        dto.setTitle(knjiga.getNaslov());
        dto.setAuthors(knjiga.getAutor() != null ? List.of(knjiga.getAutor()) : Collections.emptyList());
        dto.setDescription(knjiga.getSinopsis());
        if (knjiga.geteKnjiga() != null) {
            dto.setNumberOfPages(knjiga.geteKnjiga().getBrojStranaEK());
        }
        return dto;
    }
}
