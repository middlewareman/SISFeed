package com.middlewareman.opportunity.feeds.sis;

/**
 * This exception is thrown when a document is deemed invalid and processing
 * should not be retried. A message consumer must catch this exception and
 * swallow it in order to prevent the message consumption to roll back and
 * trigger re-delivery.
 * 
 * @author Andreas Nyberg
 */
public class InvalidDocumentException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidDocumentException(String message) {
		super(message);
	}

	public InvalidDocumentException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
