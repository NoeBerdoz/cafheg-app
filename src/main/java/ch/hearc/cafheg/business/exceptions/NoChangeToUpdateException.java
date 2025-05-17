package ch.hearc.cafheg.business.exceptions;

public class NoChangeToUpdateException extends RuntimeException {
    public NoChangeToUpdateException(String message) {
        super(message);
    }
}
