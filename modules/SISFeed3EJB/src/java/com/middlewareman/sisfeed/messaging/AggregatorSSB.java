package com.middlewareman.sisfeed.messaging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.xml.sax.InputSource;

import weblogic.ejb.GenericSessionBean;
import weblogic.ejbgen.Constants;
import weblogic.ejbgen.EjbLocalRef;
import weblogic.ejbgen.EjbLocalRefs;
import weblogic.ejbgen.FileGeneration;
import weblogic.ejbgen.JndiName;
import weblogic.ejbgen.RemoteMethod;
import weblogic.ejbgen.Session;

import com.middlewareman.sisfeed.aggregator.AggregatorException;
import com.middlewareman.sisfeed.aggregator.EntityAggregator;
import com.middlewareman.sisfeed.aggregator.IgnoreableMessageException;
import com.middlewareman.sisfeed.aggregator.MessageAttr;
import com.middlewareman.sisfeed.sisom.DOMAnyDocument;
import com.middlewareman.sisfeed.sisom.DocumentException;
import com.middlewareman.sisfeed.sisom.ResourceException;
import com.middlewareman.sisfeed.sportsdata.Key;
import com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocalHome;

/**
 * Facade for SportsdataEntity for testing.
 * 
 * @author Andreas Nyberg
 */
@EjbLocalRefs( { @EjbLocalRef(home = "com.middlewareman.sisfeed.sportsdata.SportsdataLocalHome", jndiName = "sisfeed.ejb.sportsdata", local = "com.middlewareman.sisfeed.sportsdata.SportsdataLocal", name = "ejb/sportsdata", type = Constants.RefType.ENTITY) })
@Session(ejbName = "AggregatorSSB", defaultTransaction = Constants.TransactionAttribute.REQUIRED, enableCallByReference = Constants.Bool.TRUE)
@JndiName(remote = "sisfeed.ejb.AggregatorSSBRemoteHome")
@FileGeneration(remoteClass = Constants.Bool.TRUE, remoteHome = Constants.Bool.TRUE, localClass = Constants.Bool.FALSE, localHome = Constants.Bool.FALSE)
public class AggregatorSSB extends GenericSessionBean implements SessionBean {

	private static final long serialVersionUID = 1L;

	private SportsdataEntityLocalHome sportsdataHome;

	private EntityAggregator aggregator;

	public void ejbCreate() {
		try {
			InitialContext ic = new InitialContext();
			sportsdataHome = (SportsdataEntityLocalHome) ic.lookup( "java:comp/env/ejb/sportsdata" );
		} catch (NamingException e) {
			throw new EJBException( "Cannot look up", e );
		}
		aggregator = new EntityAggregator( sportsdataHome );
	}

	private void process( String messageId, long messageTimestamp, InputSource inputSource )
			throws DocumentException, ResourceException, AggregatorException,
			IgnoreableMessageException {
		MessageAttr attr = new MessageAttr( messageId, messageTimestamp, null, 0L );
		DOMAnyDocument any = new DOMAnyDocument( messageId, inputSource );
		aggregator.process( attr, any );
	}

	@RemoteMethod
	public void process( String messageId, long messageTimestamp, String document )
			throws DocumentException, ResourceException, AggregatorException,
			IgnoreableMessageException {
		InputSource inputSource = new InputSource( new StringReader( document ) );
		MessageAttr attr = new MessageAttr( messageId, messageTimestamp, null, 0L );
		DOMAnyDocument any = new DOMAnyDocument( messageId, inputSource );
		aggregator.process( attr, any );
	}

	@RemoteMethod
	public void process( File file, long sourceTimestamp ) throws FileNotFoundException,
			DocumentException, ResourceException, AggregatorException, IgnoreableMessageException {
		InputSource inputSource = new InputSource( new FileReader( file ) );
		process( file.getName(), sourceTimestamp, inputSource );
	}

	@RemoteMethod
	public String getMaster( Key key ) throws FinderException {
		return sportsdataHome.findByPrimaryKey( key ).getDocument();
	}

	@RemoteMethod
	public void wipeTableFiles() throws EJBException, FinderException, RemoveException {
		System.out.println( "AggregatorSSB wipeTableFiles would wipe "
				+ sportsdataHome.findTestdata().size() + " entries" );
		// for ( SportsdataEntityLocal entity : sportsdataHome.findTestdata() ) {
		// entity.remove();
		// }
	}

	@RemoteMethod
	public String ping( String ping ) {
		return ping.concat( "pong" );
	}

}