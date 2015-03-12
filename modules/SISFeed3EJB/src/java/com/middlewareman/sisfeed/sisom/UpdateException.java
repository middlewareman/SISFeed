package com.middlewareman.sisfeed.sisom;

/**
 * An error during processing an update that indicate a message should be
 * rejected.
 * 
 * @author Andreas Nyberg
 */
public class UpdateException extends DocumentException {

	private static final long serialVersionUID = -450971727914039031L;

	private final String masterKey;

	public UpdateException(String masterKey, String sourceId, String message) {
		super(sourceId, message);
		this.masterKey = masterKey;
	}

	public UpdateException(String masterKey, String sourceId, String message,
			Throwable cause) {
		super(sourceId, message, cause);
		this.masterKey = masterKey;
	}

	public String getKey() {
		return masterKey;
	}

	@Override
	public String getMessage() {
		return masterKey + ": " + super.getMessage();
	}

}
