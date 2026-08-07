package nais.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nais.search.enums.Format;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDate;
import java.util.List;

public class BookDto {

    @NotNull
    @Size(min = 1)
    @Length(min = 13, max = 13)
    private List<String> isbns;

    @NotNull
    private String title;

    private String originalTitle;

    private String subtitle;

    private List<String> authors;

    private List<String> genres;

    private String language;

    private List<String> translators;

    private List<String> illustrators;

    private List<String> introductions;

    private List<String> afterwords;

    private String publisher;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate publishDate;

    private Format format;

    private String series;

    private String textExcerpt;

    private String description;

    private Integer numberOfPages;

    private List<String> awards;

    private List<String> setting;

    private List<String> characters;

    public BookDto() {}

    public BookDto(List<String> isbns, String title, String originalTitle, String subtitle,
                   List<String> authors, List<String> genres, String language,
                   List<String> translators, List<String> illustrators, List<String> introductions,
                   List<String> afterwords, String publisher, LocalDate publishDate, Format format,
                   String series, String textExcerpt, String description, Integer numberOfPages,
                   List<String> awards, List<String> setting, List<String> characters) {
        this.isbns = isbns;
        this.title = title;
        this.originalTitle = originalTitle;
        this.subtitle = subtitle;
        this.authors = authors;
        this.genres = genres;
        this.language = language;
        this.translators = translators;
        this.illustrators = illustrators;
        this.introductions = introductions;
        this.afterwords = afterwords;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.format = format;
        this.series = series;
        this.textExcerpt = textExcerpt;
        this.description = description;
        this.numberOfPages = numberOfPages;
        this.awards = awards;
        this.setting = setting;
        this.characters = characters;
    }

    public List<String> getIsbns() { return isbns; }
    public void setIsbns(List<String> isbns) { this.isbns = isbns; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getTranslators() { return translators; }
    public void setTranslators(List<String> translators) { this.translators = translators; }

    public List<String> getIllustrators() { return illustrators; }
    public void setIllustrators(List<String> illustrators) { this.illustrators = illustrators; }

    public List<String> getIntroductions() { return introductions; }
    public void setIntroductions(List<String> introductions) { this.introductions = introductions; }

    public List<String> getAfterwords() { return afterwords; }
    public void setAfterwords(List<String> afterwords) { this.afterwords = afterwords; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

    public Format getFormat() { return format; }
    public void setFormat(Format format) { this.format = format; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public String getTextExcerpt() { return textExcerpt; }
    public void setTextExcerpt(String textExcerpt) { this.textExcerpt = textExcerpt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getNumberOfPages() { return numberOfPages; }
    public void setNumberOfPages(Integer numberOfPages) { this.numberOfPages = numberOfPages; }

    public List<String> getAwards() { return awards; }
    public void setAwards(List<String> awards) { this.awards = awards; }

    public List<String> getSetting() { return setting; }
    public void setSetting(List<String> setting) { this.setting = setting; }

    public List<String> getCharacters() { return characters; }
    public void setCharacters(List<String> characters) { this.characters = characters; }
}
