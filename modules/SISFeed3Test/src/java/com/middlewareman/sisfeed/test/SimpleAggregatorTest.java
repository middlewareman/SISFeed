package com.middlewareman.sisfeed.test;

import java.util.List;

import junit.swingui.TestRunner;

import com.middlewareman.sisfeed.aggregator.SimpleAggregator;

public class SimpleAggregatorTest extends AggregatorTest {

	private final List<FileTime> files;

	public static void main( String[] args ) {
		TestRunner.run( SimpleAggregatorTest.class );
	}

	public SimpleAggregatorTest() {
		super();
		files = getFileTimes();
	}

	public void testSimpleAggregatorBulk() throws Exception {
		System.out.println( "\ntestSimpleAggregator bulk" );
		SimpleAggregator simple = new SimpleAggregator();
		doAggregatorBulk( files, simple );
		simple.describeMasters();
		describeExceptions();
	}

	public void testSimpleAggregatorSplit() throws Exception {
		System.out.println( "\ntestSimpleAggregator split" );
		SimpleAggregator simple = new SimpleAggregator();
		doAggregatorSplit( files, simple );
		simple.describeMasters();
		describeExceptions();
		
	}

}
