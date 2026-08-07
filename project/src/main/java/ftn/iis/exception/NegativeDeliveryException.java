package ftn.iis.exception;

public class NegativeDeliveryException extends RuntimeException{
    public NegativeDeliveryException() {super("Broj dana za isporuku mora biti nenegativan.");}
}
