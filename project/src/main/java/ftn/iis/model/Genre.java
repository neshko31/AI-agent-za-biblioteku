package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "zanr")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "zanr_id")
    private Long id;

    @Column(name = "zanr_name", nullable = false, unique = true)
    private String name;

    // Koristim JsonManagement jer sprecava beskonacnu petlju kod jsona
    @OneToOne(mappedBy = "zanr", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("zanr-budzet")
    private BudzetPoZanru budzetPoZanru;

    @OneToMany(mappedBy = "zanr")
    private List<Knjiga> knjige;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BudzetPoZanru getBudzetPoZanru() {
        return budzetPoZanru;
    }
    public void setBudzetPoZanru(BudzetPoZanru budzetPoZanru) {
        this.budzetPoZanru = budzetPoZanru;
    }

}
