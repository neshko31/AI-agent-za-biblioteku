package ftn.iis.exception;

public class MaksimalnaDubinaKomentaraException extends RuntimeException {
    public MaksimalnaDubinaKomentaraException() {
        super("Dostignut je maksimalan broj nivoa ugnjezdavanja komentara. Dodavanje odgovora na komentar nije moguce!");
    }
}
