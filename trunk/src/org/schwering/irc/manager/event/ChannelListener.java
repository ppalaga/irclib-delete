package org.schwering.irc.manager.event;

import java.util.EventListener;

/**
 * Listener for channel-related events. Examples are incoming messages, 
 * nick changes and joining and leaving users.
 * <p>
 * Events that are directly related to a channel the connection participates
 * in are processed by <code>ChannelListener</code>s.
 * However, events like an incoming topic cannot be clearly identified as
 * channel or connection event, because one can also request the topic of
 * channels one doesn't participate in. Therefore, the libraries general
 * strategy is to attribute events to channels if possible and fire them
 * with <code>ChannelListener</code>s. In the case of the topic-example,
 * this means that a <code>ChannelListener.topicReceived</code> is fired
 * if the connection participates in the channel and a 
 * <code>ConnectionListener.topicChanged</code> otherwise.
 * @author Christoph Schwering &lt;schwering@gmail.com&gt;
 * @since 2.00
 * @version 1.00
 */
public interface ChannelListener extends EventListener {
	void userJoined(UserParticipationEvent event);

	void userLeft(UserParticipationEvent event);

	void topicReceived(TopicEvent event);

	/**
	 * Fired when a user in the channel changed his nickname.
	 * <p> 
	 * Note: (1) When this event is fired, the channel's user list is already
	 * updated, i.e. it doesn't contain <code>oldUser</code> but 
	 * <code>newUser</code> and <code>Channel.getUser(oldUser.getNick())</code> 
	 * doesn't return <code>oldUser</code>, and (2) the channels'
	 * <code>nickChanged</code> event is fired before the global
	 * <code>ConnectionListener.nickChanged</code> event. 
	 */
	void nickChanged(NickEvent event);
	
	void channelModeReceived(ChannelModeEvent event);
	
	void privmsgReceived(MessageEvent event);
	
	void noticeReceived(MessageEvent event);
}
