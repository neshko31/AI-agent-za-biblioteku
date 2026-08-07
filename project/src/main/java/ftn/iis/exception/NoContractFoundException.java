package ftn.iis.exception;

public class NoContractFoundException extends  RuntimeException{
    public NoContractFoundException(){super("Traženi ugovor ne postoji.");}
}
