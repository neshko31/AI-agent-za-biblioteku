package ftn.iis.exception;

public class NonManagerDeletingSupplierException extends RuntimeException{
    public NonManagerDeletingSupplierException() { super("Samo menadzeri smeju da brisu dobavljace."); }
}
