package com.middlewareman.sisfeed.sportsdata;

import java.io.StringReader;
import java.sql.Date;
import java.sql.Timestamp;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;

import org.xml.sax.InputSource;

import weblogic.ejb.GenericEntityBean;
import weblogic.ejbgen.CmpField;
import weblogic.ejbgen.Constants;
import weblogic.ejbgen.Entity;
import weblogic.ejbgen.FileGeneration;
import weblogic.ejbgen.Finder;
import weblogic.ejbgen.Finders;
import weblogic.ejbgen.JndiName;
import weblogic.ejbgen.LocalMethod;

import com.middlewareman.sisfeed.sisom.DOMAnyDocument;
import com.middlewareman.sisfeed.sisom.DOMMasterDocument;
import com.middlewareman.sisfeed.sisom.DocumentException;

/**
 * Sportsdata master document entity with lazy XML parsing and serialization.
 * 
 * @author Andreas Nyberg
 */
@Entity(ejbName = "SportsdataEntity", primKeyClass = "Key", persistenceType = Entity.PersistenceType.CMP, tableName = "SportsdataEntity", findersLoadBean = Constants.Bool.TRUE, dataSourceName = "sisfeed.jdbc.oracle", databaseType = Entity.DatabaseType.ORACLE, abstractSchemaName = "SportsdataMaster", defaultTransaction = Constants.TransactionAttribute.REQUIRED, enableCallByReference = Constants.Bool.TRUE, concurrencyStrategy = Constants.ConcurrencyStrategy.EXCLUSIVE, readTimeoutSeconds = "60", cacheBetweenTransactions = Constants.Bool.TRUE, defaultDbmsTablesDdl = "SportsdataEntity.ddl", primKeyClassNogen = Constants.Bool.TRUE)
@JndiName(local = "sisfeed.ejb.sportsdata")
@FileGeneration(localClass = Constants.Bool.TRUE, localHome = Constants.Bool.TRUE, remoteClass = Constants.Bool.FALSE, remoteHome = Constants.Bool.FALSE, valueClass = Constants.Bool.FALSE)
@Finders( {
		@Finder(signature = "java.util.Collection<com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocal> findByDate(java.sql.Date date)", ejbQl = "SELECT OBJECT(sd) FROM SportsdataMaster sd WHERE sd.eventDate = ?1"),
		@Finder(signature = "java.util.Collection<com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocal> findByMessageId(String messageId)", ejbQl = "SELECT OBJECT(sd) FROM SportsdataMaster sd WHERE sd.messageId = ?1"),
		@Finder(signature = "java.util.Collection<com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocal> findExpired(java.sql.Timestamp time)", ejbQl = "SELECT OBJECT(sd) FROM SportsdataMaster sd WHERE sd.expiry <= ?1"),
		@Finder(signature = "java.util.Collection<com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocal> findTestdata()", ejbQl = "SELECT OBJECT(sd) FROM SportsdataMaster sd WHERE sd.messageId NOT LIKE 'ID:%'") })
abstract public class SportsdataEntity extends GenericEntityBean implements EntityBean {

	private MasterEnvelope envelope;

	public Key ejbCreate( Key key, MasterEnvelope envelope ) throws CreateException {
		setId( key.id );
		setEventDate( key.eventDate );
		setReplaces( 0 );
		setUpdates( 0 );
		try {
			setMasterEnvelope( envelope );
		} catch (DocumentException e) {
			final String message = "setMasterDocument failed for " + key;
			logErrorMessage( message, e );
			throw new CreateException( message + ": " + e.getMessage() );
		}
		return null;
	}

	public void ejbPostCreate( Key key, MasterEnvelope envelope ) {}

	@CmpField(column = "event_id", primkeyField = Constants.Bool.TRUE, orderingNumber = "1")
	@LocalMethod
	public abstract String getId();

	public abstract void setId( String id );

	@CmpField(column = "event_date", orderingNumber = "2")
	@LocalMethod
	public abstract Date getEventDate();

	@LocalMethod
	public abstract void setEventDate( Date date );

