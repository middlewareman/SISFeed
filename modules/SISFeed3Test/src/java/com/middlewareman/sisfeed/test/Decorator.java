package com.middlewareman.sisfeed.test;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQMessageConsumer;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueReceiver;
import com.middlewareman.sisfeed.messaging.MessageDecorator;

public class Decorator {

	private static final int LOGINTERVAL = 100;

	private static int BIGRETRYINTERVAL = 1000;

	private int remoteReconnectRetryInterval = 1000;

	private int localReconnectRetryInterval = 1000;

	private ConnectionFactory remoteConnectionFactory;

	private ConnectionFactory localConnectionFactory;

	private Destination source;

	private Destination target;

	private Connection remoteConnection;

	private Connection localConnection;

	private Session remoteSession;

	private Session localSession;

	private void setupTestSourceWeblogic() throws NamingException, JMSException {
		InitialContext ic = new InitialContext();
		remoteConnectionFactory = (QueueConnectionFactory) ic.lookup( "sisfeed.jms.local.connect" );
		source = (Queue) ic.lookup( "sisfeed.jms.local.sportsdata" );
	}

	private void setupRealSourceWeblogic() throws NamingException, JMSException {
		InitialContext ic = new InitialContext();
		remoteConnectionFactory = (QueueConnectionFactory) ic.lookup( "sisfeed.jms.remote.connect" );
		source = (Queue) ic.lookup( "sisfeed.jms.remote.sportsdata" );
	}

	private void setupRealSourceJNDI() throws NamingException {
		Hashtable<String, String> table = new Hashtable<String, String>( 2 );
		table.put( javax.naming.Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.fscontext.RefFSContextFactory" );
		table.put( javax.naming.Context.PROVIDER_URL,
				"file:/c:/Project/SIS/JNDI/FS/myGWD_mycCtx" );
		InitialContext ic = new InitialContext( table );
		remoteConnectionFactory = (QueueConnectionFactory) ic.lookup( "myGWD" );
		source = (Queue) ic.lookup( "SPORTSDATA" );
	}

	/*
	 * def qcf(myGWD) transport(CLIENT) qmgr(myGWD) hostname(SDGATEWAYDEV.SIS.TV) port(1423)
	 * channel(S_my) ccsid(1208)
	 * 
	 * def q(SPORTSDATA) queue(SPORTSDATA) targclient(MQ)
	 * 
	 * def q(SPORTSDATA_UNPROCESSED) queue(SPORTSDATA_UNPROCESSED) targclient(MQ)
	 * 
	 * def q(SPORTSDATA_RECOLLECT) queue(SPORTSDATA_RECOLLECT) targclient(MQ)
	 * 
	 */
	private void setupDirectRealSourceClear() throws JMSException {
		MQQueueConnectionFactory mqf = new MQQueueConnectionFactory();
		mqf.setTransportType( 0 );

		MQQueue mqq = new MQQueue();
	}

	private void setupTargetWeblogic() throws NamingException, JMSException {
		InitialContext ic = new InitialContext();
		localConnectionFactory = (ConnectionFactory) ic.lookup( "sisfeed.jms.local.connect" );
		target = (Destination) ic.lookup( "sisfeed.jms.local.decorated" );
	}

	private void openRemoteConnection() {
		int retry = 0;
		for ( ;; ) {
			try {
				remoteConnection = remoteConnectionFactory.createConnection();
				remoteConnection.start();
				return;
			} catch (JMSException e) {
				error( retry++, "cannot open remote connection: "
						+ remoteConnectionFactory.toString(), e );
				try {
					Thread.sleep( remoteReconnectRetryInterval );
				} catch (InterruptedException e1) {}
			}
		}
	}

	private void openLocalConnection() {
		int retry = 0;
		for ( ;; ) {
			try {
				localConnection = localConnectionFactory.createConnection();
				localConnection.start();
				return;
			} catch (JMSException e) {
				error( retry++, "cannot open local connection: "
						+ remoteConnectionFactory.toString(), e );
				try {
					Thread.sleep( localReconnectRetryInterval );
				} catch (InterruptedException e1) {}
			}
		}
	}

	private void openRemoteSession() throws JMSException {
		if ( remoteConnection == null ) openRemoteConnection();
		remoteSession = remoteConnection.createSession( false, Session.CLIENT_ACKNOWLEDGE );
	}

	private void openLocalSession() throws JMSException {
		if ( localConnection == null ) openLocalConnection();
		localSession = localConnection.createSession( true, Session.SESSION_TRANSACTED );
	}

	public void decorateSimple() throws JMSException {
		MessageConsumer consumer = remoteSession.createConsumer( source );
		//MQQueueReceiver mqqr = (MQQueueReceiver) consumer;
		long totalStart = System.currentTimeMillis();
		long intervalStart = totalStart;
		int totalCount = 0;
		int intervalCount = 0;
		for ( ;; ) {
			Message incoming = consumer.receive();
			MessageDecorator.processMessage( localSession, target, incoming );
			localSession.commit();
			incoming.acknowledge();
			totalCount++;
			intervalCount++;
			if ( intervalCount == LOGINTERVAL ) {
				final long now = System.currentTimeMillis();
				final long intervalElapsed = now - intervalStart;
				final long totalElapsed = now - totalStart;
				System.out.println( intervalCount + " messages in " + intervalElapsed + " ms: "
						+ (intervalElapsed / (float) intervalCount) + " ms/message (overall "
						+ totalCount + " messages in " + totalElapsed + " ms: "
						+ (totalElapsed / (float) totalCount) + " ms/message)" );
				intervalCount = 0;
				intervalStart = System.currentTimeMillis();
			}
		}
	}

	private static void error( int retry, String text, Throwable exception ) {
		System.out.println( text );
		if ( retry == 0 ) {
			exception.printStackTrace( System.out );
			if ( exception instanceof JMSException ) {
				JMSException jmse = (JMSException) exception;
				Exception linked = jmse.getLinkedException();
				if ( linked != null ) {
					System.out.println( "Linked exception" );
					linked.printStackTrace( System.out );
				}
			}
		}
	}

	private void closeAll() {
		close( remoteSession );
		close( remoteConnection );
		close( localSession );
		close( localConnection );
	}

	private void close( Session session ) {
		if ( session != null ) {
			try {
				session.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	private void close( Connection connection ) {
		if ( connection != null ) {
			try {
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main( String[] args ) {
		int retry = 0;
		Decorator decorator = null;
		for ( ;; ) {
			try {
				decorator = new Decorator();
				decorator.setupRealSourceJNDI();
				decorator.setupTargetWeblogic();
				decorator.openRemoteSession();
				decorator.openLocalSession();
				decorator.decorateSimple();
			} catch (Exception e) {
				error( retry, "big loop failure #" + retry, e );
				retry++;
				try {
					Thread.sleep( BIGRETRYINTERVAL );
				} catch (InterruptedException e1) {}
			} finally {
				decorator.closeAll();
			}
		}
	}

}
