package ftn.iis.exception;

public class ContractNotActiveException extends RuntimeException{
    public ContractNotActiveException() {super("Nije moguće raskinuti ugovor koji nije aktivan.");}
}
