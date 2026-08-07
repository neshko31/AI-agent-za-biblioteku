package ftn.iis.exception;

public class ActiveContractAlreadyExistsException extends RuntimeException{
    public ActiveContractAlreadyExistsException() { super("Trazeni dobavljac vec ima ugovor koji je aktivan.");}
}
