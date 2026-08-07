package ftn.iis.exception;

public class SupplierEmailAlreadyExists extends RuntimeException{
    public SupplierEmailAlreadyExists() { super("Dobavljač sa unetom email adresom već postoji u sistemu.");}
}
