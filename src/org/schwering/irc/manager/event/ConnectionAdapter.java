package org.schwering.irc.manager.event;

/**
 * Adapter for connection listener.
 * @author Christoph Schwering &lt;schwering@gmail.com&gt;
 * @since 2.00
 * @version 1.00
 */
/**
 * @author chs
 *
 */
public class ConnectionAdapter implements ConnectionListener {
	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#channelJoined(org.schwering.irc.manager.event.UserParticipationEvent)
	 */
	public void channelJoined(UserParticipationEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#channelLeft(org.schwering.irc.manager.event.UserParticipationEvent)
	 */
	public void channelLeft(UserParticipationEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#connectionEstablished(org.schwering.irc.manager.event.ConnectionEvent)
	 */
	public void connectionEstablished(ConnectionEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#connectionLost(org.schwering.irc.manager.event.ConnectionEvent)
	 */
	public void connectionLost(ConnectionEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#errorReceived(org.schwering.irc.manager.event.ErrorEvent)
	 */
	public void errorReceived(ErrorEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#invitationReceived(org.schwering.irc.manager.event.InvitationEvent)
	 */
	public void invitationReceived(InvitationEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#motdReceived(org.schwering.irc.manager.event.MOTDEvent)
	 */
	public void motdReceived(MOTDEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#pingReceived(org.schwering.irc.manager.event.PingEvent)
	 */
	public void pingReceived(PingEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#numericErrorReceived(org.schwering.irc.manager.event.NumericEvent)
	 */
	public void numericErrorReceived(NumericEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#numericReplyReceived(org.schwering.irc.manager.event.NumericEvent)
	 */
	public void numericReplyReceived(NumericEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#userModeReceived(org.schwering.irc.manager.event.UserModeEvent)
	 */
	public void userModeReceived(UserModeEvent event) {
	}
	
	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ConnectionListener#whoisReceived(org.schwering.irc.manager.event.WhoisEvent)
	 */
	public void whoisReceived(WhoisEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#banlistReceived(org.schwering.irc.manager.event.BanlistEvent)
	 */
	public void banlistReceived(BanlistEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#channelModeReceived(org.schwering.irc.manager.event.ChannelModeEvent)
	 */
	public void channelModeReceived(ChannelModeEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#messageReceived(org.schwering.irc.manager.event.MessageEvent)
	 */
	public void messageReceived(MessageEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#namesReceived(org.schwering.irc.manager.event.NamesEvent)
	 */
	public void namesReceived(NamesEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#nickChanged(org.schwering.irc.manager.event.NickEvent)
	 */
	public void nickChanged(NickEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#noticeReceived(org.schwering.irc.manager.event.MessageEvent)
	 */
	public void noticeReceived(MessageEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#topicReceived(org.schwering.irc.manager.event.TopicEvent)
	 */
	public void topicReceived(TopicEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#userJoined(org.schwering.irc.manager.event.UserParticipationEvent)
	 */
	public void userJoined(UserParticipationEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.schwering.irc.manager.event.ChannelListener#userLeft(org.schwering.irc.manager.event.UserParticipationEvent)
	 */
	public void userLeft(UserParticipationEvent event) {
	}
}
