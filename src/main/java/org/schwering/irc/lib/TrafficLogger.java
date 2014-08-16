/*
 * IRClib -- A Java Internet Relay Chat library -- class IRCUtil
 * Copyright (C) 2002 - 2006 Christoph Schwering <schwering@gmail.com>
 *
 * This library and the accompanying materials are made available under the
 * terms of the
 * 	- GNU Lesser General Public License,
 * 	- Apache License, Version 2.0 and
 * 	- Eclipse Public License v1.0.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY.
 */
package org.schwering.irc.lib;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 */
public interface TrafficLogger {
	public static final TrafficLogger SYSTEM_OUT = new TrafficLogger() {
		@Override
		public void out(String line) {
			System.out.println("< "+ line);
		}

		@Override
		public void in(String line) {
			System.out.println("> "+ line);
		}
	};
	void in(String line);
	void out(String line);
}
