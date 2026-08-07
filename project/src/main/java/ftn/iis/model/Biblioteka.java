package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "biblioteka")
public class Biblioteka {

    @Id
    @Column(name = "bid", length = 20)
    private String bid;

    @Column(nullable = false)
    private String name;

    @Column(name = "ziro_rb", nullable = false)
    private String ziroRB;

    @OneToMany(mappedBy = "biblioteka", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> members;

    @OneToMany(mappedBy = "biblioteka")
    @JsonIgnore
    private List<Katalog> catalogs = new ArrayList<>();

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZiroRB() {
        return ziroRB;
    }

    public void setZiroRB(String ziroRB) {
        this.ziroRB = ziroRB;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<Katalog> getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(List<Katalog> catalogs) {
        this.catalogs = catalogs;
    }
}
