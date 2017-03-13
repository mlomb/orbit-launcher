package com.mlomb.minecraft.launcher.util;

public class OS {

	private static String OS = System.getProperty("os.name").toLowerCase();
	private static String osString;

	private OS() {
	}

	public static String getOS() {
		if (osString != null) return osString;
		if (isWindows()) osString = "windows";
		else if (isMac()) osString = "osx";
		else if (isSolaris()) osString = "solaris";
		else
			osString = "NO";
		return osString;
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}
}