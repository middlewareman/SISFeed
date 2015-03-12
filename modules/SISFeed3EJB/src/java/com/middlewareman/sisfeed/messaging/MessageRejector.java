package com.middlewareman.sisfeed.messaging;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import weblogic.jms.extensions.WLMessageProducer;

public class MessageRejector {

	private MessageRejector() {}

	public static void reject( Connection connection, Destination destination, Message message,
			String reason ) throws JMSException {
		Session session = null;
		try {
			session = connection.createSession( true, Session.SESSION_TRANSACTED );
			reject( session, destination, message, reason );
			session.commit();
		} finally {
			if ( session != null ) {
				session.close();
			}
		}
	}

	public static void reject( Session session, Destination destination, Message message,
			String reason ) throws JMSException {
		log( "Rejecting messageID=" + message.getJMSMessageID() + " correlationID="
				+ message.getJMSCorrelationID() + ": " + reason );
		WLMessageProducer producer = (WLMessageProducer) session.createProducer( destination );
		producer.forward( message );
		producer.close();
	}

	private static void log( String text ) {
		System.out.println( text );
	}

}
