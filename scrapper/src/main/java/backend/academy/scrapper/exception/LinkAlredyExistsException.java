package backend.academy.scrapper.exception;

public class LinkAlredyExistsException extends RuntimeException {
    public LinkAlredyExistsException(String message) {
        super(message);
    }
}
