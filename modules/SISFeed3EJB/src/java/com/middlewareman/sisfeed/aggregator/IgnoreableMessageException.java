package com.middlewareman.sisfeed.aggregator;

/**
 * Superclass for exceptions that indicate the message can most likely be ignored.
 * 
 * @author Andreas Nyberg
 */
public abstract class IgnoreableMessageException extends Exception {

	private final String messageId;

	private final String key;

	public IgnoreableMessageException( String messageId, String key ) {
		this.messageId = messageId;
		this.key = key;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String getMessage() {
		return "messageId=" + messageId + " key=" + key;
	}
}
