package ftn.iis.model;

import ftn.iis.enums.StatusDobavljaca;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="dobavljac")
public class Dobavljac {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="naziv")
    private String naziv;
    @Column(name="email")
    private String email;
    @Column(name="telefon")
    private String tel;
    @Column(name="pib")
    private String pib;

    @Enumerated(EnumType.STRING)
    @Column(name= "status")
    private StatusDobavljaca status;

    @OneToOne(mappedBy = "dobavljac", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Knjizara knjizara;

    @OneToOne(mappedBy = "dobavljac", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Izdavac izdavac;

    @OneToMany(mappedBy = "dobavljac", cascade = CascadeType.ALL)
    private List<Ugovor> ugovori = new ArrayList<>();

    public Dobavljac(){

    }

    public Dobavljac(String naziv, String email, String tel, String pib){
        this.naziv = naziv;
        this.email = email;
        this.tel = tel;
        this.pib = pib;
        this.status = StatusDobavljaca.AKTIVAN; // pri kreiranju je aktivan
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPib() {
        return pib;
    }

    public void setPib(String pib) {
        this.pib = pib;
    }

    public Knjizara getKnjizara() {
        return knjizara;
    }

    public void setKnjizara(Knjizara knjizara) {
        this.knjizara = knjizara;
    }

    public Izdavac getIzdavac() {
        return izdavac;
    }

    public void setIzdavac(Izdavac izdavac) {
        this.izdavac = izdavac;
    }

    public void setStatus(StatusDobavljaca status) {
        this.status = status;
    }

    public StatusDobavljaca getStatus() {
        return status;
    }
}
