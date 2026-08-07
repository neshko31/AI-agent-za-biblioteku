package ftn.iis.exception;

public class NonManagerStartingAnalysisException extends RuntimeException{
    public NonManagerStartingAnalysisException() {super("Samo menadzeri mogu da upravljaju analizom trendova!");}
}
