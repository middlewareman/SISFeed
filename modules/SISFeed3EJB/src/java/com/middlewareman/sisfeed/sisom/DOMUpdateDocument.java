package com.middlewareman.sisfeed.sisom;

/**
 * Wrapper for an update document used to access its content.
 * 
 * @author Andreas Nyberg
 */
public class DOMUpdateDocument extends DOMAnyDocument {

	public DOMUpdateDocument(DOMAnyDocument any) throws DocumentException {
		super(any);
		assert any.isUpdate();
	}

}
