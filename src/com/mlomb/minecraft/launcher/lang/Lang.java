package com.mlomb.minecraft.launcher.lang;

import java.util.ResourceBundle;

public class Lang {

	static ResourceBundle lang;

	private Lang() {
	}

	static {
		lang = ResourceBundle.getBundle("lang/lang");
	}

	public static String getText(String key) {
		if (lang.containsKey(key)) return lang.getString(key);
		return "?";
	}
}