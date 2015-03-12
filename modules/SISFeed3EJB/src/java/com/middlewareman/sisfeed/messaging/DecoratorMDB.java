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
import javax.jms.Session;
import javax.naming.InitialContext;

import com.ibm.mq.MQException;

import weblogic.ejb.GenericMessageDrivenBean;
import weblogic.ejbgen.ForeignJmsProvider;
import weblogic.ejbgen.MessageDriven;
import weblogic.ejbgen.ResourceEnvRef;
import weblogic.ejbgen.ResourceEnvRefs;
import weblogic.ejbgen.ResourceRef;
import weblogic.ejbgen.ResourceRefs;

/**
 * Message-driven bean wrapper for MessageDecorator. When run with bean-managed transactions, the
 * outbound transactional session is cached in the bean instance. For container-managed
 * transactions, when it becomes available for WebSphere MQ and WebLogic Server 9.2, the session
 * must be opened for each request.
 * 
 * @author Andreas Nyberg
 */
@ResourceRefs( { @ResourceRef(jndiName = "sisfeed.jms.local.connect", name = "jms/targetConnection", auth = ResourceRef.Auth.CONTAINER, type = "javax.jms.ConnectionFactory") })
@ResourceEnvRefs( { @ResourceEnvRef(jndiName = "sisfeed.jms.local.decorated", name = "jms/targetDestination", type = "javax.jms.Destination") })
@ForeignJmsProvider(connectionFactoryJndiName = "sisfeed.jms.remote.connect")
@MessageDriven(ejbName = "DecoratorMDB", destinationType = "javax.jms.Queue", acknowledgeMode = MessageDriven.AcknowledgeMode.AUTO_ACKNOWLEDGE, maxBeansInFreePool = "1", destinationJndiName = "sisfeed.jms.remote.sportsdata", initialBeansInFreePool = "1", jmsClientId = "DecoratorMDB", transactionType = MessageDriven.MessageDrivenTransactionType.BEAN)
public class DecoratorMDB extends GenericMessageDrivenBean implements MessageDrivenBean,
		MessageListener {

	private static final long serialVersionUID = 1L;

	private static final boolean VERBOSE = true;

	private static final int LOGINTERVAL = 100;

	private Connection targetConnection;

	private Session targetSession;

	private Destination targetDestination;

	private long firstInvocation = 0;

	private int counter = 0;
	
	// private int failurecounter = 0;

	/**
	 * Look up and open resources to target destination.
	 * 
	 * @see weblogic.ejb.GenericMessageDrivenBean#ejbCreate()
	 */
	@Override
	public void ejbCreate() throws CreateException {
		super.ejbCreate();
		try {
			InitialContext ic = new InitialContext();
			ConnectionFactory targetConnectionFactory = (ConnectionFactory) ic
					.lookup( "java:comp/env/jms/targetConnection" );
			targetDestination = (Destination) ic.lookup( "java:comp/env/jms/targetDestination" );
			targetConnection = targetConnectionFactory.createConnection();
			targetSession = targetConnection.createSession( true, Session.SESSION_TRANSACTED );
		} catch (Exception e) {
			logErrorMessage( e.getMessage(), e );
			throw new CreateException( e.getMessage() );
		}
	}

	/**
	 * Close all resources.
	 * 
	 * @see weblogic.ejb.GenericMessageDrivenBean#ejbRemove()
	 */
	@Override
	public void ejbRemove() {
		try {
			if ( targetSession != null ) targetSession.close();
			if ( targetConnection != null ) targetConnection.close();
		} catch (JMSException e) {
			logErrorMessage( "Closing resources", e );
		}
		super.ejbRemove();
	}

	/**
	 * Use MessageDecorator to decorate incoming message.
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage( Message inbound ) {

		// if ( failurecounter++ < 100 ) throw new EJBException( "failing on purpose" );
		
		if ( VERBOSE && firstInvocation == 0L ) firstInvocation = System.currentTimeMillis();

		try {
			MessageDecorator.processMessage( targetSession, targetDestination, inbound );
			targetSession.commit();
			inbound.acknowledge();
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