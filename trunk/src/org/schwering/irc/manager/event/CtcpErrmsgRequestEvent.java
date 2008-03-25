package org.schwering.irc.manager.event;

import org.schwering.irc.manager.Channel;
import org.schwering.irc.manager.Connection;
import org.schwering.irc.manager.User;

/**
 * Fired when a CTCP ERRMSG request has been received.
 * @author Christoph Schwering &lt;schwering@gmail.com&gt;
 * @since 2.00
 * @version 1.00
 * @see CtcpListener#errmsgRequestReceived(CtcpErrmsgRequestEvent)
 */
public class CtcpErrmsgRequestEvent {
	private Connection connection;
	private User sender;
	private User destUser;
	private Channel destChannel;
	private String command;
	private String rest;

	public CtcpErrmsgRequestEvent(Connection connection, User sender, 
			User destUser, String command, String rest) {
		this.connection = connection;
		this.sender = sender;
		this.destUser = destUser;
		this.command = command;
		this.rest = rest;
	}

	public CtcpErrmsgRequestEvent(Connection connection, User sender,
			Channel destChannel, String command, String rest) {
		this.connection = connection;
		this.sender = sender;
		this.destChannel = destChannel;
		this.command = command;
		this.rest = rest;
	}

	public Connection getConnection() {
		return connection;
	}
	
	public User getSender() {
		return sender;
	}
	
	public Channel getDestinationChannel() {
		return destChannel;
	}
	
	public User getDestinationUser() {
		return destUser;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getArguments() {
		return rest;
	}
	
	/**
	 * Returns the query for which the ERRMSG was requested.
	 * <p>
	 * This is equivalent to <code>getArguments()</code>.
	 */
	public String getQuery() {
		return rest;
	}
	
	/**
	 * Answers another user's ERRMSG request.
	 * @param msg The error message for <code>getQuery()</code>.
	 */
	public void reply(String msg) {
		String dest = (destUser != null) ? destUser.getNick() 
				: destChannel.getName();
		connection.sendCtcpReply(dest, "ERRMSG", getQuery() +" :"+ msg);
	}
}
