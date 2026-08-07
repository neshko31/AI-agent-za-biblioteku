package ftn.iis.exception;

public class SupplierPhoneAlreadyExists extends RuntimeException{
    public SupplierPhoneAlreadyExists() {super("Dobavljač sa unetim telefonom već postoji u sistemu.");}
}
