package com.middlewareman.sisfeed.sisom;

/**
 * Wrapper for a master document that maintains, accesses and accepts a DOMUpdateDocument to update
 * the content of the document. UpdateListener is the hook into OpportunityManagement.
 * 
 * @author Andreas Nyberg
 */
public class DOMMasterDocument extends DOMAnyDocument {

	/**
	 * Creates a new master document wrapper from an DOMAnyDocument.
	 * 
	 * @param sisDocument
	 * @throws DocumentException
	 * @throws DocumentException
	 */
	public DOMMasterDocument( DOMAnyDocument any ) throws DocumentException {
		super( any );
		assert any.isMaster();
	}

	/**
	 * Apply updates to master document and produce side effects such as updating databases and
	 * sending messages.
	 * 
	 * @param key
	 * @param update
	 * @throws DocumentException
	 * @throws ResourceException
	 */
	public void update( String key, DOMUpdateDocument update ) throws DocumentException,
			ResourceException {
	// TODO apply update and call listener and dirty()
	}

	/**
	 * Retrieve expiry time from document.
	 * 
	 * @return
	 * @throws DocumentException
	 */
	public long getExpiry() throws DocumentException {
		String expiry = null;
		try {
			expiry = data.getAttribute( "expiry" );
			return parseTimestamp( expiry );
		} catch (Exception e) {
			throw new DocumentException( "unknown", "cannot get expiry " + expiry + ": "
					+ e.getMessage(), e );
		}
	}

}
