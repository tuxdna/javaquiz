package hs.psil.exceptions;

public class NoSuchKeyException extends Error {
	private static final long serialVersionUID = 1L;

	public NoSuchKeyException(String message) {
		super(message);
	}
}