package com.middlewareman.sisfeed.messaging;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import weblogic.ejb.GenericMessageDrivenBean;
import weblogic.ejbgen.Constants;
import weblogic.ejbgen.EjbLocalRef;
import weblogic.ejbgen.EjbLocalRefs;
import weblogic.ejbgen.MessageDriven;
import weblogic.ejbgen.ResourceEnvRef;
import weblogic.ejbgen.ResourceEnvRefs;
import weblogic.ejbgen.ResourceRef;
import weblogic.ejbgen.ResourceRefs;

import com.ibm.mq.MQException;
import com.middlewareman.sisfeed.aggregator.EntityAggregator;
import com.middlewareman.sisfeed.aggregator.IgnoreableMessageException;
import com.middlewareman.sisfeed.sisom.DocumentException;
import com.middlewareman.sisfeed.sportsdata.SportsdataEntityLocalHome;

/**
 * Consumes raw or decorated messages and uses EntityAggregator to process and persist the results.
 * Invalid messages are sent to an error queue. Other messages might trigger a message redelivery
 * request message to be sent. EntityAggregator uses SportsdataEntity to persist and update master
 * documents and to trigger the update of other resources. Global transactions (required and
 * container) should be used, or there is a risk that resources are not updated atomically, for
 * example notification messages being sent before the database update fails.
 * 
 * @author Andreas Nyberg
 */
@ResourceRefs( { @ResourceRef(auth = ResourceRef.Auth.CONTAINER, jndiName = "sisfeed.jms.local.txconnect", name = "jms/rejectConnection", type = "javax.jms.connectionFactory") })
@ResourceEnvRefs( { @ResourceEnvRef(jndiName = "sisfeed.jms.local.error", name = "jms/rejectDestination", type = "javax.jms.Destination") })
@EjbLocalRefs( { @EjbLocalRef(home = "com.middlewareman.sisfeed.sportsdata.SportsdataLocalHome", jndiName = "sisfeed.ejb.sportsdata", local = "com.middlewareman.sisfeed.sportsdata.SportsdataLocal", name = "ejb/sportsdata", type = Constants.RefType.ENTITY) })
//@ForeignJmsProvider(connectionFactoryJndiName = "sisfeed.jms.remote.connect")
@MessageDriven(ejbName = "AggregatorMDB", destinationType = "javax.jms.Queue", transactionType = MessageDriven.MessageDrivenTransactionType.CONTAINER, maxBeansInFreePool = "10", destinationJndiName = "sisfeed.jms.local.decorated", initialBeansInFreePool = "10", jmsClientId = "AggregatorMDB", defaultTransaction = MessageDriven.DefaultTransaction.REQUIRED, maxMessagesInTransaction = 50)
public class AggregatorMDB extends GenericMessageDrivenBean implements MessageDrivenBean,
		MessageListener {

	private static final long serialVersionUID = -6249368789184616926L;

	private static final boolean VERBOSE = true;

	private static final int LOGINTERVAL = 100;

	private SportsdataEntityLocalHome sportsdataHome;

	private UserTransaction userTransaction = null;

	private Connection rejectConnection;

	private Destination rejectDestination;

	private long firstInvocation = 0;

	private int counter = 0;

	@Override
	public void ejbCreate() throws CreateException {
		super.ejbCreate();
		try {
			InitialContext ic = new InitialContext();
			sportsdataHome = (SportsdataEntityLocalHome) ic.lookup( "java:comp/env/ejb/sportsdata" );
			ConnectionFactory rejectConnectionFactory = (ConnectionFactory) ic
					.lookup( "java:comp/env/jms/rejectConnection" );
			rejectConnection = rejectConnectionFactory.createConnection();
			rejectDestination = (Destination) ic.lookup( "java:comp/env/jms/rejectDestination" );
			try {
				userTransaction = getMessageDrivenContext().getUserTransaction();
			} catch (IllegalStateException e) {}
			System.out.println( "Created " + this + " bean-managed " + (userTransaction != null) );
		} catch (Exception e) {
			logErrorMessage( e.getMessage(), e );
			throw new CreateException( e.getMessage() );
		}
	}

	@Override
	public void ejbRemove() {
		super.ejbRemove();
		try {
			rejectConnection.close();
		} catch (JMSException e) {
			logErrorMessage( "Error closing connection", e );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage( Message msg ) {

		if ( VERBOSE && firstInvocation == 0L ) firstInvocation = System.currentTimeMillis();

		try {
			if ( userTransaction != null ) userTransaction.begin();
			MessageRetriever.Envelope envelope = null;
			try {
				envelope = MessageRetriever.retrieve( msg );
				EntityAggregator aggregator = new EntityAggregator( sportsdataHome );
				aggregator.process( envelope.attributes, envelope.document );
				if ( userTransaction != null ) userTransaction.commit();
			} catch (DocumentException e) {
				String reason = e.getMessage();
				logErrorMessage( "REJECTING messageId=" + envelope.attributes.getMessageId() + " "
						+ reason );
				MessageRejector.reject( rejectConnection, rejectDestination, msg, e.getMessage() );
				if ( userTransaction != null ) userTransaction.commit();
			} catch (IgnoreableMessageException e) {
				logErrorMessage( "IGNORING " + e.getMessage() );
				if ( userTransaction != null ) userTransaction.commit();
			}
		} catch (JMSException jmse) {
			logErrorMessage( "JMSException", jmse );
			Exception linked = jmse.getLinkedException();
			if ( linked != null ) {
				logErrorMessage( "linked Exception", linked );
				if ( linked instanceof MQException ) {
					logErrorMessage( "linked MQException reason code: "
							+ ((MQException) linked).reasonCode );
				}
			}
			throw new EJBException( jmse );
		} catch (Exception e) {
			logErrorMessage( "Exception", e );
			throw new EJBException( e );
		}

		if ( VERBOSE && ++counter % LOGINTERVAL == 0 ) {
			final long elapsed = System.currentTimeMillis() - firstInvocation;
			debug( this + " " + counter + ": last " + LOGINTERVAL + " messages in " + elapsed
					+ " ms: " + (elapsed / counter) + " ms/message" );
			firstInvocation = 0L;
			counter = 0;
		}

	}

	private void debug( String message ) {
		System.out.println( message ); // TODO logging
	}

}