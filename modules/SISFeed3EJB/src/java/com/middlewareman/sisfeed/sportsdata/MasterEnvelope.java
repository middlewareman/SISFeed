package com.middlewareman.sisfeed.sportsdata;

import java.io.Serializable;

import com.middlewareman.sisfeed.aggregator.MessageAttr;
import com.middlewareman.sisfeed.sisom.DOMMasterDocument;
import com.middlewareman.sisfeed.sisom.DOMUpdateDocument;

/**
 * Joins the pure master document with other attributes that are also persisted along with it.
 * 
 * @author Andreas Nyberg
 */
public class MasterEnvelope implements Serializable {

	private static final long serialVersionUID = 1L;

	private DOMMasterDocument document;

	private String messageId;

	private long messageTimestamp;

	private long decoratedTimestamp;

	private int replaces;

	private int updates;

	private boolean dirty;

	@Override
	public String toString() {
		return "MasterEnvelope(messageId=" + messageId + ",messageTimestamp=" + messageTimestamp
				+ ",replaces=" + replaces + ",updates=" + updates + ",dirty=" + dirty + ")";
	}

	public MasterEnvelope( String messageId, long messageTimestamp, DOMMasterDocument document ) {
		this.messageId = messageId;
		this.messageTimestamp = messageTimestamp;
		this.document = document;
	}

	public void setDocument( DOMMasterDocument master ) {
		this.document = master;
	}

	public DOMMasterDocument getDocument() {
		return document;
	}

	public void setMessageId( String messageId ) {
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageTimestamp( long messageTimestamp ) {
		this.messageTimestamp = messageTimestamp;
	}

	public long getMessageTimestamp() {
		return messageTimestamp;
	}

	public void setDecoratedTimestamp( long decoratedTimestamp ) {
		this.decoratedTimestamp = decoratedTimestamp;
	}

	public long getDecoratedTimestamp() {
		return decoratedTimestamp;
	}

	public void setReplaces( int count ) {
		replaces = count;
	}

	public int getReplaces() {
		return replaces;
	}

	public void setUpdates( int count ) {
		updates = count;
	}

	public int getUpdates() {
		return updates;
	}

	public void setDirty( boolean flag ) {
		dirty = flag;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void update( MessageAttr attr, DOMUpdateDocument update ) {
	// TODO: Perform real update of document and side effects
	}

	public void flush() {
	// TODO
	}

}
