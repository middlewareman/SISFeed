package com.middlewareman.sisfeed.aggregator;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Java bean to hold attributes harvested from incoming message.
 * 
 * @author Andreas Nyberg
 */
public class MessageAttr implements Serializable {

	private static final long serialVersionUID = 1L;

	private String messageId;

	private long messageTimestamp;

	private String masterId;

	long incomingTimestamp;

	public MessageAttr( String messageId, long messageTimestamp, String masterKey,
			long decoratedTimestamp ) {
		this.messageId = messageId;
		this.messageTimestamp = messageTimestamp;
		this.masterId = masterKey;
		this.incomingTimestamp = decoratedTimestamp;
	}

	public String getMessageId() {
		return messageId;
	}

	public long getMessageTimestamp() {
		return messageTimestamp;
	}
	
	public String getMasterId() {
		return masterId;
	}
	
	public long getIncomingTimestamp() {
		return incomingTimestamp;
	}

	@Override
	public String toString() {
		return "(messageId=" + messageId + ", timestamp=" + new Timestamp( messageTimestamp )
				+ ", masterId=" + masterId + ", decorated=" + new Timestamp( incomingTimestamp )
				+ ")";
	}
}
