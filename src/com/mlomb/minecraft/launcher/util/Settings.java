package com.mlomb.minecraft.launcher.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.mlomb.minecraft.launcher.Launcher;
import com.mlomb.minecraft.launcher.auth.Auth;
import com.mlomb.minecraft.launcher.ui.GUI;
import com.mlomb.minecraft.launcher.ui.components.Console;

public class Settings {
	private static Properties prop = new Properties();

	public static String ids = null, selectedProfile = "";
	public static boolean remember;

	public Settings() {
		load();
	}

	public static void save() {
		selectedProfile = GUI.getSelectedProfile();
		if (selectedProfile == null) selectedProfile = "";

		OutputStream output = null;
		try {
			output = new FileOutputStream(new File(Launcher.DIRECTORY, "launcher.config"));

			if (remember) {
				prop.setProperty("accessToken", Auth.accessToken);
				prop.setProperty("clientToken", Auth.clientToken);
				prop.setProperty("displayName", Auth.username);
				prop.setProperty("login", Auth.email);
			} else {
				prop.setProperty("accessToken", "");
				prop.setProperty("clientToken", "");
				prop.setProperty("displayName", "");
				prop.setProperty("login", "");
			}
			prop.setProperty("remember", remember + "");
			prop.setProperty("selectedProfile", selectedProfile);

			prop.store(output, null);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) try {
				output.close();
			} catch (IOException e) {
			}
		}
	}

	public static void load() {
		InputStream input = null;
		try {
			input = new FileInputStream(new File(Launcher.DIRECTORY, "launcher.config"));

			prop.load(input);

			if (prop.containsKey("accessToken")) Auth.accessToken = prop.getProperty("accessToken");
			if (prop.containsKey("clientToken")) Auth.clientToken = prop.getProperty("clientToken");
			if (prop.containsKey("dispalyName")) {
				Auth.username = prop.getProperty("dispalyName");
				ids = prop.getProperty("dispalyName");
			}
			if (prop.containsKey("login")) Auth.email = prop.getProperty("login");
			if (prop.containsKey("selectedProfile")) selectedProfile = prop.getProperty("selectedProfile");
			if (prop.containsKey("remember")) remember = Boolean.parseBoolean(prop.getProperty("remember"));

			Console.appendln("Configuration file loaded.");
		} catch (IOException ex) {
			Console.appendln("Can't load launcher.config, creating.");
			save();
		} finally {
			if (input != null) try {
				input.close();
			} catch (IOException e) {
			}
		}
	}
}