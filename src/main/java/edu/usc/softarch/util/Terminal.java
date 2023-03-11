package edu.usc.softarch.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Terminal {
	private static Instant lastMessage = Instant.MIN;
	public static long period = 10;

	public enum Level {
		OFF (null),
		WARN ("\u001B[31m"),
		INFO ("\u001B[37m"),
		DEBUG ("\u001B[33m");

		private static final String ANSI_RESET = "\u001B[0m";
		private final String color;

		Level(String color) { this.color = color; }

		String colorMessage(String message) {
			return this.color + message + ANSI_RESET; }
	}

	public static Level level = Level.WARN;
	private static final DateTimeFormatter dtf =
		DateTimeFormatter.ofPattern("HH:mm:ss");

	public static void timePrint(String message) {
		if (level == Level.OFF) return;

		LocalTime time = LocalTime.now();

		System.out.println(dtf.format(time) + ": " + message);
	}

	public static synchronized void timePrint(
			String message, Level messageLevel) {
		if ((messageLevel.compareTo(level) > 0)
				|| Duration.between(lastMessage, Instant.now()).getSeconds() < period)
			return;

		lastMessage = Instant.now();
		LocalTime time = LocalTime.now();
		System.out.println(messageLevel.colorMessage(
			dtf.format(time) + ": " + message));
	}

	public static void timePrintError(String message) {
		LocalTime time = LocalTime.now();
		System.err.println("ERROR: " + dtf.format(time) + ": " + message);
	}
}
