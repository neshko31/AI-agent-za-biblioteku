package ftn.iis.exception;

public class NonClanRecievingNotification extends RuntimeException{
    public NonClanRecievingNotification() {super("Samo clanovi mogu da pregledaju notifikacije vezane za pracenje statusa predloga naslova za nabavku.");}
}
