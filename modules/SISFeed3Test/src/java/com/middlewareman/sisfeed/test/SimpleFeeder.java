package com.middlewareman.sisfeed.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.middlewareman.sisfeed.messaging.AggregatorSSBRemoteHome;
import com.middlewareman.sisfeed.messaging.MessageDecorator;

public class SimpleFeeder extends AggregatorTest {

	private static final String aggregatorSSBName = "sisfeed.ejb.AggregatorSSBRemoteHome";

	private static final String connectionFactoryName = "sisfeed.jms.local.connect";

	private static final String destinationName = "sisfeed.jms.local.sportsdata"; // "sisfeed.jms.local.decorated"

	private char[] buffer = new char[10000];

	public void testFeeder() throws NamingException, JMSException, IOException, EJBException,
			FinderException, RemoveException, CreateException {
		InitialContext ic = new InitialContext();

		((AggregatorSSBRemoteHome) ic.lookup( aggregatorSSBName )).create().wipeTableFiles();

		// System.out.println( "Wiped table" );

		ConnectionFactory connectionFactory = (ConnectionFactory) ic.lookup( connectionFactoryName );
		Destination destination = (Destination) ic.lookup( destinationName );
		Connection connection = connectionFactory.createConnection();
		Session session = connection.createSession( true, Session.SESSION_TRANSACTED );
		MessageProducer producer = session.createProducer( destination );

		List<FileTime> fileTimes = getFileTimes();
		System.out.println( "got " + fileTimes.size() + " files to load" );

		int count = 0;
		for ( FileTime fileTime : fileTimes ) {
			String text = read( fileTime.getFile() );
			Message message = session.createTextMessage( text );
			message.setLongProperty( MessageDecorator.ORIGINAL_TIMESTAMP_PROPERTY, fileTime
					.getTimestamp() );
			message.setJMSCorrelationID( fileTime.getFile().getName() );
			producer.send( message );
			++count;
		}
		session.commit();
		System.out.println( "Released " + count + " messages to destination at "
				+ System.currentTimeMillis() );
		session.close();
		connection.close();
	}

	private String read( File file ) throws IOException {
		StringWriter writer = new StringWriter( (int) file.length() );
		FileReader reader = new FileReader( file );
		int len;
		while ((len = reader.read( buffer )) != -1) {
			writer.write( buffer, 0, len );
		}
		reader.close();
		return writer.toString();
	}

}
