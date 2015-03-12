package com.middlewareman.sisfeed.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.middlewareman.sisfeed.sportsdata.Key;
import com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocal;
import com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocalHome;

/**
 * Servlet implementation class for Servlet: MasterDumpServlet
 * 
 * @web.servlet name="MasterDumpServlet" display-name="MasterDumpServlet"
 * 
 * @web.servlet-mapping url-pattern="/MasterDumpServlet"
 * 
 */
public class MasterDumpServlet extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {

	private static final long serialVersionUID = -2057435049453114755L;

	public final String masterHomeJndiName = "sisfeed.ejb.sportsdata";

	public final String idParamName = "id";

	public final String dateParamName = "date";

	public final String myurl = "/SISFeed3SimpleWeb/MasterDump";

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public MasterDumpServlet() {
		super();
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException {
		final String id = request.getParameter( idParamName );
		final String date = request.getParameter( dateParamName );
		final PrintWriter out = response.getWriter();
		try {
			if ( id == null && date == null ) {
				Map<Date, Integer> dates = selectDates();
				response.setContentType( "text/html" );
				listDates( dates, out );
			} else if ( id == null && date != null ) {
				Date dateDate = java.sql.Date.valueOf( date );
				Collection<SportsdataEntityLocal> masters = getMasterHome().findByDate( dateDate );
				response.setContentType( "text/html" );
				listMasters( masters, out );
			} else if ( id != null && date != null ) {
				Date dateDate = java.sql.Date.valueOf( date );
				Key key = new Key( id, dateDate );
				SportsdataEntityLocal master = getMasterHome().findByPrimaryKey( key );
				response.setContentType( "text/xml" );
				out.print( master.getDocument() );
			}
		} catch (Exception e) {
			e.printStackTrace( out );
		}
	}

	private void listDates( Map<Date, Integer> dates, PrintWriter out ) {
		out.println( "<html><head><title>List masters</title></head><body>" );
		out.println( "<table border=\"1\">" );
		out.println( "<tr><th>date</th><th>number of masters</th></tr>" );
		for ( Map.Entry<Date, Integer> pair : dates.entrySet() ) {
			out.println( "<tr>" );
			out
					.print( "<td><a href=\"" + myurl + "?" + dateParamName + "=" + pair.getKey()
							+ "\">" );
			out.print( pair.getKey() );
			out.println( "</a></td>" );

			out.print( "<td>" );
			out.print( pair.getValue() );
			out.println( "</td></tr>" );
		}
		out.println( "</table></body></html>" );
	}

	private void listMasters( Collection<SportsdataEntityLocal> masters, PrintWriter out ) {
		out.println( "<html><head><title>List masters</title></head><body>" );
		out.println( "<table border=\"1\">" );
		out
				.println( "<tr><th>masterId</th><th>length</th><th>messageId</th><th>replaces</th><th>updates</th><th>messageTimestamp</th><th>incomingTimestamp</th><th>updatedTimestamp</th><th>processing ms</th><th>flighttime ms</th></tr>" );
		for ( SportsdataEntityLocal sd : masters ) {
			out.println( "<tr>" );
			out.print( "<td><a href=\"" + myurl + "?" + idParamName + "=" + sd.getId() + "&"
					+ dateParamName + "=" + sd.getEventDate() + "\">" );
			out.print( new Key( sd.getId(), sd.getEventDate() ) );
			out.println( "</a></td>" );

			out.print( "<td>" );
			out.print( sd.getDocument().length() );
			out.println( "</td>" );

			out.print( "<td>" );
			out.print( sd.getMessageId() );
			out.println( "</td>" );

			out.print( "<td>" );
			out.print( sd.getReplaces() );
			out.println( "</td>" );

			out.print( "<td>" );
			out.print( sd.getUpdates() );
			out.println( "</td>" );

			out.print( "<td>" );
			out.print( sd.getMessageTimestamp() );
			out.println( "</td>" );

			out.print( "<td>" );
			out.print( sd.getDecoratedTimestamp() );
			out.println( "</td>" );

			out.print( "<td>" );
			out.print( sd.getUpdatedTimestamp() );
			out.println( "</td>" );

			out.print( "<td>" );
			out.print( (sd.getUpdatedTimestamp().getTime() - sd.getDecoratedTimestamp().getTime()) );
			out.println( "</td>" );

			out.print( "<td>" );
			out.print( (sd.getUpdatedTimestamp().getTime() - sd.getMessageTimestamp().getTime()) );
			out.println( "</td>" );

			out.println( "</tr>" );

		}
		out.println( "</table>" );
		out.println( "</body></html>" );

	}

	private SportsdataEntityLocalHome getMasterHome() throws NamingException {
		return (SportsdataEntityLocalHome) new InitialContext().lookup( masterHomeJndiName );
	}

	public Map<Date, Integer> selectDates() {
		Connection con = null;
		try {
			DataSource ds = (DataSource) new InitialContext().lookup( "sisfeed.jdbc.oracle" );
			con = ds.getConnection();
			PreparedStatement stmt = con
					.prepareStatement( "select event_date,count(*) from sportsdataentity group by event_date" );
			ResultSet rs = stmt.executeQuery();
			Map<Date, Integer> map = new TreeMap<Date, Integer>();
			while (rs.next()) {
				map.put( rs.getDate( 1 ), rs.getInt( 2 ) );
			}
			rs.close();
			stmt.close();
			return map;
		} catch (Exception e) {
			throw new EJBException( e );
		} finally {
			if ( con == null ) try {
				con.close();
			} catch (SQLException e) {
				throw new EJBException( e );
			}
		}
	}

}