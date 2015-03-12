package com.middlewareman.sisfeed.aggregator;

import com.middlewareman.sisfeed.sisom.DOMAnyDocument;
import com.middlewareman.sisfeed.sisom.DOMMasterDocument;
import com.middlewareman.sisfeed.sisom.DOMUpdateDocument;
import com.middlewareman.sisfeed.sisom.DocumentException;
import com.middlewareman.sisfeed.sisom.ResourceException;
import com.middlewareman.sisfeed.sportsdata.Key;
import com.middlewareman.sisfeed.sportsdata.MasterEnvelope;

/**
 * This implementation of the aggregator logic is decoupled from the method of document storage.
 * Note that the bahaviour of the method that processes all ids in a document may have different
 * transactional properties than that which processes exactly one id.
 * 
 * @author Andreas Nyberg
 */
public abstract class AbstractAggregator {

	/**
	 * Adds a new master for the first time. Use get to check whether it existed in the first place.
	 * 
	 * @param id
	 * @param master
	 * @throws AggregatorException
	 */
	protected abstract void insert( Key key, MasterEnvelope master ) throws AggregatorException;

	/**
	 * Retrieve master document from data store.
	 * 
	 * @param id
	 * @return the document or null if none is found
	 * @throws AggregatorException
	 */
	protected abstract MasterEnvelope get( Key key ) throws AggregatorException;

	/**
	 * Replace an existing master document.
	 * 
	 * @param id
	 * @param master
	 * @throws AggregatorException
	 *             if the master does not exist or any other technical problem
	 * @throws DocumentException
	 */
	protected abstract void replace( Key key, MasterEnvelope master ) throws AggregatorException,
			DocumentException;

	/**
	 * Process a given document with attributes. Processing depends on the type of document (master
	 * or update) and whether a key is given in the attributes.
	 * 
	 * @param attributes
	 * @param any
	 * @throws DocumentException
	 * @throws IgnoreableMessageException
	 * @throws AggregatorException
	 * @throws ResourceException
	 */
	public void process( MessageAttr attributes, DOMAnyDocument any ) throws DocumentException,
			IgnoreableMessageException, AggregatorException, ResourceException {
		if ( any.isMaster() )
			process( attributes, new DOMMasterDocument( any ) );
		else if ( any.isUpdate() )
			process( attributes, new DOMUpdateDocument( any ) );
		else
			throw new DocumentException( attributes.getMessageId(), "Neither master nor update" );
	}

	private void process( MessageAttr attributes, DOMMasterDocument master )
			throws IgnoreableMessageException, AggregatorException, DocumentException {
		final String givenId = attributes.getMasterId();
		if ( givenId != null ) {
			assert master.getIds().contains( givenId );
			process( attributes, master, givenId );
		} else {
			for ( String id : master.getIds() )
				process( attributes, master, id );
		}
	}

	private void process( MessageAttr attributes, DOMUpdateDocument update )
			throws IgnoreableMessageException, MissingMasterException, AggregatorException,
			DocumentException, ResourceException {
		final String givenId = attributes.getMasterId();
		if ( givenId != null ) {
			assert update.getIds().contains( givenId );
			process( attributes, update, givenId );
		} else {
			for ( String id : update.getIds() )
				process( attributes, update, id );
		}
	}

	private void process( MessageAttr newAttr, DOMMasterDocument newMaster, String id )
			throws AggregatorException, DuplicateMessageException, MessageOrderException,
			DocumentException {
		final Key key = new Key( id, newMaster.getDate() );
		MasterEnvelope envelope = get( key );
		if ( envelope == null ) {
			envelope = new MasterEnvelope( newAttr.getMessageId(), newAttr.getMessageTimestamp(),
					newMaster );
			newMaster.addComment( "Inserted " + DOMAnyDocument.timestamp() + " messageTimestamp="
					+ DOMAnyDocument.timestamp( newAttr.getMessageTimestamp() ) );
			envelope.setDecoratedTimestamp( newAttr.incomingTimestamp ); // may not be set
			insert( key, envelope );
		} else {
			if ( envelope.getMessageId().equals( newAttr.getMessageId() ) )
				throw new DuplicateMessageException( newAttr.getMessageId(), id );
			if ( newAttr.getMessageTimestamp() < envelope.getMessageTimestamp() )
				throw new MessageOrderException( newAttr.getMessageId(), id );
			newMaster.addComment( "Replaced " + DOMAnyDocument.timestamp() + " by "
					+ DOMAnyDocument.timestamp( newAttr.getMessageTimestamp() ) );
			envelope.setMessageId( newAttr.getMessageId() );
			envelope.setMessageTimestamp( newAttr.getMessageTimestamp() );
			envelope.setDecoratedTimestamp( newAttr.incomingTimestamp ); // may not be set
			envelope.setDocument( newMaster );
			envelope.setReplaces( envelope.getReplaces() + 1 );
			envelope.setDirty( true );
			replace( key, envelope );
		}
	}

	private void process( MessageAttr newAttr, DOMUpdateDocument update, String id )
			throws AggregatorException, DuplicateMessageException, DocumentException,
			ResourceException, MissingMasterException, MessageOrderException {
		final Key key = new Key( id, update.getDate() );
		final MasterEnvelope envelope = get( key );
		if ( envelope == null ) throw new MissingMasterException( newAttr.getMessageId(), id );
		if ( envelope.getMessageId().equals( newAttr.getMessageId() ) )
			throw new DuplicateMessageException( envelope.getMessageId(), id );
		if ( newAttr.getMessageTimestamp() < envelope.getMessageTimestamp() )
			throw new MessageOrderException( newAttr.getMessageId(), id );
		envelope.getDocument().addComment(
				"Updated " + DOMAnyDocument.timestamp() + " by "
						+ DOMAnyDocument.timestamp( newAttr.getMessageTimestamp() ) );
		envelope.setMessageId( newAttr.getMessageId() );
		envelope.setMessageTimestamp( newAttr.getMessageTimestamp() );
		envelope.setDecoratedTimestamp( newAttr.incomingTimestamp ); // may not be set
		envelope.update( newAttr, update );
		envelope.setUpdates( envelope.getUpdates() + 1 );
		envelope.setDirty( true );
	}

}
