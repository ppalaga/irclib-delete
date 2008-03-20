package org.schwering.irc.manager;

import org.schwering.irc.lib.IRCUtil;

/**
 * Represents a message object that wraps a simple string message.
 * If the connection that belongs to the message object is set to 
 * strip mIRC colorcodes, this object does the job. 
 * @author Christoph Schwering &lt;schwering@gmail.com&gt;
 * @since 2.00
 * @version 1.00
 */
public class Message {
	private Connection conn;
	private String msg;
	
	Message(Connection conn, String msg) {
		this.conn = conn;
		this.msg = msg;
	}
	
	/**
	 * Indicates whether the message is empty.
	 */
	public boolean isEmpty() {
		return msg == null || msg.length() == 0;
	}
	
	/**
	 * Returns the text. If <code>Connection.isColorsEnabled() == false</code>,
	 * potentially present mIRC colorcodes are removed.
	 */
	public String getText() {
		return conn.isColorsEnabled() ? msg : IRCUtil.stripColors(msg);
	}
	
	/**
	 * Returns the same as <code>getText()</code>.
	 * @see #getText()
	 */
	public String toString() {
		return getText();
	}
}
