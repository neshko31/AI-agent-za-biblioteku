package ftn.iis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "knjizara")
public class Knjizara {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Dobavljac dobavljac;

    @Column(name="url_sajta")
    private String urlOnlineProdavnice;

    public Knjizara(){}

    public Knjizara(Dobavljac dobavljac){
        this.dobavljac = dobavljac;
    }

    public void setUrlOnlineProdavnice(String urlOnlineProdavnice) {
        this.urlOnlineProdavnice = urlOnlineProdavnice;
    }

    public String getUrlOnlineProdavnice() {
        return urlOnlineProdavnice;
    }
}
