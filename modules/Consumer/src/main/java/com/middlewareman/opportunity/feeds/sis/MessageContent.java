package com.middlewareman.opportunity.feeds.sis;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The content of a Sportsdata JMS message is converted into an instance of
 * MessageContent, which provides decoupling from JMS and access to the
 * attributes in the message that are relevant for Aggregator. As some
 * Sportsdata messages refer to multiple master ids, MessageContent also has the
 * ability to split itself into several messages with a single id each. Note
 * that this implementation does not split the actual document, but merely
 * points out the single id through the id property. MessageContent that refers
 * to a single id will have the id property set. All MessageContent have the
 * master ids property set, but it may be a list of a single id.
 * 
 * @author Andreas Nyberg
 */
public class MessageContent {

	private String messageId;

	private boolean redelivered;

	private Document document;

	private Element data;

	private boolean master, update, refresh;

	private List<String> masterIds;

	private String masterId;

	/**
	 * Create a new MessageContent.
	 * 
	 * @param messageId
	 *            Unique message identifier from JMS used to detect duplicates.
	 * @param redelivered
	 *            Flag from JMS indicating message is redelivered.
	 * @param document
	 *            The document content of the message.
	 * @throws InvalidDocumentException
	 *             If the document is deemed invalid for any reason encountered
	 *             during the extraction of the relevant attributes only.
	 */
	public MessageContent( String messageId, boolean redelivered,
			Document document ) throws InvalidDocumentException {
		this.messageId = messageId;
		this.document = document;
		populate();
	}

	/* This implementation does not split the actual document. */
	private MessageContent( String masterId, MessageContent parent ) {
		this.masterId = masterId;
		messageId = parent.messageId;
		redelivered = parent.redelivered;
		document = parent.document;
		data = parent.data;
		master = parent.master;
		update = parent.update;
		refresh = parent.refresh;
		masterIds = java.util.Arrays.asList( masterId );
	}

	/** Returns the original JMSMessageID. */
	public String getMessageId() {
		return messageId;
	}

	/** Indicates the message might be a duplicate. */
	public boolean isRedelivered() {
		return redelivered;
	}

	/** Returns the document. */
	public Document getDocument() {
		return document;
	}

	/** Indicates the message is a master message. */
	public boolean isMaster() {
		return master;
	}

	/** Indicates the message is an update message. */
	public boolean isUpdate() {
		return update;
	}

	/**
	 * Indicates the (master) message is a refresh. If not, it is expected to be
	 * new.
	 */
	public boolean isRefresh() {
		return refresh;
	}

	/**
	 * Returns the list of master ids in the document, most often a list of a
	 * single id.
	 */
	public List<String> getMasterIds() {
		return masterIds;
	}

	/**
	 * Returns the single id in the document; of there are more than one id,
	 * this method returns null;
	 */
	public String getMasterId() {
		return masterId;
	}

	/**
	 * Returns a list of MessageContent with a single id each. Note that the
	 * actual documents are not split in this implementation; only the given id
	 * must be processed regardless of which other ids appear in the document.
	 */
	public List<MessageContent> split() {
		if ( getMasterId() != null ) {
			return Arrays.asList( this );
		} else {
			List<MessageContent> list = new ArrayList<MessageContent>(
					getMasterIds().size() );
			for ( String id : getMasterIds() ) {
				list.add( new MessageContent( id, this ) );
			}
			return list;
		}
	}

	private void populate() throws InvalidDocumentException {
		data = document.getDocumentElement();
		if ( !"data".equals( data.getNodeName() ) ) {
			throw new InvalidDocumentException(
					"document element is not 'data':" + this.toString() );
		}

		String type = data.getAttribute( "type" );
		if ( type == null ) {
			throw new InvalidDocumentException( "no type: " + this.toString() );
		}
		master = "master".equals( type );
		update = "update".equals( type );
		if ( !(master ^ update) ) {
			throw new InvalidDocumentException(
					"type not either 'master' or 'update': " + this.toString() );
		}

		if ( master ) {
			refresh = "yes".equals( data.getAttribute( "refresh" ) );
		}

		String idattr = data.getAttribute( "id" );
		if ( masterIds == null ) {
			throw new InvalidDocumentException( "no id: " + this.toString() );
		}
		StringTokenizer tokenizer = new StringTokenizer( idattr );
		int len = tokenizer.countTokens();
		masterIds = new ArrayList<String>( len );
		while ( tokenizer.hasMoreTokens() ) {
			masterIds.add( tokenizer.nextToken() );
		}
		if ( masterIds.size() < 1 ) {
			throw new InvalidDocumentException( "empty list of ids: "
					+ this.toString() );
		}
		if ( masterIds.size() == 1 ) {
			masterId = masterIds.get( 0 );
		}
	}
}
