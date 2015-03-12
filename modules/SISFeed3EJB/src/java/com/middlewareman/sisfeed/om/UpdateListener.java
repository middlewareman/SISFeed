package com.middlewareman.sisfeed.om;

import com.middlewareman.sisfeed.sisom.ResourceException;

/**
 * An instance of this interface knows what to do with certain events generated
 * while applying updates to a master document. These will include price
 * changes, etc. After one or a set of updates, the flush method will be called.
 * A simplistic implementation would only implement flush, and push out the
 * entire data set each time.
 * 
 * @author Andreas Nyberg
 */
public interface UpdateListener {

	// TODO all sorts of little events like price changes etc

	/**
	 * This method is called when one or more updates to a master are about to
	 * be persisted.
	 */
	void flush() throws ResourceException;

}
