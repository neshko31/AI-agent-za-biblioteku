package ftn.iis.dto;

import ftn.iis.model.Komentar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class KomentarResponseDto {
    private Long id;
    private String tekstK;
    private LocalDateTime datumKreiranjaK;
    private String autorJmbg;
    private String autorIme;
    private int brojLajkova;
    private boolean lajkovaoTrenutniKorisnik;
    private Long odgovorNaId;
    private List<KomentarResponseDto> odgovori;

    public KomentarResponseDto(Long id, String tekstK, LocalDateTime datumKreiranjaK, String autorJmbg, String autorIme, int brojLajkova, boolean lajkovaoTrenutniKorisnik, Long odgovorNaId, List<KomentarResponseDto> odgovori) {
        this.id = id;
        this.tekstK = tekstK;
        this.datumKreiranjaK = datumKreiranjaK;
        this.autorJmbg = autorJmbg;
        this.autorIme = autorIme;
        this.brojLajkova = brojLajkova;
        this.lajkovaoTrenutniKorisnik = lajkovaoTrenutniKorisnik;
        this.odgovorNaId = odgovorNaId;
        this.odgovori = odgovori;
    }

    public KomentarResponseDto() {
    }

    public static KomentarResponseDto fromKomentar(Komentar k, String trenutniJmbg) {
        KomentarResponseDto dto = new KomentarResponseDto();
        dto.setId(k.getId());
        dto.setTekstK(k.getTekstK());
        dto.setDatumKreiranjaK(k.getDatumKreiranjaK());
        dto.setAutorJmbg(k.getClan().getJmbg());
        dto.setAutorIme(k.getClan().getFirstName() + " " + k.getClan().getLastName());
        dto.setBrojLajkova(k.getLajkovali().size());
        dto.setLajkovaoTrenutniKorisnik(
                k.getLajkovali().stream().anyMatch(u -> u.getJmbg().equals(trenutniJmbg))
        );
        dto.setOdgovori(
                k.getOdgovori().stream()
                        .map(o -> KomentarResponseDto.fromKomentar(o, trenutniJmbg))
                        .collect(Collectors.toList())
        );
        dto.setOdgovorNaId(k.getOdgovorNa() != null ? k.getOdgovorNa().getId() : null);  // ← i ovo
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTekstK() {
        return tekstK;
    }

    public void setTekstK(String tekstK) {
        this.tekstK = tekstK;
    }

    public LocalDateTime getDatumKreiranjaK() {
        return datumKreiranjaK;
    }

    public void setDatumKreiranjaK(LocalDateTime datumKreiranjaK) {
        this.datumKreiranjaK = datumKreiranjaK;
    }

    public String getAutorJmbg() {
        return autorJmbg;
    }

    public void setAutorJmbg(String autorJmbg) {
        this.autorJmbg = autorJmbg;
    }

    public String getAutorIme() {
        return autorIme;
    }

    public void setAutorIme(String autorIme) {
        this.autorIme = autorIme;
    }

    public int getBrojLajkova() {
        return brojLajkova;
    }

    public void setBrojLajkova(int brojLajkova) {
        this.brojLajkova = brojLajkova;
    }

    public boolean isLajkovaoTrenutniKorisnik() {
        return lajkovaoTrenutniKorisnik;
    }

    public void setLajkovaoTrenutniKorisnik(boolean lajkovaoTrenutniKorisnik) {
        this.lajkovaoTrenutniKorisnik = lajkovaoTrenutniKorisnik;
    }

    public List<KomentarResponseDto> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(List<KomentarResponseDto> odgovori) {
        this.odgovori = odgovori;
    }

    public Long getOdgovorNaId() {
        return odgovorNaId;
    }

    public void setOdgovorNaId(Long odgovorNaId) {
        this.odgovorNaId = odgovorNaId;
    }
}
