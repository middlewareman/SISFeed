package com.middlewareman.opportunity.feeds.sis.masterentity;


public interface MasterRepository {

	void insert( MasterEntity master ) throws DuplicateMasterException; // TODO Duplicate

	MasterEntity retrieve( String masterId );

	void update( MasterEntity masterEntity );

	void remove(String mastrId);
}
