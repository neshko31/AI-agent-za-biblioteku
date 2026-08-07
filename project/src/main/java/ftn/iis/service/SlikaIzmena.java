package ftn.iis.service;

/**
 * Enkapsulira nameru korisnika u vezi sa slikom prilikom editovanja poruke.
 * Postoje tri moguća, međusobno isključiva stanja:
 *
 *   - novaSlikaBase64 != null  -> slika se zamenjuje novom (bez obzira na to
 *                                 da li je poruka ranije imala sliku)
 *   - ukloniSliku == true      -> postojeća slika (ako je ima) se briše
 *   - ni jedno ni drugo        -> stara slika (ako postoji) ostaje
 *                                 nepromenjena na novoj grani
 *
 * Kontroler je zadužen da spreči nemoguću kombinaciju (novaSlikaBase64 != null
 * i ukloniSliku == true istovremeno) pre nego što ovaj objekat i nastane.
 */
public class SlikaIzmena {
    private final String novaSlikaBase64;
    private final boolean ukloniSliku;

    public SlikaIzmena(String novaSlikaBase64, boolean ukloniSliku) {
        this.novaSlikaBase64 = novaSlikaBase64;
        this.ukloniSliku = ukloniSliku;
    }

    /** Bez izmene slike - stara slika (ako postoji) se prenosi nepromenjena. */
    public static SlikaIzmena nepromenjena() {
        return new SlikaIzmena(null, false);
    }

    public String getNovaSlikaBase64() {
        return novaSlikaBase64;
    }

    public boolean isUkloniSliku() {
        return ukloniSliku;
    }

    public boolean imaNovuSliku() {
        return novaSlikaBase64 != null;
    }

    /**
     * Vraća sliku koju treba upisati na novu (izmenjenu) poruku, polazeći od
     * postojeće slike originalne poruke koja se edituje.
     */
    public String resolveZa(String postojecaSlikaBase64) {
        if (imaNovuSliku()) {
            return novaSlikaBase64;
        }
        if (ukloniSliku) {
            return null;
        }
        return postojecaSlikaBase64;
    }
}