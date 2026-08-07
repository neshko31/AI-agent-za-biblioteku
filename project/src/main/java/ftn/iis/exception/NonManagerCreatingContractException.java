package ftn.iis.exception;

public class NonManagerCreatingContractException extends RuntimeException{
    public NonManagerCreatingContractException() { super("Samo menadzeri smeju da kreiraju ugovore.") ;}
}
