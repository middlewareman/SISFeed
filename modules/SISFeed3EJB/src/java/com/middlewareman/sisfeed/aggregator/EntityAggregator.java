package com.middlewareman.sisfeed.aggregator;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;

import com.middlewareman.sisfeed.sisom.DocumentException;
import com.middlewareman.sisfeed.sportsdata.Key;
import com.middlewareman.sisfeed.sportsdata.MasterEnvelope;
import com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocal;
import com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocalHome;

/**
 * Aggregator that uses an the local SportsdataEntity for storage.
 * 
 * @author Andreas Nyberg
 */
public class EntityAggregator extends AbstractAggregator {

	private final SportsdataEntityLocalHome home;

	public EntityAggregator( SportsdataEntityLocalHome home ) {
		this.home = home;
	}

	protected @Override
	void insert( Key key, MasterEnvelope master ) throws AggregatorException {
		try {
			home.create( key, master );
		} catch (CreateException e) {
			throw new AggregatorException( e.getMessage(), e );
		}
	}

	@Override
	protected MasterEnvelope get( Key key ) throws AggregatorException {
		try {
			SportsdataEntityLocal entity = home.findByPrimaryKey( key );
			if ( entity == null )
				return null;
			else
				return entity.getMasterEnvelope();
		} catch (ObjectNotFoundException e) {
			return null;
		} catch (FinderException e) {
			throw new AggregatorException( e.getMessage(), e );
		}
	}

	@Override
	protected void replace( Key key, MasterEnvelope master ) throws AggregatorException,
			DocumentException {
		SportsdataEntityLocal entity;
		try {
			entity = home.findByPrimaryKey( key );
		} catch (FinderException e) {
			throw new AggregatorException( e.getMessage(), e );
		}
		entity.setMasterEnvelope( master );
	}

}
