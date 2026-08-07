package ftn.iis.exception;

public class NonManagerViewingSuggestionsException extends RuntimeException{
    public NonManagerViewingSuggestionsException() {super("Samo menadzeri smeju da gledaju odobrene predloge naslova.");}
}
