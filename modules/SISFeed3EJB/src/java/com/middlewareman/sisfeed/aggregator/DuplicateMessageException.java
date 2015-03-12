package com.middlewareman.sisfeed.aggregator;


/**
 * A message has been identified as a duplicate. This usually means the message
 * can simply be ignored.
 * 
 * @author Andreas Nyberg
 */
public class DuplicateMessageException extends IgnoreableMessageException {

	private static final long serialVersionUID = -5964640792200263235L;

	public DuplicateMessageException(String messageId, String key) {
		super(messageId, key);
	}

	@Override
	public String getMessage() {
		return "Duplicate message: " + super.getMessage();
	}
}
