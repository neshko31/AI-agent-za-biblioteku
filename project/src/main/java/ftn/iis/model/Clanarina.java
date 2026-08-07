package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ftn.iis.enums.NacinUplate;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name = "clanarina")
public class Clanarina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idc")
    private Long idc;

    @Column(name = "datup", nullable = false)
    private LocalDate datUplate;
    /*
    * ako je mesecna onda je datUplate + 30 dana
    * ako je godisnja onda je datUplate + godina
    * */
    @Column(name = "datisteka", nullable = false)
    private LocalDate datIsteka;

    @Column(name = "datbris", nullable = false)
    private LocalDate datBrisanja;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "nacin_uplate", nullable = false)
    private NacinUplate nacinUplate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jmbg", nullable = false)
    @JsonManagedReference("user-clan")
    @JsonIgnore
    private User user;

    public Clanarina() {
    }

    public Long getIdc() {
        return idc;
    }

    public void setIdc(Long idc) {
        this.idc = idc;
    }

    public LocalDate getDatUplate() {
        return datUplate;
    }

    public void setDatUplate(LocalDate datUplate) {
        this.datUplate = datUplate;
    }

    public LocalDate getDatIsteka() {
        return datIsteka;
    }

    public void setDatIsteka(LocalDate datIsteka) {
        this.datIsteka = datIsteka;
    }

    public LocalDate getDatBrisanja() {
        return datBrisanja;
    }

    public void setDatBrisanja(LocalDate datBrisanja) {
        this.datBrisanja = datBrisanja;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public NacinUplate getNacinUplate() {
        return nacinUplate;
    }

    public void setNacinUplate(NacinUplate nacinUplate) {
        this.nacinUplate = nacinUplate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
