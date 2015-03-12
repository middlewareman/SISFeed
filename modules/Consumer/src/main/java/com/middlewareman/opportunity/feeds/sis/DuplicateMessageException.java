package com.middlewareman.opportunity.feeds.sis;

public class DuplicateMessageException extends Exception {

	private static final long serialVersionUID = 1L;

	public DuplicateMessageException( String messageId, String masterId ) {
		super( "messageId=" + messageId + ", masterId=" + masterId );
	}

}
