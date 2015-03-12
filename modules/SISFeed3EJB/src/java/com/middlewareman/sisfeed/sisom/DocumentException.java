package com.middlewareman.sisfeed.sisom;

/**
 * An error in format or processing a message that would warrant a message to be
 * rejected.
 * 
 * @author Andreas Nyberg
 */
public class DocumentException extends Exception {

	private static final long serialVersionUID = 5308561143888999186L;

	private final String sourceId;

	public DocumentException(String sourceId, String message) {
		super(message);
		this.sourceId = sourceId;
	}

	public DocumentException(String sourceId, String message, Throwable cause) {
		super(message, cause);
		this.sourceId = sourceId;
	}

	public String getSourceId() {
		return sourceId;
	}

	@Override
	public String getMessage() {
		return sourceId + ": " + super.getMessage();
	}

}
