package ftn.iis.exception;

public class InvalidContractDateException extends RuntimeException{
        public InvalidContractDateException () {super("Datum isteka ne može biti pre datuma početka.");}
}
