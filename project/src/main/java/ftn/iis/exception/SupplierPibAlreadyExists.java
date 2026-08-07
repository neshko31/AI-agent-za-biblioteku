package ftn.iis.exception;

public class SupplierPibAlreadyExists extends RuntimeException {
    public SupplierPibAlreadyExists() {super("Dobavljač sa unetim PIB-om već postoji u sistemu.");}
}
