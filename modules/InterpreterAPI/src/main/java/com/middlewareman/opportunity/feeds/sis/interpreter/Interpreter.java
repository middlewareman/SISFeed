package com.middlewareman.opportunity.feeds.sis.interpreter;

import org.w3c.dom.Document;

import com.middlewareman.opportunity.feeds.sis.InvalidDocumentException;

/**
 * An Interpreter has the double responsibility of semantically interpreting the
 * documents and syntactically apply an update document to a master document.
 * Some documents may contain several master ids, but the interpreter should
 * only operate on the given master id as the processing may have been split
 * into several streams according to master id for reasons of ordering.
 * 
 * @author Andreas Nyberg
 */
public interface Interpreter {

	/**
	 * Imports a previously unseen master document.
	 * 
	 * @param masterId
	 *            The single master id even if the document contains several
	 *            ids.
	 * @param master
	 *            The master document to interpret.
	 * @return The same master document, possibly decorated with a comment or
	 *         similar.
	 * @throws InvalidDocumentException
	 *             if the document is not valid.
	 */
	Document insert( String masterId, Document master )
			throws InvalidDocumentException;

	/**
	 * Imports a master document that is expected to already have been imported.
	 * 
	 * @param masterId
	 *            The single master id even if the document contains several
	 *            ids.
	 * @param master
	 *            The master document to interpret.
	 * @return The same master document, possibly decorated with a comment or
	 *         similar.
	 * @throws InvalidDocumentException
	 *             if the document is not valid.
	 */
	Document refresh( String masterId, Document master )
			throws InvalidDocumentException;

	/**
	 * Applies an update document to an existing master document and interprets
	 * the difference.
	 * 
	 * @param masterId
	 *            The single master id even if the document contains several
	 *            ids.
	 * @param master
	 *            The previous version of the master document.
	 * @param update
	 *            The update document.
	 * @return The updated master document, possibly decorated with a comment or
	 *         similar.
	 * @throws InvalidDocumentException
	 *             if any document is not valid.
	 */
	Document update( String masterId, Document master, Document update )
			throws InvalidDocumentException;

}
