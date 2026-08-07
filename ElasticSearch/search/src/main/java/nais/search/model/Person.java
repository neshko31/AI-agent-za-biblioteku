package nais.search.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import nais.search.dto.PersonDto;
import nais.search.enums.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.List;

@Document(indexName="persons")
public class Person {

    @Id
    private String personId;

    @Field(type = FieldType.Keyword)
    private String firstName;

    @Field(type = FieldType.Keyword)
    private String lastName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String placeOfBirth;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dateOfDeath;

    @Field(type = FieldType.Keyword)
    private Role role;

    @Field(type = FieldType.Keyword)
    private List<String> genres;

    @Field(type = FieldType.Keyword)
    private List<String> books;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String bio;

    public Person(String personId, String firstName, String lastName, String placeOfBirth, LocalDate dateOfBirth, LocalDate dateOfDeath, Role role, List<String> genres, List<String> books, String bio) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.placeOfBirth = placeOfBirth;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        this.role = role;
        this.genres = genres;
        this.books = books;
        this.bio = bio;
    }

    public Person() {
    }

    public Person(PersonDto dto) {
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
        this.placeOfBirth = dto.getPlaceOfBirth();
        this.dateOfBirth = dto.getDateOfBirth();
        this.dateOfDeath = dto.getDateOfDeath();
        this.role = dto.getRole();
        this.genres = dto.getGenres();
        this.books = dto.getBooks();
        this.bio = dto.getBio();
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(LocalDate dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getBooks() {
        return books;
    }

    public void setBooks(List<String> books) {
        this.books = books;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
