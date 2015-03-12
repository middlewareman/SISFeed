package com.middlewareman.sisfeed.sisom;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * <data type="update" xmlns:xu="http://www.satelliteinfo.co.uk/feed/update"
 * xmlns:hrdg="http://www.satelliteinfo.co.uk/feed/master/hrdg"
 * xmlns="http://www.satelliteinfo.co.uk/feed/master/hrdg" id="AUHRES75CK1,AUHRES75CK2,AUHRES75CK3"
 * name="meeting" mnem="ES" timestamp="1169021635248" date="2007-01-17" group="SDS" category="HR"
 * source="sportsData" country="AU" route="534453" version="1.2.5">
 */

/**
 * Wrapper for DOM document handling all parsing and flattening and determining the document type
 * (master or update).
 * 
 * @author Andreas Nyberg
 */
public class DOMAnyDocument {

	private static final String TIMESTAMPFORMATSTRING = "yyyy-MM-dd'T'HH:mm:ss";

	private static final String DATEFORMATSTRING = "yyyy-MM-dd";

	static long parseTimestamp( String timestamp ) throws ParseException {
		return new SimpleDateFormat( TIMESTAMPFORMATSTRING ).parse( timestamp ).getTime();
	}

	static long parseDate( String date ) throws ParseException {
		return new SimpleDateFormat( DATEFORMATSTRING ).parse( date ).getTime();
	}

	public static String timestamp() {
		return new java.sql.Timestamp( System.currentTimeMillis() ).toString();
	}

	public static String timestamp( long millis ) {
		if ( millis == 0L )
			return "null";
		else
			return new java.sql.Timestamp( millis ).toString();
	}

	private String messageId; // Only used for logging exceptions

	private final Document document;

	protected final Element data;

	private boolean dirty = false;

	protected DOMAnyDocument( DOMAnyDocument any ) {
		document = any.document;
		data = any.data;
	}
	
	public DOMAnyDocument( String messageId, Document document ) throws DocumentException {
		this.document = document;
		NodeList datas = document.getElementsByTagName( "data" );
		if ( datas.getLength() == 0 ) {
			throw new DocumentException( messageId, "no \"data\" element" );
		} else if ( datas.getLength() > 1 ) { throw new DocumentException( messageId,
				"more than one \"data\" element" ); }
		data = (Element) datas.item( 0 );
	}

	/**
	 * Create a new wrapper for a document from a source.
	 * 
	 * @param sourceId
	 *            MessageId or file name to detect duplicate input
	 * @param input
	 * @throws DocumentException
	 */
	public DOMAnyDocument( String messageId, InputSource input ) throws DocumentException {
		this.messageId = messageId;
		// this.lastMessageTimestamp = messageTimestamp;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace( true );
		dbf.setIgnoringComments( true );
		// TODO set validating and schema ?
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException( "could not set up parser", e );
		}
		try {
			document = db.parse( input );
		} catch (SAXException e) {
			throw new DocumentException( messageId, "could not parse xml", e );
		} catch (IOException e) {
			throw new DocumentException( messageId, "could not parse xml", e );
		}
		NodeList datas = document.getElementsByTagName( "data" );
		if ( datas.getLength() == 0 ) {
			throw new DocumentException( messageId, "no \"data\" element" );
		} else if ( datas.getLength() > 1 ) { throw new DocumentException( messageId,
				"more than one \"data\" element" ); }
		data = (Element) datas.item( 0 );
		addComment( "Parsed " + timestamp() );
	}

	public Document getDocument() {
		return document;
	}

	/**
	 * Update must call this to indicate that the document has changed.
	 */
	public void setDirty( boolean flag ) {
		dirty = flag;
	}

	/**
	 * Return dirty flag
	 * 
	 * @return true if document has changed
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Returns a cached or generated XML representation of the current document.
	 * 
	 * @return
	 */
	public String flatten() {
		addComment( "Flattened " + timestamp() );
		StringWriter writer = new StringWriter( 1024 );
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty( "method", "xml" );
			transformer.transform( new DOMSource( document ), new StreamResult( writer ) );
		} catch (Exception e) {
			throw new RuntimeException( "could not flatten document to xml", e );
		}
		return writer.toString();
	}

	/**
	 * Adds a comment to the DOM. Comments are added before the data element, which makes them
	 * appear in the order they were added.
	 * 
	 * @param comment
	 */
	public void addComment( String comment ) {
		document.insertBefore( document.createComment( comment ), data );
	}

	/*
	 * Access attributes: type, id, name, mnem, timestamp, date, group, category, source, country,
	 * route, version
	 */

	public boolean isMaster() throws DocumentException {
		return "master".equals( getType() );
	}

	public boolean isUpdate() throws DocumentException {
		return "update".equals( getType() );
	}

	private String getType() {
		return data.getAttribute( "type" );
	}

	public Collection<String> getIds() {
		String ids = data.getAttribute( "id" );
		StringTokenizer tokenizer = new StringTokenizer( ids, "," );
		int tokens = tokenizer.countTokens();
		ArrayList<String> list = new ArrayList<String>( tokens );
		for ( int i = 0; i < tokens; i++ ) {
			list.add( tokenizer.nextToken() );
		}
		return list;
	}

	public String getName() {
		return data.getAttribute( "name" );
	}

	public String getMnem() {
		return data.getAttribute( "mnem" );
	}

	public String getDateString() {
		return data.getAttribute( "date" );
	}

	/**
	 * Retrieve date from document.
	 * 
	 * @return
	 * @throws DocumentException
	 */
	public Date getDate() throws DocumentException {
		String dateString = null;
		try {
			return new Date( parseDate( getDateString() ) );
		} catch (Exception e) {
			throw new DocumentException( messageId, "cannot get expiry " + dateString + ": "
					+ e.getMessage(), e );
		}
	}

	public String getGroup() {
		return data.getAttribute( "group" );
	}

	public String getCategory() {
		return data.getAttribute( "category" );
	}

	public String getSource() {
		return data.getAttribute( "source" );
	}

	public String getCountry() {
		return data.getAttribute( "country" );
	}

	public String getRoute() {
		return data.getAttribute( "route" );
	}

	public String getVersion() {
		return data.getAttribute( "version" );
	}

}
