package com.middlewareman.sisfeed.aggregator;

/**
 * A technical error occurred during aggregation.
 * 
 * @author Andreas Nyberg
 */
public class AggregatorException extends Exception {

	private static final long serialVersionUID = -4864807687510086276L;

	public AggregatorException(String message) {
		super(message);
	}

	public AggregatorException(String message, Throwable cause) {
		super(message, cause);
	}

}
