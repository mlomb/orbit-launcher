package com.mlomb.minecraft.launcher.versions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mlomb.minecraft.launcher.Launcher;
import com.mlomb.minecraft.launcher.lang.Lang;
import com.mlomb.minecraft.launcher.ui.components.Console;
import com.mlomb.minecraft.launcher.util.Http;

public class Versions {

	private static File file = new File(Launcher.DIRECTORY, "versions");

	public static String lastRelease = "";
	public static String lastSnapshot = "";

	public static LinkedHashMap<String, Version> versions;

	private Versions() {
	}

	public static void load() {
		JSONParser jsonParser = new JSONParser();
		FileReader fileReader = null;
		versions = new LinkedHashMap<String, Version>();

		try {
			fileReader = new FileReader(new File(file, "versions.json"));

			JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
			JSONObject latest = (JSONObject) jsonObject.get("latest");
			lastRelease = (String) latest.get("release");
			lastSnapshot = (String) latest.get("snapshot");

			JSONArray vers = (JSONArray) jsonObject.get("versions");
			Iterator i = vers.iterator();

			String[] directories = file.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return new File(dir, name).isDirectory();
				}
			});

			List<String> dirs = new ArrayList<String>();
			for (int ii = 0; ii < directories.length; ii++) {
				dirs.add(directories[ii]);
			}

			while (i.hasNext()) {
				String json = (String) "" + i.next();
				JSONObject jsonObj = (JSONObject) jsonParser.parse(json);
				String id = (String) jsonObj.get("id");
				if (dirs.contains(id)) {
					dirs.remove(id);
				}
				versions.put(id, new Version(id, (String) jsonObj.get("releaseTime"), (String) jsonObj.get("type"), false));
			}

			for (String id : dirs) {
				if (id.contains("mods_temp")) continue; // TODO Solo para marcar que se hace esta comprobación
				File jarFile = new File(new File(file, id), id + ".jar");
				File jsonFile = new File(new File(file, id), id + ".json");

				if (jarFile.exists() && jsonFile.exists() && !jarFile.isDirectory() && !jsonFile.isDirectory()) {
					parseVersionFromFile(jsonFile);
				} else {
					Console.appendln("Error loading " + id + " missing JAR or JSON.");
				}
			}

			Console.appendln("Loaded " + versions.size() + " versions.");

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, Lang.getText("versionscantload") + "\n\n" + e.getMessage(), "", 0);
			JOptionPane.showMessageDialog(null, Lang.getText("launcherclose"), "", 0);
			System.exit(-1);
		} finally {
			try {
				if (fileReader != null) fileReader.close();
			} catch (IOException e) {
			}
		}
	}

	private static void parseVersionFromFile(File jsonFile) {
		JSONParser jsonParser = new JSONParser();
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(jsonFile);

			JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
			String id = (String) jsonObject.get("id");
			String type = (String) jsonObject.get("type");
			String releaseTime = (String) jsonObject.get("releaseTime");

			versions.put(id, new Version(id, releaseTime, type, true));
		} catch (Exception e) {
			Console.appendln("Error loading custom version: " + e.getMessage());
		} finally {
			try {
				if (fileReader != null) fileReader.close();
			} catch (IOException e) {
			}
		}
	}

	public static void download() {
		Writer writer = null;
		File f = new File(file, "versions.json");
		if (!file.exists()) file.mkdirs();
		try {
			String response = Http.performGet(new URL(Launcher.VERSIONS_URL), Proxy.NO_PROXY);
			if (response != null) {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "utf-8"));
				writer.write(response);
				Console.appendln("Version file downloaded and saved succesfully.");
			} else {
				Console.appendln("Can't download version file.");
			}
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, Lang.getText("versionsdownfail"), "", 0);
			if (!f.exists() && !f.isDirectory()) {
				JOptionPane.showMessageDialog(null, Lang.getText("launcherclose"), "", 0);
				System.exit(-1);
			}
			Console.appendln("Error downloading versions file: " + ex.getMessage());
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}
}