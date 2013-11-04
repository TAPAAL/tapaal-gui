package dk.aau.cs.debug;

import java.io.PrintStream;

/* Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk> All rights reserved.  */

public class Logger {

	static PrintStream logDevice = System.out;
	static boolean enableDebug = false;

	public static void changeLogDevice(PrintStream l) {
		logDevice = l;
	}

	public static void enableLogging(boolean b) {
		enableDebug = b;
	}

	public static void log(String log) {
		if (enableDebug) {
			logDevice.println(log);
		}

	}

	public static void log(Object log) {
		if (enableDebug) {
			logDevice.println(log.toString());
		}

	}

}
