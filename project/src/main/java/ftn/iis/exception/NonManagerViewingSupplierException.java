package ftn.iis.exception;

public class NonManagerViewingSupplierException extends RuntimeException{
    public NonManagerViewingSupplierException() { super("Samo menadzeri smeju da pregledaju dobavljace.") ;}
}
