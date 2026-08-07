package ftn.iis.exception;

public class ZanrNotFoundException extends RuntimeException {
    public ZanrNotFoundException(Long zanrId) {
        super("Žanr sa id-jem " + zanrId + " ne postoji.");
    }
}
