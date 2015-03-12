package com.middlewareman.sisfeed.aggregator;

import java.util.HashMap;

import com.middlewareman.sisfeed.sportsdata.Key;
import com.middlewareman.sisfeed.sportsdata.MasterEnvelope;

/**
 * Simple in-memory implementation of CRU(D) for testing purposes.
 * 
 * @author Andreas Nyberg
 */
public class SimpleAggregator extends AbstractAggregator {

	private HashMap<Key, MasterEnvelope> map = new HashMap<Key, MasterEnvelope>();

	@Override
	public void insert( Key key, MasterEnvelope master ) throws AggregatorException {
		if ( map.containsKey( key ) )
			throw new AggregatorException( "key " + key + " already exists" );
		map.put( key, master );
	}

	@Override
	public MasterEnvelope get( Key key ) {
		return map.get( key );
	}

	@Override
	public void replace( Key key, MasterEnvelope master ) throws AggregatorException {
		if ( !map.containsKey( key ) )
			throw new AggregatorException( "key " + key + " does not exist" );
		map.put( key, master );
	}

	public void describeMasters() {
		System.out.println( "number of masters: " + map.size() );
		int replaces = 0, updates = 0;
		for ( MasterEnvelope master : map.values() ) {
			replaces += master.getReplaces();
			updates += master.getUpdates();
		}
		System.out.println( "TOTAL replaces=" + replaces + ", updates=" + updates );
		System.out.println( map );
	}

}