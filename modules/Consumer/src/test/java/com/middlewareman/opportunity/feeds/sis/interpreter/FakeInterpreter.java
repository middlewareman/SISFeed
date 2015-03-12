package com.middlewareman.opportunity.feeds.sis.interpreter;

import org.w3c.dom.Document;

import com.middlewareman.opportunity.feeds.sis.InvalidDocumentException;

public class FakeInterpreter implements Interpreter {

	public Document insert( String id, Document master )
			throws InvalidDocumentException {
		// TODO Auto-generated method stub
		return master;
	}

	public Document refresh( String id, Document master )
			throws InvalidDocumentException {
		// TODO Auto-generated method stub
		return master;
	}

	public Document update( String id, Document master, Document update )
			throws InvalidDocumentException {
		// TODO Auto-generated method stub
		return master;
	}

}
