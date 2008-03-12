package org.schwering.irc.manager;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.IRCConstants;
import org.schwering.irc.lib.IRCUtil;
import org.schwering.irc.manager.event.BanlistEvent;
import org.schwering.irc.manager.event.ChannelModeEvent;
import org.schwering.irc.manager.event.ConnectionEvent;
import org.schwering.irc.manager.event.ErrorEvent;
import org.schwering.irc.manager.event.InfoEvent;
import org.schwering.irc.manager.event.InvitationEvent;
import org.schwering.irc.manager.event.LinksEvent;
import org.schwering.irc.manager.event.ListEvent;
import org.schwering.irc.manager.event.MotdEvent;
import org.schwering.irc.manager.event.MessageEvent;
import org.schwering.irc.manager.event.NamesEvent;
import org.schwering.irc.manager.event.NickEvent;
import org.schwering.irc.manager.event.NumericEvent;
import org.schwering.irc.manager.event.PingEvent;
import org.schwering.irc.manager.event.TopicEvent;
import org.schwering.irc.manager.event.UnexpectedEvent;
import org.schwering.irc.manager.event.UserModeEvent;
import org.schwering.irc.manager.event.UserParticipationEvent;
import org.schwering.irc.manager.event.UserStatusEvent;
import org.schwering.irc.manager.event.WhoEvent;
import org.schwering.irc.manager.event.WhoisEvent;
import org.schwering.irc.manager.event.WhowasEvent;

/**
 * Distributes the <code>IRCEventListener</code> events to the respective
 * listeners of a <code>Connection</code> object.
 * @author Christoph Schwering &lt;schwering@gmail.com&gt;
 * @since 2.00
 * @version 1.00
 */
class BasicListener implements IRCEventListener {
	private Connection owner;
	private boolean registered = false;
	
	public BasicListener(Connection owner) {
		this.owner = owner;
	}

	/* Connection events */
	
	public void onRegistered() {
		registered = true;
		owner.fireConnectionEstablished(new ConnectionEvent(owner));
	}

	public void onDisconnected() {
		registered = false;
		owner.fireConnectionLost(new ConnectionEvent(owner));
		owner.clearChannels();
	}

	public void onError(String msg) {
		ErrorEvent event = new ErrorEvent(owner, msg);
		owner.fireErrorReceived(event);
	}

	public void onInvite(String chan, IRCUser user, String passiveNick) {
		User invitingUser = owner.resolveUser(user);
		User invitedUser = owner.resolveUser(passiveNick);
		Channel channel = owner.resolveChannel(chan);
		InvitationEvent event = new InvitationEvent(owner, channel, 
				invitingUser, invitedUser);
		owner.fireInvitationReceived(event);
	}

	public void onPing(String ping) {
		PingEvent event = new PingEvent(owner, ping);
		owner.firePingReceived(event);
	}

	public void onMode(IRCUser user, String passiveNick, String mode) {
		User activeUser = owner.resolveUser(user);
		User passiveUser = owner.resolveUser(passiveNick);
		UserModeEvent event = new UserModeEvent(owner, activeUser, passiveUser,
				mode);
		owner.fireUserModeReceived(event);
	}

	public void onError(int num, String msg) {
		if (!registered) { // ask for a new nickname
			switch (num) {
			case IRCConstants.ERR_NICKNAMEINUSE:
			case IRCConstants.ERR_ERRONEUSNICKNAME:
			case IRCConstants.ERR_NONICKNAMEGIVEN:
				String newNick = owner.getNickGenerator().createNewNick();
				if (newNick != null) {
					owner.getIRCConnection().doNick(newNick);
				} else {
					owner.getIRCConnection().doQuit();
				}
			}
		} else {
			NumericEvent event = new NumericEvent(owner, num, null, msg); 
			owner.fireNumericErrorReceived(event);
		}
	}
	
