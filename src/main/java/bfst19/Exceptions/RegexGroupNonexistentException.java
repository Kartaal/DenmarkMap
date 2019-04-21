package bfst19.Exceptions;

public class RegexGroupNonexistentException extends RuntimeException {
	private String message;

	public RegexGroupNonexistentException(String group) {
		this.message = "Group (" + group + ") was not found in search string.";
	}

	@Override
	public String getMessage() {
		return message;
	}
}
