package com.middlewareman.sisfeed.messaging;

import java.util.Collection;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import weblogic.jms.extensions.WLMessageProducer;

import com.middlewareman.sisfeed.aggregator.MessageAttr;
import com.middlewareman.sisfeed.sisom.DOMAnyDocument;
import com.middlewareman.sisfeed.sisom.DocumentException;

/**
 * MessageDecorator decorates and sends a JMS message to a WebLogic destination. If the incoming
 * message contains several ids, it is resent once for each id. Most significantly, each outgoing
 * message is decorated with the id as the unit of order and a message property. The outgoing
 * messages also carry the incoming JMSMessageID as their JMSCorrelationID, original message
 * timestamp, decorated timestamp, and event date. If the message is invalid, it is simply forwarded
 * as it will be correctly handled and rejected by the aggregator. MessageDecorator assumes no
 * responsibility for committing the outbound session in case it is transacted.
 * 
 * @author Andreas Nyberg
 */
public abstract class MessageDecorator {

	static final String MASTER_ID_PROPERTY = "masterId";

	static final String EVENT_DATE_PROPERY = "eventDate";

	static public final String ORIGINAL_TIMESTAMP_PROPERTY = "originalTimestamp";

	static final String INCOMING_TIMESTAMP_PROPERTY = "incomingTimestamp";

	/* Do not instantiate static class. */
	private MessageDecorator() {}

	/**
	 * Retrieve, decorate and forward a raw incoming message using a given session.
	 * 
	 * @param session
	 * @param destination
	 * @param inbound
	 * @throws JMSException
	 */
	public static void processMessage( Session session, Destination destination, Message inbound )
			throws JMSException {
		final long decoratedTimestamp = System.currentTimeMillis();
		try {
			MessageRetriever.Envelope envelope = MessageRetriever.retrieve( inbound );
			decorateAndSend( session, destination, decoratedTimestamp, envelope.document,
					envelope.attributes );
		} catch (DocumentException e) {
			reject( session, destination, inbound, e.getMessage() );
		}
	}

	/**
	 * Send a message with content not automatically extracted from a message.
	 * 
	 * @param session
	 * @param target
	 * @param decoratedTimestamp
	 * @param any
	 * @param attr
	 * @throws JMSException
	 */
	private static void decorateAndSend( Session session, Destination target,
			long decoratedTimestamp, DOMAnyDocument any, MessageAttr attr ) throws JMSException {
		Collection<String> ids = any.getIds();
		any.addComment( "Decorated " + DOMAnyDocument.timestamp( decoratedTimestamp )
				+ " Original " + DOMAnyDocument.timestamp( attr.getMessageTimestamp() ) + " Delay "
				+ (decoratedTimestamp - attr.getMessageTimestamp()) + " ms" );
		final Message outbound = session.createTextMessage( any.flatten() ); // DOM?
		// final Message outbound = ((WLSession)session).createXMLMessage(any.getDocument());
		outbound.setJMSCorrelationID( attr.getMessageId() );
		outbound.setLongProperty( ORIGINAL_TIMESTAMP_PROPERTY, attr.getMessageTimestamp() );
		outbound.setLongProperty( INCOMING_TIMESTAMP_PROPERTY, decoratedTimestamp );
		outbound.setStringProperty( EVENT_DATE_PROPERY, any.getDateString() );
		for ( String id : ids ) {
			// outbound.setStringProperty( MASTER_ID_PROPERTY, id );
			WLMessageProducer producer = (WLMessageProducer) session.createProducer( target );
			producer.setUnitOfOrder( id );
			producer.send( outbound );
			producer.close();
		}
	}

	private static void reject( Session session, Destination destination, Message inbound,
			String reason ) throws JMSException {
		System.out.println( "Decorator REJECTING " + inbound.getJMSMessageID() + ": " + reason );
		WLMessageProducer producer = (WLMessageProducer) session.createProducer( destination );
		producer.setUnitOfOrder( "reject" );
		producer.forward( inbound );
		producer.close();
	}

}
