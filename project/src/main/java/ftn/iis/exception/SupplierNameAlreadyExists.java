package ftn.iis.exception;

public class SupplierNameAlreadyExists extends RuntimeException {
    public SupplierNameAlreadyExists() {super("Dobavljač sa unetim nazivom već postoji u sistemu.");}
}
