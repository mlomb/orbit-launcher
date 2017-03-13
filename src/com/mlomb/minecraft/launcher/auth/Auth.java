package com.mlomb.minecraft.launcher.auth;

import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.mlomb.minecraft.launcher.ui.*;
import com.mlomb.minecraft.launcher.ui.components.*;
import com.mlomb.minecraft.launcher.util.*;

public class Auth {

	public static String BASE_URL = "https://authserver.mojang.com";

	public static String username = "";
	public static String uuid = "";
	public static String email = "";
	public static String password = "";
	public static String accessToken = "";
	public static String clientToken = "";
	public static boolean login = false;

	public static boolean login() {
		String json = Http.performPayloadPOST(BASE_URL + "/authenticate", "{\"agent\": {\"name\": \"Minecraft\"},\"username\": \"" + email + "\", \"password\": \"" + password + "\"}");
		if (json == null) return false;
		return parse(json);
	}

	private static boolean parse(String json) {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject;
			jsonObject = (JSONObject) jsonParser.parse(json);

			JSONObject selPro = (JSONObject) jsonObject.get("selectedProfile");

			uuid = (String) selPro.get("id");
			username = (String) selPro.get("name");
			if (username == null) return false;

			accessToken = (String) jsonObject.get("accessToken");
			clientToken = (String) jsonObject.get("clientToken");

			login = true;
			GUI.setLogin(true);
			Settings.save();
			
			return true;
		} catch (ParseException e) {
			clear();
			return false;
		}
	}

	public static boolean revalidate() {
		String json = Http.performPayloadPOST(BASE_URL + "/refresh", "{\"accessToken\": \"" + accessToken + "\",\"clientToken\": \"" + clientToken + "\"}");
		if (json == null) {
			accessToken = "";
			clientToken = "";
			return false;
		}
		return parse(json);
	}

	public static void invalidate() {
		clear();
		if (!login) return;
		Http.performPayloadPOST(BASE_URL + "/invalidate", "{\"accessToken\": \"" + accessToken + "\",\"clientToken\": \"" + clientToken + "\"}");

		Settings.save();

		Console.appendln("Session invalidated.");
	}

	private static void clear() {
		accessToken = "";
		clientToken = "";
		password = "";
		username = "";
		uuid = "";
		login = false;
	}
}