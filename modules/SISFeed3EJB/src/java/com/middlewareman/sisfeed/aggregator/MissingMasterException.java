package com.middlewareman.sisfeed.aggregator;


/**
 * An update has been received for a master that is not known. This should not
 * happen under normal circumstances. If it does, one might consider handling it
 * by requesting a new master.
 * 
 * @author Andreas Nyberg
 */
public class MissingMasterException extends IgnoreableMessageException {

	private static final long serialVersionUID = -2576193036210250752L;

	public MissingMasterException(String messageId, String key) {
		super(messageId, key);
	}

	@Override
	public String getMessage() {
		return "Missing master document: " + super.getMessage();
	}
}
