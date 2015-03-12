package com.middlewareman.sisfeed.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

import com.middlewareman.sisfeed.aggregator.AbstractAggregator;
import com.middlewareman.sisfeed.aggregator.AggregatorException;
import com.middlewareman.sisfeed.aggregator.MessageAttr;
import com.middlewareman.sisfeed.sisom.DOMAnyDocument;
import com.middlewareman.sisfeed.sisom.DocumentException;
import com.middlewareman.sisfeed.sisom.ResourceException;

public abstract class AggregatorTest extends TestCase {

	private static final String DOCUMENTDIR = "C:\\Temp\\SISFeed\\Testdata\\20070203";

	private static final int MAXDOCUMENTS = 0;

	private static final int DUPLICATES = 0;

	private static final int SHUFFLES = 0;

	private class Counter {

		int count;

		Counter( int i ) {
			count = i;
		}

		@Override
		public String toString() {
			return Integer.toString( count );
		}
	}

	private HashMap<Class, Counter> exceptions = null;

	// new HashMap<Class, Integer>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		exceptions = new HashMap<Class, Counter>();
	}

	protected List<FileTime> getFileTimes() {
		return getFileTimes( DOCUMENTDIR, MAXDOCUMENTS, DUPLICATES, SHUFFLES );
	}

	protected List<FileTime> getFileTimes( String documentDir, int maxDocuments, int duplicates,
			int shuffles ) {
		File[] files = new File( documentDir ).listFiles();
		FileTime[] fileTimes = wrap( files );
		FileTime.sort( fileTimes );

		/* Truncate and copy. */
		final int size = (maxDocuments > 0) ? Math.min( maxDocuments, fileTimes.length )
				: fileTimes.length;
		ArrayList<FileTime> documents = new ArrayList<FileTime>( size + duplicates );
		for ( int i = 0; i < size; i++ )
			documents.add( fileTimes[i] );

		Random rand = new Random();

		/* Permute order. */
		for ( int i = 0; i < shuffles; i++ ) {
			final int one = rand.nextInt( size );
			int two;
			do {
				two = rand.nextInt( size );
			} while (one == two);
			FileTime temp = documents.get( one );
			documents.set( one, documents.get( two ) );
			documents.set( two, temp );
		}

		/* Insert duplicates. */
		for ( int i = 0; i < duplicates; i++ ) {
			final int index = rand.nextInt( documents.size() );
			documents.add( index, documents.get( index ) );
		}

		System.out.println( "returning a list of " + documents.size() + " including " + duplicates
				+ " duplicates, shuffles " + shuffles );
		return documents;
	}

	protected void doAggregatorBulk( List<FileTime> documents, AbstractAggregator aggregator )
			throws FileNotFoundException, AggregatorException, ResourceException {
		System.out.println( "doAggregatorDuplicates with " + aggregator.getClass().getName() );
		long start = System.currentTimeMillis();
		for ( FileTime fileTime : documents ) {
			InputSource input = new InputSource( new FileReader( fileTime.getFile() ) );
			try {
				MessageAttr attr = new MessageAttr( fileTime.getFile().getName(), fileTime
						.getTimestamp(), null, 0L );
				DOMAnyDocument any = new DOMAnyDocument( fileTime.getFile().getPath(), input );
				aggregator.process( attr, any );
			} catch (Exception e) {
				logException( e );
			}
		}
		long stop = System.currentTimeMillis();
		System.out.println( documents.size() + " documents processed in " + (stop - start)
				+ " milliseconds; " + (documents.size() * 1000 / (stop - start))
				+ " documents per second" );
	}

	protected void doAggregatorSplit( List<FileTime> documents, AbstractAggregator aggregator )
			throws FileNotFoundException, AggregatorException, ResourceException {
		System.out.println( "doAggregatorDuplicates with " + aggregator.getClass().getName() );
		long start = System.currentTimeMillis();
		for ( FileTime fileTime : documents ) {
			InputSource input = new InputSource( new FileReader( fileTime.getFile() ) );
			try {
				DOMAnyDocument any = new DOMAnyDocument( fileTime.getFile().getPath(), input );
				for ( String key : any.getIds() ) {
					MessageAttr attr = new MessageAttr( fileTime.getFile().getName(), fileTime
							.getTimestamp(), key, 0L );
					try {
						aggregator.process( attr, any );
					} catch (Exception e) {
						logException( e );
					}
				}
			} catch (DocumentException e) {
				logException( e );
			}
		}
		long stop = System.currentTimeMillis();
		System.out.println( documents.size() + " documents processed in " + (stop - start)
				+ " milliseconds; " + (documents.size() * 1000 / (stop - start))
				+ " documents per second" );
	}

	private FileTime wrap( File file ) {
		String name = file.getName();
		final int before = name.lastIndexOf( '_' );
		final int after = name.indexOf( '.', before + 1 );
		final long millis = Long.parseLong( name.substring( before + 1, after ) );
		return new FileTime( file, millis );
	}

	private FileTime[] wrap( File[] files ) {
		FileTime[] fileTimes = new FileTime[files.length];
		for ( int i = 0; i < files.length; i++ )
			fileTimes[i] = wrap( files[i] );
		return fileTimes;
	}

	private void logException( Exception e ) {
		Counter counter = exceptions.get( e.getClass() );
		if ( counter == null )
			exceptions.put( e.getClass(), new Counter( 1 ) );
		else
			++counter.count;

	}

	protected void describeExceptions() {
		System.out.println( exceptions );
	}

}