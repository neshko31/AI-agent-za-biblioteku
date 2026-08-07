package nais.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import nais.search.enums.Role;

import java.time.LocalDate;
import java.util.List;

public class PersonDto {

    private String firstName;
    private String lastName;
    private String placeOfBirth;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfDeath;

    private Role role;
    private List<String> genres;
    private List<String> books;
    private String bio;

    public PersonDto() {}

    public PersonDto(String firstName, String lastName, String placeOfBirth,
                     LocalDate dateOfBirth, LocalDate dateOfDeath, Role role,
                     List<String> genres, List<String> books, String bio) {
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

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDate getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(LocalDate dateOfDeath) { this.dateOfDeath = dateOfDeath; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public List<String> getBooks() { return books; }
    public void setBooks(List<String> books) { this.books = books; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}