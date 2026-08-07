package ftn.iis.exception;

public class NonManagerUpdatingSupplierException extends RuntimeException{
    public NonManagerUpdatingSupplierException() { super("Samo menadzeri smeju da menjaju dobavljace."); }
}
