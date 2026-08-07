package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Elasticsearch document model for a book in the "books" index.
 */
@Setter
@Getter
@Document(indexName = "books")
public class Book {
    @Id
    private String bookId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String series;

    @Field(type = FieldType.Keyword)
    private String author;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String language;

    @Field(type = FieldType.Keyword)
    private String isbn;

    @Field(type = FieldType.Keyword)
    private List<String> genres;

    @Field(type = FieldType.Keyword)
    private List<String> characters;

    @Field(type = FieldType.Keyword)
    private String bookForm;

    @Field(type = FieldType.Keyword)
    private String edition;

    @Field(type = FieldType.Integer)
    private Integer pages;

    @Field(type = FieldType.Keyword)
    private String publisher;

    @Field(type = FieldType.Keyword)
    private String publishDate;

    @Field(type = FieldType.Keyword)
    private String firstPublishDate;

    @Field(type = FieldType.Keyword)
    private List<String> awards;

    @Field(type = FieldType.Integer)
    private Integer numRatings;

    @Field(type = FieldType.Integer)
    private List<Integer> ratingsByStars;

    @Field(type = FieldType.Double)
    private Double likedPercent;

    @Field(type = FieldType.Keyword)
    private List<String> setting;

    @Field(type = FieldType.Keyword)
    private String coverImg;

    @Field(type = FieldType.Double)
    private Double bbeScore;

    @Field(type = FieldType.Integer)
    private Integer bbeVotes;

    @Field(type = FieldType.Double)
    private Double price;

    public Book(String bookId, String title, String series, String author, Double rating, String description, String language, String isbn, List<String> genres, List<String> characters, String bookForm, String edition, Integer pages, String publisher, String publishDate, String firstPublishDate, List<String> awards, Integer numRatings, List<Integer> ratingsByStars, Double likedPercent, List<String> setting, String coverImg, Double bbeScore, Integer bbeVotes, Double price) {
        this.bookId = bookId;
        this.title = title;
        this.series = series;
        this.author = author;
        this.rating = rating;
        this.description = description;
        this.language = language;
        this.isbn = isbn;
        this.genres = genres;
        this.characters = characters;
        this.bookForm = bookForm;
        this.edition = edition;
        this.pages = pages;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.firstPublishDate = firstPublishDate;
        this.awards = awards;
        this.numRatings = numRatings;
        this.ratingsByStars = ratingsByStars;
        this.likedPercent = likedPercent;
        this.setting = setting;
        this.coverImg = coverImg;
        this.bbeScore = bbeScore;
        this.bbeVotes = bbeVotes;
        this.price = price;
    }

    public Book() {
    }
}
