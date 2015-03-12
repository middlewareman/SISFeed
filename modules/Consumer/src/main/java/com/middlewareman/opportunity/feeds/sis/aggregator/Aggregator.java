package com.middlewareman.opportunity.feeds.sis.aggregator;

import com.middlewareman.opportunity.feeds.sis.DuplicateMessageException;
import com.middlewareman.opportunity.feeds.sis.InvalidDocumentException;
import com.middlewareman.opportunity.feeds.sis.MessageContent;
import com.middlewareman.opportunity.feeds.sis.interpreter.Interpreter;
import com.middlewareman.opportunity.feeds.sis.masterentity.DuplicateMasterException;
import com.middlewareman.opportunity.feeds.sis.masterentity.MasterEntity;
import com.middlewareman.opportunity.feeds.sis.masterentity.MasterRepository;

public class Aggregator {

	private MasterRepository masterRepository;

	private Interpreter interpreter;

	public Aggregator( MasterRepository masterRepository,
			Interpreter interpreter ) {
		this.masterRepository = masterRepository;
		this.interpreter = interpreter;
	}

	public void process( MessageContent messageContent )
			throws InvalidDocumentException, DuplicateMessageException {

		for ( MessageContent subContent : messageContent.split() ) {
			String messageId = messageContent.getMessageId();
			String masterId = subContent.getMasterId();
			if ( messageContent.isMaster() ) {
				if ( !messageContent.isRefresh() ) {
					/* New master */
					if ( subContent.isRedelivered() ) {
						/* Anticipate duplicate */
						MasterEntity master = masterRepository
								.retrieve( masterId );
						if ( master != null
								&& master.getLastMessageId().equals( messageId ) ) {
							throw new DuplicateMessageException( messageId,
									masterId );
						}
					} else {
						MasterEntity master = new MasterEntity(); // TODO
						try {
							masterRepository.insert( master );
						} catch ( DuplicateMasterException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						interpreter.insert( masterId, subContent.getDocument() );
					}
				} else {
					// TODO refresh
				}
			} else if ( messageContent.isUpdate() ) {
				// TODO update
			} else
				throw new InvalidDocumentException(
						"Neither master nor update: "
								+ messageContent.toString() );
		}

	}
}
