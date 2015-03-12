package com.middlewareman.sisfeed.test;

import com.middlewareman.sisfeed.messaging.AggregatorSSBRemote;
import com.middlewareman.sisfeed.messaging.AggregatorSSBRemoteHome;
import junit.swingui.TestRunner;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.List;

public class AggregatorSSBTest extends AggregatorTest {

	public static void main( String[] args ) {
		TestRunner.run( AggregatorSSBTest.class );
	}

	public AggregatorSSBTest() {
		super();
	}

	public void testSSBConnect() throws RemoteException, NamingException, CreateException {
		System.out.println( "testSSBConnect" );
		assertEquals( "pingpong", getSSB().ping( "ping" ), "pingpong" );
	}

	public void testWipeTable() throws RemoteException, EJBException, FinderException,
			RemoveException, NamingException, CreateException {
		getSSB().wipeTableFiles();
	}

	public void testAggregatorSSBSingle() throws Exception {
		System.out.println( "testAggregatorSSBSingle" );
		int exceptions = 0;
		List<FileTime> files = getFileTimes();
		AggregatorSSBRemote aggregator = getSSB();
		aggregator.wipeTableFiles();
		final long start = System.currentTimeMillis();
		for ( FileTime fileTime : files ) {
			try {
				aggregator.process( fileTime.getFile(), fileTime.getTimestamp() );
			} catch (Exception e) {
				++exceptions;
			}
		}
		final long finish = System.currentTimeMillis();
		System.out.println( "single: " + (finish - start) + " millis for " + files.size()
				+ " documents" );
		System.out.println( "exceptions " + exceptions );
	}

	private AggregatorSSBRemoteHome getHome() throws NamingException {
		return (AggregatorSSBRemoteHome) new InitialContext()
				.lookup( "sisfeed.ejb.AggregatorSSBRemoteHome" );
	}

	private AggregatorSSBRemote getSSB() throws NamingException, RemoteException, CreateException {
		return (AggregatorSSBRemote) getHome().create();
	}

}
