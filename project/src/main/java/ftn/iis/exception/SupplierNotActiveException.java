package ftn.iis.exception;

public class SupplierNotActiveException extends RuntimeException {
    public SupplierNotActiveException() {super("Nije moguće sklapati ugovor sa neaktivnim dobavljačem.");}
}
