package com.middlewareman.sisfeed.messaging;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import weblogic.jms.extensions.WLMessage;
import weblogic.jms.extensions.XMLMessage;

import com.middlewareman.sisfeed.aggregator.MessageAttr;
import com.middlewareman.sisfeed.sisom.DOMAnyDocument;
import com.middlewareman.sisfeed.sisom.DocumentException;

public class MessageRetriever {

	/**
	 * Simple container for the document payload and the extracted attributes of an incoming
	 * message.
	 * 
	 * @author Andreas Nyberg
	 */
	static class Envelope {

		public DOMAnyDocument document;

		public MessageAttr attributes;
	}

	/* Do not instantiate static class. */
	private MessageRetriever() {}

	/**
	 * Retrieve attributes and content of a message.
	 * 
	 * @param message
	 * @return
	 * @throws JMSException
	 * @throws DocumentException
	 */
	public static Envelope retrieve( Message message ) throws JMSException, DocumentException {
		final MessageAttr messageAttr = messageAttr( message );
		DOMAnyDocument any = null;
		if ( message instanceof XMLMessage ) {
			XMLMessage xmlMessage = (XMLMessage) message;
			Document document = xmlMessage.getDocument();
			any = new DOMAnyDocument( messageAttr.getMessageId(), document );
		} else if ( message instanceof BytesMessage ) {
			BytesMessage bytesMessage = (BytesMessage) message;
			long bodyLength = bytesMessage.getBodyLength();
			byte[] buffer = new byte[(int) bodyLength];
			int size = bytesMessage.readBytes( buffer );
			InputSource inputSource = new InputSource( new ByteArrayInputStream( buffer, 0, size ) );
			any = new DOMAnyDocument( messageAttr.getMessageId(), inputSource );
		} else if ( message instanceof TextMessage ) {
			TextMessage textMessage = (TextMessage) message;
			InputSource inputSource = new InputSource( new StringReader( textMessage.getText() ) );
			any = new DOMAnyDocument( messageAttr.getMessageId(), inputSource );
		} else
			throw new RuntimeException( "Unknown message type: " + message.getClass().getName() );

		Envelope envelope = new Envelope();
		envelope.attributes = messageAttr;
		envelope.document = any;
		return envelope;
	}

	private static MessageAttr messageAttr( Message message ) throws JMSException {
		String messageId = message.getJMSCorrelationID();
		if ( messageId == null ) messageId = message.getJMSMessageID();
		long messageTimestamp = message
				.propertyExists( MessageDecorator.ORIGINAL_TIMESTAMP_PROPERTY ) ? message
				.getLongProperty( MessageDecorator.ORIGINAL_TIMESTAMP_PROPERTY ) : message
				.getJMSTimestamp();
		String messageKey = null;
		if ( message instanceof WLMessage ) {
			WLMessage wlmessage = (WLMessage) message;
			messageKey = wlmessage.getUnitOfOrder();	// may be null
		}
		//if ( message.propertyExists( MessageDecorator.MASTER_ID_PROPERTY ) )
		//	messageKey = message.getStringProperty( MessageDecorator.MASTER_ID_PROPERTY );
		long incomingTimestamp = message
				.propertyExists( MessageDecorator.INCOMING_TIMESTAMP_PROPERTY ) ? message
				.getLongProperty( MessageDecorator.INCOMING_TIMESTAMP_PROPERTY ) : System
				.currentTimeMillis();
		return new MessageAttr( messageId, messageTimestamp, messageKey, incomingTimestamp );
	}

}
