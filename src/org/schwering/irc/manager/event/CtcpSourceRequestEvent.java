package org.schwering.irc.manager.event;

import org.schwering.irc.manager.Channel;
import org.schwering.irc.manager.Connection;
import org.schwering.irc.manager.User;

/**
 * Fired when a CTCP SOURCE request has been received.
 * @author Christoph Schwering &lt;schwering@gmail.com&gt;
 * @since 2.00
 * @version 1.00
 * @see CtcpListener#sourceRequestReceived(CtcpSourceRequestEvent)
 */
public class CtcpSourceRequestEvent {
	private Connection connection;
	private User sender;
	private User destUser;
	private Channel destChannel;
	private String command;
	private String rest;

	public CtcpSourceRequestEvent(Connection connection, User sender, User destUser,
			String command, String rest) {
		this.connection = connection;
		this.sender = sender;
		this.destUser = destUser;
		this.command = command;
		this.rest = rest;
	}

	public CtcpSourceRequestEvent(Connection connection, User sender,
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
	 * Informs someone where to get this client.
	 * @param host Host where the client can be gotton.
	 * @param dir Directory of the server where the client can be gotton.
	 * @param files Space separated list of files that can be gotton.
	 * @see #reply()
	 */
	public void reply(String host, String dir, String files) {
		String dest = (destUser != null) ? destUser.getNick() 
				: destChannel.getName();
		connection.sendCtcpReply(dest, "SOURCE", host +":"+ dir +":"+ files);
	}
	
	/**
	 * Marks the end of a list of {@link #reply(String, String, String)}s.
	 */
	public void reply() {
		String dest = (destUser != null) ? destUser.getNick() 
				: destChannel.getName();
		connection.sendCtcpReply(dest, "SOURCE");
	}
}