	@CmpField(column = "document", columnType = CmpField.ColumnType.CLOB, orderingNumber = "3")
	@LocalMethod
	public abstract String getDocument();

	@LocalMethod
	public abstract void setDocument( String document );

	@CmpField(column = "message_id", orderingNumber = "4")
	@LocalMethod
	public abstract String getMessageId();

	public abstract void setMessageId( String messageId );

	@CmpField(column = "message_timestamp", orderingNumber = "5")
	@LocalMethod
	public abstract Timestamp getMessageTimestamp();

	public abstract void setMessageTimestamp( Timestamp time );

	@CmpField(column = "decorated_timestamp", orderingNumber = "6")
	@LocalMethod
	public abstract Timestamp getDecoratedTimestamp();

	public abstract void setDecoratedTimestamp( Timestamp time );

	@CmpField(column = "updated_timestamp", orderingNumber = "7")
	@LocalMethod
	public abstract Timestamp getUpdatedTimestamp();

	public abstract void setUpdatedTimestamp( Timestamp time );

	@CmpField(column = "expiry", orderingNumber = "8")
	public abstract Timestamp getExpiry();

	public abstract void setExpiry( Timestamp time );

	@CmpField(column = "replaces", orderingNumber = "9")
	@LocalMethod
	public abstract Integer getReplaces();

	public abstract void setReplaces( Integer replaces );

	@CmpField(column = "updates", orderingNumber = "10")
	@LocalMethod
	public abstract Integer getUpdates();

	public abstract void setUpdates( Integer updates );

	/**
	 * Map the DOMMasterDocument to document field before writing the record; get the master to
	 * flush itself. This way serialization of the XML and performing any other work as a result of
	 * updating the master is restricted to the end of the transaction.
	 */
	@Override
	public void ejbStore() {
		try {
			if ( envelope != null && (getDocument() == null || envelope.isDirty()) ) {
				setDocument( envelope.getDocument().flatten() );
				setMessageId( envelope.getMessageId() );
				setMessageTimestamp( new Timestamp( envelope.getMessageTimestamp() ) );
				setDecoratedTimestamp( new Timestamp( envelope.getDecoratedTimestamp() ) );
				setUpdatedTimestamp( new Timestamp( System.currentTimeMillis() ) );
				// NOW KEY setDate( envelope.getDocument().getDate() );
				setExpiry( new Timestamp( envelope.getDocument().getExpiry() ) );
				setReplaces( envelope.getReplaces() );
				setUpdates( envelope.getUpdates() );
				envelope.setDirty( false );
				envelope.flush();
			}
		} catch (Exception e) {
			final String message = "Cannot pre-store master " + getId();
			logErrorMessage( message, e );
			throw new EJBException( message, e );
		}
		super.ejbStore();
	}

	/**
	 * Retrieve the envelope, either a cached instance or reconstructed from entity data.
	 * 
	 * @return
	 */
	@LocalMethod
	public MasterEnvelope getMasterEnvelope() {
		if ( envelope == null ) {
			/* Construct master from entity. */
			InputSource input = new InputSource( new StringReader( getDocument() ) );
			try {
				DOMMasterDocument master = new DOMMasterDocument( new DOMAnyDocument(
						getMessageId(), input ) );
				envelope = new MasterEnvelope( getMessageId(), getMessageTimestamp().getTime(),
						master );
				envelope.setDecoratedTimestamp( getDecoratedTimestamp().getTime() );
				envelope.setReplaces( getReplaces() );
				envelope.setUpdates( getUpdates() );
			} catch (DocumentException e) {
				final String message = "getMasterDocument failed for key " + getId();
				logErrorMessage( message, e );
				throw new EJBException( message, e );
			}
		}
		return envelope;
	}

	/**
	 * Replace master in existing entity.
	 * 
	 * @param newMaster
	 * @throws DocumentException
	 */
	@LocalMethod
	public void setMasterEnvelope( MasterEnvelope newEnvelope ) throws DocumentException {
		envelope = newEnvelope;
		setReplaces( getReplaces() + 1 );
		envelope.setDirty( true );
	}

}