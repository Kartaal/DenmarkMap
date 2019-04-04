package bfst19.osmdrawing.KDTree;

public class UnexpectedSizeException extends Throwable {

    String message;

    public UnexpectedSizeException(String s) {
        this.message = s;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
