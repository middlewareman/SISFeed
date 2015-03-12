package com.middlewareman.sisfeed.sisom;

/**
 * Error accessing external resources, but not a business error indicating a
 * problem in a message received.
 * 
 * @author Andreas Nyberg
 */
public class ResourceException extends Exception {

	private static final long serialVersionUID = 6020852845130685314L;

	public ResourceException(String message, Throwable cause) {
		super(message, cause);
	}

}
