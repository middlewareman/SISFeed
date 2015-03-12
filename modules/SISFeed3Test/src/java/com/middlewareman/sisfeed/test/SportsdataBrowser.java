package com.middlewareman.sisfeed.test;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

public class SportsdataBrowser extends TestCase {

	private ConnectionFactory connectionFactory;

	private Queue sportsdata;

	public void testDirectLookup() throws NamingException {
		System.out.println( "testDirectLookup" );
		lookupDirect();
		System.out.println( connectionFactory.toString() );
		System.out.println( sportsdata.toString() );
	}

	public void testWebLogicLookup() throws NamingException {
		System.out.println( "testWebLogicLookup" );
		lookupWebLogic();
		System.out.println( connectionFactory.toString() );
		System.out.println( sportsdata.toString() );
	}

	public void testBrowseDirect() throws NamingException, JMSException {
		System.out.println( "testBrowseDirect" );
		lookupDirect();
		browse( 10 );
	}

	public void testBrowseWebLogic() throws NamingException, JMSException {
		System.out.println( "testBrowseWebLogic" );
		lookupWebLogic();
		browse( 10 );
	}

	private void lookupDirect() throws NamingException {
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory" );
		table.put( Context.PROVIDER_URL,
				"file:/c:/Project/SIS/JNDI/FS/myGWD_mycCtx" );
		InitialContext ic = new InitialContext( table );
		connectionFactory = (ConnectionFactory) ic.lookup( "myGWD" );
		sportsdata = (Queue) ic.lookup( "SPORTSDATA" );
	}

	private void lookupWebLogic() throws NamingException {
		InitialContext ic = new InitialContext();
		connectionFactory = (ConnectionFactory) ic.lookup( "sisfeed.jms.remote.connect" );
		sportsdata = (Queue) ic.lookup( "sisfeed.jms.remote.sportsdata" );
	}

	private void browse( int max ) throws JMSException {
		Connection connection = connectionFactory.createConnection();
		Session session = connection.createSession( true, 0 );
		QueueBrowser browser = session.createBrowser( sportsdata );
		Enumeration msgs = browser.getEnumeration();
		for ( int i = 0; i < max && msgs.hasMoreElements(); i++ ) {
			Object object = msgs.nextElement();
			System.out.println( "i=" + i + ": " + object.getClass().getName() );
			System.out.println( object );
		}
		browser.close();
		session.close();
		connection.close();
	}

	public static void main( String[] args ) {}

}
