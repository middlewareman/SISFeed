package com.middlewareman.sisfeed.aggregator;

/**
 * The message being processed has a timestamp that precedes that of a
 * previously received message. If messages are expected to arrive out of order,
 * such as when message sorting is used, the message can be ignored. If messages
 * are not expected out of order, this exception should raise alarms.
 * 
 * @author Andreas Nyberg
 */
public class MessageOrderException extends IgnoreableMessageException {

	private static final long serialVersionUID = -1628686970040474004L;

	public MessageOrderException(String messageId, String key) {
		super(messageId, key);
	}

	@Override
	public String getMessage() {
		return "Timestamps out of order: " + super.getMessage();
	}
}
