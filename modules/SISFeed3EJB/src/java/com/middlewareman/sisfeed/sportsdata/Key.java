package com.middlewareman.sisfeed.sportsdata;

import java.io.Serializable;
import java.sql.Date;

public class Key implements Serializable {

	private static final long serialVersionUID = 1L;

	public String id;

	public Date eventDate;

	public Key() {}

	public Key( String id, Date eventDate ) {
		this.id = id;
		this.eventDate = eventDate;
	}

	@Override
	public boolean equals( Object other ) {
		return other instanceof Key && equals( (Key) other );
	}

	public boolean equals( Key other ) {
		return id.equals( other.id ) && eventDate.equals( other.eventDate );
	}

	@Override
	public int hashCode() {
		return id.hashCode() ^ eventDate.hashCode();
	}

	@Override
	public String toString() {
		return eventDate + "/" + id;
	}

}
