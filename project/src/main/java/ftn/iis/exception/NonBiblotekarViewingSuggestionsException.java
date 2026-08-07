package ftn.iis.exception;

public class NonBiblotekarViewingSuggestionsException extends RuntimeException{
    public NonBiblotekarViewingSuggestionsException() {super("Samo bibliotekari smeju da gledaju predloge naslova na cekanju.");}
}
