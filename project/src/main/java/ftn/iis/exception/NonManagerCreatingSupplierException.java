package ftn.iis.exception;

public class NonManagerCreatingSupplierException extends RuntimeException{
    public NonManagerCreatingSupplierException() { super("Samo menadzeri smeju da unose dobavljace."); }
}