	public void onReply(int num, String val, String msg) {
		System.out.println("onReply("+num+","+val+","+msg+")");
		boolean handled = false;
		try {
			handled |= whoisChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= whowasChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= namesChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= whoChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= topicChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= listChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= banlistChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= handleChannelMode(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= motdChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= infoChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			handled |= linksChain.numericReceived(num, val, msg);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		if (!handled) {
			NumericEvent event = new NumericEvent(owner, num, val, msg); 
			owner.fireNumericReplyReceived(event);
		}
	}
	
	private NumericEventChain topicChain = new NumericEventChain(
			new int[] { IRCConstants.RPL_TOPIC, IRCConstants.RPL_NOTOPIC, IRCConstants.RPL_TOPICINFO }, 
			IRCConstants.RPL_TOPICINFO) {
		protected Object getInitObject(String id) {
			return new Topic(owner.resolveChannel(id));
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			Topic topic = (Topic)obj;
			if (num == IRCConstants.RPL_TOPIC) {
				Message message = new Message(msg);
				topic.setMessage(message);
			} else if (num == IRCConstants.RPL_NOTOPIC) {
				topic.setMessage(null);
			} else if (num == IRCConstants.RPL_TOPICINFO) {
				StringTokenizer tokenizer = new StringTokenizer(val);
				tokenizer.nextToken();
				tokenizer.nextToken();
				User user = owner.resolveUser(tokenizer.nextToken());
				topic.setUser(user);
				try {
					long millis = Long.parseLong(msg) * 1000;
					Date date = new Date(millis);
					topic.setDate(date);
				} catch (Exception exc) {
				}
			}
		}
			
		protected void fire(Object obj) {
			Topic topic = (Topic)obj;
			TopicEvent event = new TopicEvent(owner, topic);
			owner.fireTopicReceived(event);
			topic.getChannel().fireTopicReceived(event);
		}
	};
	
	private NumericEventChain listChain = new NumericEventChain(
			new int[] { IRCConstants.RPL_LISTSTART, IRCConstants.RPL_LIST, IRCConstants.RPL_LISTEND },
			IRCConstants.RPL_LIST,
			IRCConstants.RPL_LISTEND) {
		class TopicList {
			List topics = new LinkedList();
			List visibleCount = new LinkedList();
		}
		
		protected Object getInitObject(String id) {
			return new TopicList();
		}
		
		protected void handle(Object obj, int num, String val, String msg) {
			TopicList list = (TopicList)obj;
			StringTokenizer tokenizer = new StringTokenizer(val +" "+ msg);
			tokenizer.nextToken();
			String channelName = tokenizer.nextToken();
			String visibleCountStr = tokenizer.nextToken();
			String topicStr = (tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : null;
			Channel channel = owner.resolveChannel(channelName);
			int visibleCount;
			try {
				visibleCount = Integer.parseInt(visibleCountStr);
			} catch (Exception exc) {
				visibleCount = -1;
			}
			Message topicMsg = (topicStr != null && topicStr.length() > 0) ? new Message(topicStr) : null;
			Topic topic = new Topic(channel, topicMsg);
			list.topics.add(topic);
			list.visibleCount.add(new Integer(visibleCount));
		}
		
		protected void fire(Object obj) {
			TopicList list = (TopicList)obj;
			ListEvent event = new ListEvent(owner, list.topics, 
					list.visibleCount);
			owner.fireListReceived(event);
		}
		
		protected String getID(int num, String val, String msg) {
			return "";
		}
	};
	
	private NumericEventChain banlistChain = new NumericEventChain(
			new int [] { IRCConstants.RPL_BANLIST, IRCConstants.RPL_ENDOFBANLIST }, 
			IRCConstants.RPL_BANLIST, 
			IRCConstants.RPL_ENDOFBANLIST) {
		class Banlist {
			Channel channel;
			Vector ids = new Vector();
			Vector users = new Vector();
			Vector dates = new Vector();
			
			Banlist(String id) { channel = owner.resolveChannel(id); }
		}
		
		protected Object getInitObject(String id) {
			return new Banlist(id);
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			Banlist banlist = (Banlist)obj;
			String id;
			User user;
			Date date;
			try {
				StringTokenizer tokenizer = new StringTokenizer(val);
				tokenizer.nextToken();
				tokenizer.nextToken();
				id = tokenizer.nextToken();
				String nick = tokenizer.nextToken();
				if (!nick.equals("*")) {
					user = owner.resolveUser(nick);
				} else {
					user = null;
				}
				long millis = Long.parseLong(msg) * 1000;
				date = new Date(millis);
			} catch (Exception exc) {
				id = msg;
				user = null;
				date = null;
			}
			banlist.ids.add(id);
			banlist.users.add(user);
			banlist.dates.add(date);
		}
		
		protected void fire(Object obj) {
			Banlist banlist = (Banlist)obj;
			BanlistEvent event = new BanlistEvent(owner, banlist.channel, 
					banlist.ids, banlist.users, banlist.dates);
			owner.fireBanlistReceived(event);
			banlist.channel.fireBanlistReceived(event);
		}
	};
	
	private NumericEventChain whoisChain = new NumericEventChain(
			new int[] { IRCConstants.RPL_WHOISUSER,
					IRCConstants.RPL_WHOISSERVER,
					IRCConstants.RPL_WHOISCHANNELS,
					IRCConstants.RPL_WHOISOPERATOR,
					IRCConstants.RPL_WHOISIDLE,
					IRCConstants.RPL_WHOISAUTHNAME,
					IRCConstants.RPL_ENDOFWHO },
			new int[] { IRCConstants.RPL_WHOISUSER,
					IRCConstants.RPL_WHOISSERVER,
					IRCConstants.RPL_WHOISCHANNELS,
					IRCConstants.RPL_WHOISOPERATOR,
					IRCConstants.RPL_WHOISIDLE,
					IRCConstants.RPL_WHOISAUTHNAME },
					IRCConstants.RPL_ENDOFWHO
			) {
		class Whois {
			User user;
			String username, host, realName, server, serverInfo, authName;
			boolean operator = false, idle = false;
			Message awayMessage;
			long millisIdle, millisSignon;
			List channels = new LinkedList();
			
			Whois(String id) { user = owner.resolveUser(id); }
		}
		
		protected Object getInitObject(String id) {
			return new Whois(id);
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			Whois whois = (Whois)obj;
			val = skipFirstToken(val);
			if (whois.user == null) {
				String nick = super.getFirstToken(val);
				whois.user = owner.resolveUser(nick);
			}
			val = skipFirstToken(val);
			if (num == IRCConstants.RPL_WHOISUSER) {
				StringTokenizer valTok = new StringTokenizer(val);
				whois.username = valTok.nextToken();
				whois.host = valTok.nextToken();
				whois.realName = msg;
			} else if (num == IRCConstants.RPL_WHOISSERVER) {
				whois.server = val;
				whois.serverInfo = msg;
			} else if (num == IRCConstants.RPL_WHOISOPERATOR) {
				whois.operator = true;
			} else if (num == IRCConstants.RPL_WHOISIDLE) {
				whois.idle = true;
				StringTokenizer valTok = new StringTokenizer(val);
				try {
					whois.millisIdle = Long.parseLong(valTok.nextToken()) * 1000;
				} catch (Exception exc) {
					whois.millisIdle = -1;
					exc.printStackTrace();
				}
				try {
					whois.millisSignon = Long.parseLong(valTok.nextToken()) * 1000;
				} catch (Exception exc) {
					whois.millisSignon = -1;
					exc.printStackTrace();
				}
			} else if (num == IRCConstants.RPL_WHOISCHANNELS) {
				StringTokenizer msgTok = new StringTokenizer(msg);
				while (msgTok.hasMoreTokens()) {
					String tok = msgTok.nextToken();
					whois.channels.add(tok);
				}
			} else if (num == IRCConstants.RPL_WHOISAUTHNAME) {
				whois.authName = val;
			}
		}

		protected void fire(Object obj) {
			Whois whois = (Whois)obj;
			if (whois.username != null && whois.host != null) {
				whois.user.setUsername(whois.username);
				whois.user.setHost(whois.host);
			}
			if (whois.idle) {
				whois.user.setAway(true);
			}
			Date dateSignon = null;
			if (whois.millisSignon != -1) {
				dateSignon = new Date(whois.millisSignon);
			}
			WhoisEvent event = new WhoisEvent(owner, whois.user, 
					whois.realName, whois.authName, whois.server, 
					whois.serverInfo, whois.operator, dateSignon,
					whois.idle, whois.millisIdle, whois.awayMessage, 
					whois.channels);
			owner.fireWhoisReceived(event);
		}
	};
	
	private NumericEventChain whowasChain = new NumericEventChain(
			new int[] { IRCConstants.RPL_WHOWASUSER, IRCConstants.RPL_ENDOFWHOWAS },
			IRCConstants.RPL_WHOWASUSER, 
			IRCConstants.RPL_ENDOFWHOWAS) {
		class Whowas {
			User user;
			String realName;
			
			Whowas(String id) { user = owner.resolveUser(id); }
		}
		
		protected Object getInitObject(String id) {
			return new Whowas(id);
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			Whowas whowas = (Whowas)obj;
			StringTokenizer tokenizer = new StringTokenizer(val);
			tokenizer.nextToken();
			tokenizer.nextToken();
			String userName = tokenizer.nextToken();
			String host = tokenizer.nextToken();
			
			whowas.user.setUsername(userName);
			whowas.user.setHost(host);
			whowas.realName = msg;
		}

		protected void fire(Object obj) {
			Whowas whowas = (Whowas)obj;
			WhowasEvent event = new WhowasEvent(owner, whowas.user,
					whowas.realName);
			owner.fireWhowasReceived(event);
		}
	};
	
	private NumericEventChain namesChain = new NumericEventChain(
			new int[] { IRCConstants.RPL_NAMREPLY, IRCConstants.RPL_ENDOFNAMES }, 
			IRCConstants.RPL_NAMREPLY, 
			IRCConstants.RPL_ENDOFNAMES) {
		class Names {
			Channel channel;
			boolean hasNewUsers = false;
			List channelUsers = new LinkedList();
			
			Names(String id) { channel = owner.resolveChannel(id); }
		}
		
		protected Object getInitObject(String id) {
			return new Names(id);
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			Names names = (Names)obj;
			String str = val +" "+ msg;
			StringTokenizer tokenizer = new StringTokenizer(str);
			tokenizer.nextToken();
			String first = tokenizer.nextToken();
			if (first.equals("@") || first.equals("*") || first.equals("=")) {
				tokenizer.nextToken(); // skip channel name
			}
			while (tokenizer.hasMoreTokens()) {
				String tok = tokenizer.nextToken();
				int status = Channel.NONE;
				if (tok.charAt(0) == '@') {
					status = Channel.OPERATOR;
					tok = tok.substring(1);
				} else if (tok.charAt(0) == '+') {
					status = Channel.VOICED;
					tok = tok.substring(1);
				}
				User user = owner.resolveUser(tok);
				ChannelUser chanUser = names.channel.getUser(user);
				if (chanUser == null) {
					chanUser = new ChannelUser(names.channel, user, status);
					if (owner.hasChannel(names.channel)) {
						names.channel.addUser(chanUser);
						names.hasNewUsers |= true;
					}
				} else if (chanUser != null && chanUser.getStatus() != status) {
					chanUser.addStatus(status);
					UserStatusEvent event = new UserStatusEvent(owner, 
							names.channel, chanUser);
					names.channel.fireUserStatusChanged(event);
				}
				names.channelUsers.add(chanUser);
			}
		}
		
		protected String getID(int num, String val, String msg) {
			if (num == IRCConstants.RPL_NAMREPLY) {
				String str = val +" "+ msg;
				StringTokenizer tokenizer = new StringTokenizer(str);
				tokenizer.nextToken();
				String tok = tokenizer.nextToken();
				if (tok.equals("@") || tok.equals("*") || tok.equals("=")) {
					return tokenizer.nextToken();
				} else {
					return tok;
				}
			} else {
				return super.getID(num, val, msg);
			}
		}
		
		protected void fire(Object obj) {
			Names names = (Names)obj;
			NamesEvent event = new NamesEvent(owner, names.channel, 
					names.hasNewUsers, names.channelUsers);
			owner.fireNamesReceived(event);
			names.channel.fireNamesReceived(event);
		}
	};
	
	private NumericEventChain whoChain = new NumericEventChain(
			new int[] { IRCConstants.RPL_WHOREPLY, IRCConstants.RPL_ENDOFWHO }, 
			IRCConstants.RPL_WHOREPLY, 
			IRCConstants.RPL_ENDOFWHO) {
		class Who {
			Channel channel;
			boolean hasNewUsers = false;
			List channelUsers = new Vector();
			List servers = new Vector();
			List realNames = new Vector();
			List hopcounts = new Vector();
			
			Who(String id) { channel = owner.resolveChannel(id); }
		}
		
		protected Object getInitObject(String id) {
			return new Who(id);
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			Who who = (Who)obj;
			StringTokenizer tokenizer = new StringTokenizer(val);
			tokenizer.nextToken();
			tokenizer.nextToken(); // skip channel name
			String userName = tokenizer.nextToken();
			String host = tokenizer.nextToken();
			String server = tokenizer.nextToken();
			String nick = tokenizer.nextToken();
			String info = tokenizer.nextToken();
			String hopcount = tokenizer.nextToken();
			String realName = msg;
			
			boolean away = info.charAt(0) == 'G';
			char c = info.charAt(info.length()-1);
			int status = Channel.NONE;
			if (c == '@') {
				status = Channel.OPERATOR;
			} else if (c == '+') {
				status = Channel.VOICED;
			}

			User user = owner.resolveUser(nick);
			user.setUsername(userName);
			user.setHost(host);
			ChannelUser chanUser = who.channel.getUser(user);
			if (chanUser == null) {
				chanUser = new ChannelUser(who.channel, user, status);
				if (owner.hasChannel(who.channel)) {
					who.channel.addUser(chanUser);
					who.hasNewUsers |= true;
				}
			} else if (chanUser != null && chanUser.getStatus() != status) {
				chanUser.addStatus(status);
				UserStatusEvent event = new UserStatusEvent(owner, 
						who.channel, chanUser);
				who.channel.fireUserStatusChanged(event);
			}
			chanUser.setAway(away);
			who.channelUsers.add(chanUser);
			who.realNames.add(realName);
			who.servers.add(server);
			who.hopcounts.add(hopcount);
		}
		
		protected String getID(int num, String val, String msg) {
			if (num == IRCConstants.RPL_WHOREPLY) {
				String str = val;
				StringTokenizer tokenizer = new StringTokenizer(str);
				tokenizer.nextToken();
				String tok = tokenizer.nextToken();
				if (tok.equals("@") || tok.equals("*") || tok.equals("=")) {
					return tokenizer.nextToken();
				} else {
					return tok;
				}
			} else {
				return super.getID(num, val, msg);
			}
		}
		
		protected void fire(Object obj) {
			Who who = (Who)obj;
			WhoEvent event = new WhoEvent(owner, who.channel, who.hasNewUsers, 
					who.channelUsers, who.realNames, who.servers, who.hopcounts);
			owner.fireWhoReceived(event);
			who.channel.fireWhoReceived(event);
		}
	};
	
	private NumericEventChain motdChain = new NumericEventChain(
			IRCConstants.RPL_MOTDSTART,
			new int[] { IRCConstants.RPL_MOTDSTART, IRCConstants.RPL_MOTD }, 
			IRCConstants.RPL_ENDOFMOTD
			) {
		protected Object getInitObject(String id) {
			return new LinkedList();
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			List motd = (List)obj;
			motd.add(msg);
		}
		
		protected void fire(Object obj) {
			MotdEvent event = new MotdEvent(owner, (List)obj);
			owner.fireMotdReceived(event);
		}
		
		protected String getID(int num, String val, String msg) {
			return "";
		}
	};
	
	private NumericEventChain infoChain = new NumericEventChain(
			new int[] { IRCConstants.RPL_INFOSTART, IRCConstants.RPL_INFO, IRCConstants.RPL_ENDOFINFO },
			IRCConstants.RPL_INFO,
			IRCConstants.RPL_ENDOFINFO
			) {
		protected Object getInitObject(String id) {
			return new LinkedList();
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			List info = (List)obj;
			info.add(msg);
		}
		
		protected void fire(Object obj) {
			InfoEvent event = new InfoEvent(owner, (List)obj);
			owner.fireInfoReceived(event);
		}
		
		protected String getID(int num, String val, String msg) {
			return "";
		}
	};
	
	private NumericEventChain linksChain = new NumericEventChain(
			new int[] { IRCConstants.RPL_LINKS, IRCConstants.RPL_ENDOFLINKS },
			IRCConstants.RPL_LINKS,
			IRCConstants.RPL_ENDOFLINKS
			) {
		class Links {
			String mask;
			List servers = new Vector();
			List serverInfos = new Vector();
			List hopCounts = new Vector();
			
			Links(String mask) { this.mask = mask; }
		}
		
		protected Object getInitObject(String id) {
			return new Links(id);
		}

		protected void handle(Object obj, int num, String val,
				String msg) {
			Links links = (Links)obj;
			StringTokenizer tokenizer = new StringTokenizer(val +" "+ msg);
			tokenizer.nextToken();
			tokenizer.nextToken();
			String server = tokenizer.nextToken();
			String hopCount = tokenizer.nextToken();
			String info = skipFirstToken(msg);
			links.servers.add(server);
			links.serverInfos.add(info);
			links.hopCounts.add(hopCount);
		}
		
		protected void fire(Object obj) {
			Links links = (Links)obj;
			LinksEvent event = new LinksEvent(owner, links.mask, links.servers, 
					links.serverInfos, links.hopCounts);
			owner.fireLinksReceived(event);
		}
	};
	
	private boolean handleChannelMode(int num, String val, String msg) {
		if (num != IRCConstants.RPL_CHANNELMODEIS) {
			return false;
		}
		String str = skipFirstToken(val +" "+ msg);
		String channelName = getFirstToken(str);
		Channel channel = owner.resolveChannel(channelName);
		str = skipFirstToken(str);
		IRCModeParser modeParser = new IRCModeParser(str);
		ChannelModeEvent event = new ChannelModeEvent(owner, channel, null, 
				modeParser);
		owner.fireChannelModeReceived(event);
		channel.fireChannelModeReceived(event);
		return true;
	}
	
	private static String getFirstToken(String str) {
		str = str.trim();
		for (int i = 0; i < str.length(); i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return str.substring(0, i);
			}
		}
		return str;
	}
	
	private static String skipFirstToken(String str) {
		str = str.trim();
		boolean found = false;
		for (int i = 0; i < str.length(); i++) {
			if (!found && Character.isWhitespace(str.charAt(i))) {
				found = true;
			} else if (found && !Character.isWhitespace(str.charAt(i))) {
				return str.substring(i);
			}
		}
		return "";
	}
	
	public void onNick(IRCUser ircUser, String newNick) {
		User user = owner.resolveUser(ircUser);
		String oldNick = user.getNick();
		user.setNick(newNick);
		NickEvent event = new NickEvent(owner, user, oldNick);
		for (Iterator it = owner.getChannels().iterator(); it.hasNext(); ) {
			Channel channel = (Channel)it.next();
			System.out.println("status(user) = status("+ event.getUser() +") = "+ channel.getUserStatus(event.getUser()));
			System.out.println("status(oldNick) = status("+ event.getOldNick() +") = "+ channel.getUserStatus(event.getOldNick()));
			System.out.println("readding in "+ channel);
			if (channel.hasUser(oldNick)) { // re-add user with new nick
				ChannelUser channelUser = channel.removeUser(oldNick);
				channel.addUser(channelUser);
			}
		}
		owner.fireNickChanged(event);
		for (Iterator it = owner.getChannels().iterator(); it.hasNext(); ) {
			Channel channel = (Channel)it.next();
			if (channel.hasUser(user)) {
				channel.fireNickChanged(event);
			}
		}
	}

	public void onQuit(IRCUser ircUser, String msg) {
		User user = owner.resolveUser(ircUser);
		if (user.getNick().equalsIgnoreCase(owner.getNick())) {
			for (Iterator it = owner.getChannels().iterator(); it.hasNext(); ) {
				Channel channel = (Channel)it.next();
				UserParticipationEvent event = new UserParticipationEvent(owner,
						channel, user, UserParticipationEvent.QUIT, 
						new Message(msg));
				owner.fireChannelLeft(event);
				owner.removeChannel(channel);
			}
		} else {
			UserParticipationEvent event = new UserParticipationEvent(owner,
					null, user, UserParticipationEvent.QUIT, new Message(msg));
			owner.fireUserLeft(event);
			for (Iterator it = owner.getChannels().iterator(); it.hasNext(); ) {
				Channel channel = (Channel)it.next();
				UserParticipationEvent event2 = new UserParticipationEvent(owner,
						channel, user, UserParticipationEvent.QUIT, 
						new Message(msg));
				if (channel.hasUser(user)) {
					channel.fireUserLeft(event2);
					channel.removeUser(user);
				}
			}
		}
	}

	public void unknown(String prefix, String command, String middle,
			String trailing) {
		Object[] args = new Object[] { prefix, command, middle };
		UnexpectedEvent event = new UnexpectedEvent(owner, "unknown", args);
		owner.fireUnexpectedEventReceived(event);
	}
	
	public void onNotice(String target, IRCUser ircUser, String msg) {
		User sender = owner.resolveUser(ircUser);
		if (IRCUtil.isChan(target)) {
			Channel channel = owner.resolveChannel(target);
			MessageEvent event = new MessageEvent(owner, sender, channel, msg);
			owner.fireNoticeReceived(event);
			channel.fireNoticeReceived(event);
		} else {
			User user = owner.resolveUser(target);
			MessageEvent event = new MessageEvent(owner, sender, user, 
					new Message(msg));
			owner.fireNoticeReceived(event);
			owner.firePrivateNoticeReceived(event);
		}
	}

	public void onPrivmsg(String target, IRCUser ircUser, String msg) {
		User sender = owner.resolveUser(ircUser);
		if (IRCUtil.isChan(target)) {
			Channel channel = owner.resolveChannel(target);
			MessageEvent event = new MessageEvent(owner, sender, channel, msg);
			owner.fireMessageReceived(event);
			channel.fireMessageReceived(event);
		} else {
			User user = owner.resolveUser(target);
			MessageEvent event = new MessageEvent(owner, sender, user, 
					new Message(msg));
			owner.fireMessageReceived(event);
			owner.firePrivateMessageReceived(event);
		}
	}
	
	/* Channel events */

	public void onJoin(String chan, IRCUser ircUser) {
		Channel channel = owner.resolveChannel(chan);
		User user = owner.resolveUser(ircUser);
		UserParticipationEvent event = new UserParticipationEvent(owner, 
				channel, user, UserParticipationEvent.JOIN);
		if (user.getNick().equalsIgnoreCase(owner.getNick())) {
			owner.addChannel(channel);
			channel.addUser(user);
			owner.fireChannelJoined(event);
			if (owner.getRequestModes()) {
				owner.getIRCConnection().doMode(chan);
				owner.getIRCConnection().doMode(chan, "+b");
			}
		} else {
			channel.addUser(user);
			owner.fireUserJoined(event);
			channel.fireUserJoined(event);
		}
	}

	public void onPart(String chan, IRCUser ircUser, String msg) {
		Channel channel = owner.resolveChannel(chan);
		User user = owner.resolveUser(ircUser);
		UserParticipationEvent event = new UserParticipationEvent(owner, 
				channel, user, UserParticipationEvent.PART, 
				new Message(msg));
		if (user.getNick().equalsIgnoreCase(owner.getNick())) {
			owner.fireChannelLeft(event);
			channel.removeUser(user);
			owner.removeChannel(channel);
		} else {
			owner.fireUserLeft(event);
			channel.fireUserLeft(event);
			channel.removeUser(user);
		}
	}

	public void onKick(String chan, IRCUser ircUser, String passiveNick, 
			String msg) {
		Channel channel = owner.resolveChannel(chan);
		User user = owner.resolveUser(ircUser);
		User kickingUser = owner.resolveUser(passiveNick);
		User kickedUser = owner.resolveUser(passiveNick);
		UserParticipationEvent event = new UserParticipationEvent(owner, 
				channel, user, UserParticipationEvent.KICK, 
				new Message(msg), kickingUser);
		if (user.getNick().equalsIgnoreCase(owner.getNick())) {
			owner.fireChannelLeft(event);
			channel.removeUser(kickedUser);
			owner.removeChannel(channel);
		} else {
			owner.fireUserLeft(event);
			channel.fireUserLeft(event);
			channel.removeUser(kickedUser);
		}
	}
	
	public void onTopic(String chan, IRCUser ircUser, String msg) {
		Channel channel = owner.resolveChannel(chan);
		Message message = (msg == null || msg.trim().length() > 0) ? new Message(msg) : null;
		User user = owner.resolveUser(ircUser);
		Date date = new Date();
		Topic topic = new Topic(channel, message, user, date);
		channel.setTopic(topic);
		TopicEvent event = new TopicEvent(owner, topic);
		owner.fireTopicReceived(event);
		channel.fireTopicReceived(event);
	}

	public void onMode(String chan, IRCUser ircUser, IRCModeParser modeParser) {
		Channel channel = owner.resolveChannel(chan);
		User user = owner.resolveUser(ircUser);
		ChannelModeEvent event = new ChannelModeEvent(owner, channel, user, 
				modeParser);
		owner.fireChannelModeReceived(event);
		channel.fireChannelModeReceived(event);
		for (int i = 0; i < modeParser.getCount(); i++) { // user status changed
			int mode = modeParser.getModeAt(i);
			if (mode == 'o' || mode == 'v') {
				int status = (mode == 'o') ? Channel.OPERATOR
						: Channel.VOICED;
				int oper = modeParser.getOperatorAt(i);
				String arg = modeParser.getArgAt(i);
				ChannelUser chanUser = channel.getUser(arg);
				int oldStatus = chanUser.getStatus();
				if (oper == '+') {
					chanUser.addStatus(status);
				} else if (oper == '-') {
					chanUser.removeStatus(status);
				}
				if (chanUser.getStatus() != oldStatus) {
					UserStatusEvent e = new UserStatusEvent(owner, channel, 
							chanUser);
					channel.fireUserStatusChanged(e);
				}
			}
		}
	}
}
