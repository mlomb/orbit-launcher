package com.mlomb.minecraft.launcher.profiles;

import java.io.*;
import java.util.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.mlomb.minecraft.launcher.*;
import com.mlomb.minecraft.launcher.lang.*;
import com.mlomb.minecraft.launcher.ui.components.Console;

public class Profiles {

	private static File file = new File(Launcher.DIRECTORY, "profiles.json");

	public static HashMap<String, Profile> profiles;

	private Profiles() {
	}

	public static void load() {
		JSONParser jsonParser = new JSONParser();
		FileReader fileReader = null;
		profiles = new HashMap<String, Profile>();

		try {
			fileReader = new FileReader(file);

			JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
			JSONArray profs = (JSONArray) jsonObject.get("profiles");

			Iterator i = profs.iterator();

			while (i.hasNext()) {
				String json = (String) "" + i.next();
				JSONObject jsonObj = (JSONObject) jsonParser.parse(json);
				String name = (String) jsonObj.get("name");
				String version = (String) jsonObj.get("version");
				int rx = 0;
				int ry = 0;
				double ram = 0;
				boolean experimental = false;
				ArrayList<Integer> mods = null;
				if (jsonObj.containsKey("resolutionX")) rx = (int) (long) jsonObj.get("resolutionX");
				if (jsonObj.containsKey("resolutionY")) ry = (int) (long) jsonObj.get("resolutionY");
				if (jsonObj.containsKey("ram")) ram = (double) jsonObj.get("ram");
				if (jsonObj.containsKey("experimental") && jsonObj.get("experimental").equals("true")) experimental = true;
				if (jsonObj.containsKey("mods")) {
					JSONArray marr = (JSONArray) jsonObj.get("mods");
					mods = new ArrayList<Integer>();
					Iterator it = marr.iterator();
					while (it.hasNext()) {
						mods.add(Integer.parseInt(it.next() + ""));
					}
				}
				if (name == null || version == null) continue;
				profiles.put(name, new Profile(name, version, experimental, rx, ry, ram, mods));
			}
		} catch (Exception e) {
			Console.appendln("Can't load profiles: " + e.getMessage());
			if (!file.exists() && !file.isDirectory()) create();
		}
		// Default
		checkNone();
	}

	private static void create() {
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		obj.put("profiles", list);

		try {
			FileWriter f = new FileWriter(file);
			f.write(obj.toJSONString());
			f.flush();
			f.close();
		} catch (IOException e) {
			Console.appendln("Can't create profiles file: " + e.getMessage());
		}
	}

	public static void save() {
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();

		for (Profile p : profiles.values()) {
			JSONObject o = new JSONObject();

			o.put("name", p.name);
			o.put("version", p.version);
			if (p.resolutionX != 0) o.put("resolutionX", p.resolutionX);
			if (p.resolutionY != 0) o.put("resolutionY", p.resolutionY);
			if (p.RAM != 0) o.put("ram", p.RAM);
			if (p.experimental == true) o.put("experimental", p.experimental + "");
			if (p.mods != null && p.mods.size() != 0) o.put("mods", p.mods);

			list.add(o);
		}

		obj.put("profiles", list);

		try {
			FileWriter f = new FileWriter(file);
			f.write(obj.toJSONString());
			f.flush();
			f.close();
		} catch (IOException e) {
			Console.appendln("Can't update profiles file: " + e.getMessage());
		}
	}

	public static void checkNone() {
		if (profiles.size() == 0) profiles.put(Lang.getText("default"), new Profile(Lang.getText("default"), "-1", false, 0, 0, 0, null));
	}
}