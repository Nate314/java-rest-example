package com.nathangawith.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Minimal in-memory brute-force protection: after MAX_FAILURES failed logins
 * for the same (client address, username) within WINDOW_MILLIS, further
 * attempts are refused until the window elapses. State is per process.
 */
@Component
public class LoginThrottle {

	static final int MAX_FAILURES = 5;
	static final long WINDOW_MILLIS = 15 * 60 * 1000L;
	private static final int MAX_TRACKED_KEYS = 10_000;

	private static final class Entry {
		int failures;
		long windowStart;
	}

	private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

	private static String key(String address, String username) {
		return address + "|" + (username == null ? "" : username.toLowerCase());
	}

	public boolean isBlocked(String address, String username) {
		return isBlocked(address, username, System.currentTimeMillis());
	}

	boolean isBlocked(String address, String username, long now) {
		String k = key(address, username);
		Entry e = entries.get(k);
		if (e == null) {
			return false;
		}
		synchronized (e) {
			if (now - e.windowStart >= WINDOW_MILLIS) {
				entries.remove(k, e);
				return false;
			}
			return e.failures >= MAX_FAILURES;
		}
	}

	public void recordFailure(String address, String username) {
		recordFailure(address, username, System.currentTimeMillis());
	}

	void recordFailure(String address, String username, long now) {
		if (entries.size() >= MAX_TRACKED_KEYS) {
			entries.entrySet().removeIf(en -> now - en.getValue().windowStart >= WINDOW_MILLIS);
		}
		Entry e = entries.computeIfAbsent(key(address, username), k -> {
			Entry n = new Entry();
			n.windowStart = now;
			return n;
		});
		synchronized (e) {
			if (now - e.windowStart >= WINDOW_MILLIS) {
				e.windowStart = now;
				e.failures = 0;
			}
			e.failures++;
		}
	}

	public void recordSuccess(String address, String username) {
		entries.remove(key(address, username));
	}
}
