package com.mlomb.minecraft.launcher.versions;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.mlomb.minecraft.launcher.*;
import com.mlomb.minecraft.launcher.assets.*;
import com.mlomb.minecraft.launcher.auth.*;
import com.mlomb.minecraft.launcher.lang.*;
import com.mlomb.minecraft.launcher.libs.*;
import com.mlomb.minecraft.launcher.profiles.*;
import com.mlomb.minecraft.launcher.ui.*;
import com.mlomb.minecraft.launcher.ui.components.Console;
import com.mlomb.minecraft.launcher.util.*;

public class Version {

	public String id;
	public String releaseTime;
	public String type;
	public boolean custom, nJar, nJson, virtual;
	private File jar, json, nativesFolder;

	// In Json
	public String minecraftArguments;
	public String assetsVersion;
	public String mainClass;

	public ArrayList<Library> libraries;
	public ArrayList<Asset> assets;
	public ArrayList<Asset> assetsToDownload;

	public Version(String id, String releaseTime, String type, boolean custom) {
		this.id = id;
		this.releaseTime = releaseTime;
		this.type = type;
		this.custom = custom;
	}

	public boolean needDownload() {
		GUI.setTask(Lang.getText("checkingmd5"), "-");

		File dir = new File(new File(Launcher.DIRECTORY, "versions"), id);
		if (!dir.exists()) dir.mkdirs();
		jar = new File(dir, id + ".jar");
		json = new File(dir, id + ".json");

		if (jar.exists() && !jar.isDirectory()) {
			nJar = checkJar();
		} else nJar = true;

		if (json.exists() && !json.isDirectory()) {
			nJson = checkJson();
		} else nJson = true;

		GUI.setTask("-", "-");
		if (nJar || nJson) return true;
		return false;
	}

	private boolean checkJar() {
		GUI.setTask(null, id + ".jar");
		HttpURLConnection response;
		try {
			response = Util.verifyMD5Hash(Util.getMD5Checksum(jar.getAbsolutePath()), new URL(Launcher.VERSIONS_DOWNLOAD_BASE + id + "/" + id + ".jar"));
			int responseStatus = response.getResponseCode();
			if (responseStatus == 304) {
				Console.appendln(id + ".jar file match!");
				return false;
			}
		} catch (Exception e) {
			Console.appendln("Error checking MD5 of " + id + ".jar: " + e.getMessage());
		}
		Console.appendln("Need download: " + id + ".jar");
		return true;
	}

	private boolean checkJson() {
		GUI.setTask(null, id + ".json");
		HttpURLConnection response;
		try {
			response = Util.verifyMD5Hash(Util.getMD5Checksum(json.getAbsolutePath()), new URL(Launcher.VERSIONS_DOWNLOAD_BASE + id + "/" + id + ".json"));
			int responseStatus = response.getResponseCode();
			if (responseStatus == 304) {
				Console.appendln(id + ".json file match!");
				return false;
			}
		} catch (Exception e) {
			Console.appendln("Error checking MD5 of " + id + ".json: " + e.getMessage());
		}
		Console.appendln("Need download: " + id + ".json");
		return true;
	}

	public boolean download() {
		Console.appendln("Downloading version files...");
		GUI.setTask(Lang.getText("downloading"), "-");
		try {
			if (nJar) {
				Console.appendln("Downloading " + id + ".jar");
				GUI.setTask(null, id + ".jar");
				if (Http.downloadFile(Launcher.VERSIONS_DOWNLOAD_BASE + id + "/" + id + ".jar", jar.getAbsolutePath())) {
					Console.appendln(id + ".jar file downloaded succesfully.");
					nJar = false;
				} else {
					Console.appendln("Can't download " + id + ".jar");
					if (jar.exists() && !jar.isDirectory()) Console.appendln("The file " + id + ".jar exists, assuming it's OK.");
					else return false;
				}
			}
			if (nJson) {
				Console.appendln("Downloading " + id + ".json");
				GUI.setTask(null, id + ".json");
				if (Http.downloadFile(Launcher.VERSIONS_DOWNLOAD_BASE + id + "/" + id + ".json", json.getAbsolutePath())) {
					Console.appendln(id + ".json file downloaded succesfully.");
					nJson = false;
				} else {
					Console.appendln("Can't download " + id + ".json");
					if (json.exists() && !json.isDirectory()) Console.appendln("The file " + id + ".json exists, assuming it's OK.");
					else return false;
				}
			}

			GUI.setTask("-", "-");
			return true;
		} catch (Exception ex) {
			Console.appendln("Error downloading versions file: (No ioException)" + ex.getMessage());
			return false;
		}
	}

	public void loadJson() {
		libraries = new ArrayList<Library>();
		JSONParser jsonParser = new JSONParser();
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(json);
			JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);

			minecraftArguments = (String) jsonObject.get("minecraftArguments");
			mainClass = (String) jsonObject.get("mainClass");
			assetsVersion = (String) jsonObject.get("assets");

			JSONArray libs = (JSONArray) jsonObject.get("libraries");

			for (int i = 0; i < libs.size(); i++) {
				String json = (String) "" + libs.get(i);
				JSONObject jsonObj = (JSONObject) jsonParser.parse(json);

				String nameInitial = (String) jsonObj.get("name");
				String natives = null;
				if (jsonObj.containsKey("natives")) natives = (String) "" + jsonObj.get("natives");
				String[] splitted = nameInitial.split(":");

				String _package = splitted[0];
				String name = splitted[1];
				String version = splitted[2];

				String win = null;
				String osx = null;
				String linux = null;

				if (natives != null) {
					JSONObject jsonObj2 = (JSONObject) jsonParser.parse(natives);

					win = (String) jsonObj2.get("windows");
					osx = (String) jsonObj2.get("osx");
					linux = (String) jsonObj2.get("linux");

					if (OS.getOS().equals("windows") && win != null) {
						boolean is64bit = false;
						if (System.getProperty("os.name").contains("Windows")) is64bit = (System.getenv("ProgramFiles(x86)") != null);
						else is64bit = (System.getProperty("os.arch").indexOf("64") != -1);

						if (is64bit) win = win.replace("${arch}", "64");
						else win = win.replace("${arch}", "32");
					}
				}

				libraries.add(new Library(_package, name, version, linux, win, osx));
			}
		} catch (Exception e) {
			Console.appendln("Error loading " + id + ".json : " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (fileReader != null) fileReader.close();
			} catch (IOException e) {
			}
		}
	}

	public boolean checkResources() {
		File baseDirectory = new File(Launcher.DIRECTORY);
		File assets = new File(baseDirectory, "assets");
		File objectsFolder = new File(assets, "objects");
		File indexesFolder = new File(assets, "indexes");
		assetsToDownload = new ArrayList<Asset>();
		long start = System.currentTimeMillis();

		if (assetsVersion == null) assetsVersion = "legacy";

		File indexFile = new File(indexesFolder, assetsVersion + ".json");

		if (!downloadIndexFile(indexFile)) return false;
		String json;
		try {
			json = Util.readFile(indexFile.getAbsolutePath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Console.appendln("Can't load " + indexFile.getAbsolutePath() + ": " + e.getMessage());
			return false;
		}
		try {
			loadAssets(json);
		} catch (ParseException e) {
			Console.appendln("Error parsing: " + indexFile.getName() + ": " + e.getMessage());
			return false;
		}

		GUI.setTask(Lang.getText("checkingassets"), "-");

		for (Asset asset : this.assets) {
			GUI.setTask(null, asset.realFilename);
			String filename = asset.hash.substring(0, 2) + "/" + asset.hash;
			File file = new File(objectsFolder.getAbsolutePath(), filename);
			if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

			if (file.exists()) {
				if (Util.getFileSize(file) != asset.size) assetsToDownload.add(asset);
			} else assetsToDownload.add(asset);
		}

		long end = System.currentTimeMillis();
		long delta = end - start;
		Console.appendln("Need to download " + assetsToDownload.size() + " assets.");
		Console.appendln("Time to compare resources: " + delta + " ms ");
		Console.appendln("IsVirtual: " + virtual);

		if (assetsToDownload.size() == 0) return true;
		return false;
	}

	@SuppressWarnings({ "unchecked" })
	private void loadAssets(String json) throws ParseException {
		GUI.setTask(Lang.getText("checkingassets"), "-");
		Console.appendln("Parsing assets...");

		assets = new ArrayList<Asset>();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(json);

		virtual = jsonObject.containsKey("virtual");

		JSONObject objects = (JSONObject) jsonObject.get("objects");
		Iterator<JSONObject> iterator = objects.values().iterator();

		Set<String> keys = objects.keySet();
		ArrayList<String> list = new ArrayList<String>(keys);

		int i = 0;
		while (iterator.hasNext()) {
			JSONObject jsonChildObject = iterator.next();
			assets.add(new Asset(list.get(i), list.get(i).split("/")[list.get(i).split("/").length - 1], (String) jsonChildObject.get("hash"), (Long) jsonChildObject.get("size")));
			i++;
		}
		GUI.setTask("-", "-");
		Console.appendln(assets.size() + " assets loaded.");
	}

	private boolean downloadIndexFile(File indexFile) {
		Console.appendln("Downloading " + assetsVersion + " index file...");
		String indexUrl = Launcher.RESOURCES_DOWNLOAD + "indexes/" + assetsVersion + ".json";
		indexFile.getParentFile().mkdirs();

		return Http.downloadFile(indexUrl, indexFile);
	}

	public boolean downloadResources() {
		Console.appendln("Downloading assets (" + assetsToDownload.size() + ")...");
		long start = System.currentTimeMillis();
		int ok = 0, error = 0;
		for (Asset asset : assetsToDownload) {
			GUI.setTask(Lang.getText("downloading") + " (" + (assetsToDownload.size() - (ok + error)) + ")", "-");

			String url = Launcher.MOJANG_ASSETS_DOWNLOAD_BASE + asset.hash.substring(0, 2) + "/" + asset.hash;
			File file = new File(Launcher.DIRECTORY, "assets/objects/" + asset.hash.substring(0, 2) + "/" + asset.hash);
			if (Http.downloadFile(url, file, asset.realFilename)) ok++;
			else error++;
		}
		Console.appendln("Downloaded resources | OK: " + ok + " ERROR: " + error);

		long end = System.currentTimeMillis();
		long delta = end - start;
		Console.appendln("Time to download resources: " + delta + " ms ");

		if (error == 0) return true;
		return false;
	}

	public int parseVirtualAssets() {
		final File baseDirectory = new File(Launcher.DIRECTORY);
		final File assets = new File(baseDirectory, "assets");
		final File virtualRoot = new File(new File(assets, "virtual"), "legacy");
		final File objectsFolder = new File(assets, "objects");
		Console.appendln("Reconstructing virtual assets at " + virtualRoot.getAbsolutePath());
		final long start = System.nanoTime();

		int error = 0;

		for (Asset a : this.assets) {
			GUI.setTask(Lang.getText("parsingvassets"), a.realFilename);
			File target = null;
			try {
				target = new File(URLDecoder.decode(virtualRoot + "/" + a.path, "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				error++;
				Console.appendln("Error parsing: " + a.realFilename + " : " + e1.getMessage());
			}
			if (target == null) continue;
			File original = new File(new File(objectsFolder, a.hash.substring(0, 2)), a.hash);
			if (!target.exists()) {
				GUI.setTask(Lang.getText("copying"), a.realFilename);
				File destParent = target.getParentFile();
				destParent.mkdirs();
				try {
					Util.copyFile(original, target);
				} catch (IOException e) {
					error++;
					Console.appendln("Error coping: " + a.realFilename + " : " + e.getMessage());
				}
			}
		}
		final long end = System.nanoTime();
		final long delta = end - start;
		Console.appendln("Time to reconstruct virtual assets: " + delta / 1000000L + " ms ");
		return error;
	}

	public void checkVirtual() {
		if (virtual) {
			int ep = parseVirtualAssets();
			if (ep != 0) Console.appendln("Can't parse virtual assets, skipping.");
		}
	}

	public void prepare() {
		GUI.setTask(Lang.getText("deletingoldnatives"), "-");
		deleteOldNatives();

		nativesFolder = new File(jar.getParentFile(), "natives-" + System.nanoTime());
		if (!nativesFolder.exists()) nativesFolder.mkdirs();

		GUI.setTask(Lang.getText("decompressingnatives"), "-");
		unpackNatives();
		GUI.setTask("-", "-");
	}

	private void unpackNatives() {
		for (Library l : libraries) {
			String nat = null;
			switch (OS.getOS()) {
			case "windows":
				nat = l.natWindows;
				break;
			case "linux":
				nat = l.natLinux;
				break;
			case "osx":
				nat = l.natOsx;
				break;
			}
			if (nat == null) continue;
			GUI.setTask(null, l.name);
			String sep = System.getProperty("file.separator");
			String finalPath = l._package.replace(".", sep) + sep + l.name + sep + l.version + sep + l.name + "-" + l.version + "-" + nat + ".jar";
			File nativeFile = new File(new File(Launcher.DIRECTORY, "libraries"), finalPath);

			if (nativeFile.exists() && !nativeFile.isDirectory()) Util.unZip(nativeFile.getAbsolutePath(), nativesFolder.getAbsolutePath());
			else Console.appendln("Can't unZip: " + l.name);
		}
	}

	public void deleteOldNatives() {
		String[] names = new File(jar.getParentFile().getAbsolutePath()).list();

		if (names == null) return;
		for (String name : names) {
			File newFile = new File(jar.getParentFile(), name);
			if (newFile.isDirectory()) {
				if (name.contains("natives")) {
					Util.deleteDir(newFile);
				}
			}
		}
	}

	public String getCommand(Profile profile) {
		String userType = "\\$\\{user_type\\}";
		String userPropeties = "{}";

		String finalArguments = minecraftArguments;

		finalArguments = finalArguments.replaceAll("\\$\\{auth_player_name\\}", "\"" + Auth.username + "\"");
		finalArguments = finalArguments.replaceAll("\\$\\{version_name\\}", id);
		File game_directory = new File(Launcher.DIRECTORY);
		finalArguments = finalArguments.replaceAll("\\$\\{game_directory\\}", "\"" + game_directory.getAbsolutePath().replaceAll("\\\\", "/") + "\"");
		File assets_directory = new File(Launcher.DIRECTORY + "\\assets");
		finalArguments = finalArguments.replaceAll("\\$\\{assets_root\\}", "\"" + assets_directory.getAbsolutePath().replaceAll("\\\\", "/") + "\"");
		if (!virtual) finalArguments = finalArguments.replaceAll("\\$\\{game_assets\\}", "\"" + assets_directory.getAbsolutePath().replaceAll("\\\\", "/") + "\"");
		else finalArguments = finalArguments.replaceAll("\\$\\{game_assets\\}", "\"" + new File(new File(assets_directory, "virtual"), "legacy").getAbsolutePath().replaceAll("\\\\", "/") + "\"");
		finalArguments = finalArguments.replaceAll("\\$\\{assets_index_name\\}", "\"" + assetsVersion + "\"");
		String uuid = Auth.uuid;
		if (Auth.uuid == null || Auth.uuid.length() == 0) uuid = "\"\"";
		finalArguments = finalArguments.replaceAll("\\$\\{auth_uuid\\}", uuid);
		String aToken = Auth.accessToken;
		if (Auth.accessToken == null || Auth.accessToken.length() == 0) aToken = "\"\"";
		finalArguments = finalArguments.replaceAll("\\$\\{auth_access_token\\}", aToken);
		finalArguments = finalArguments.replaceAll("\\$\\{user_properties\\}", userPropeties); // {'twitch_access_token':'xxxxxxxxx\}
		finalArguments = finalArguments.replaceAll("\\$\\{user_type\\}", userType);
		finalArguments = finalArguments.replaceAll("\\$\\{auth_session\\}", aToken);
		//finalArguments = finalArguments.replaceAll("", "");
		if (profile.resolutionX > 0 || profile.resolutionY > 0) {
			finalArguments += " --width " + profile.resolutionX;
			finalArguments += " --height " + profile.resolutionY;
		}

		String libsPath = "";
		File libsBase = new File(Launcher.DIRECTORY, "libraries");
		String sep = System.getProperty("file.separator");
		for (Library l : libraries) {
			if (l.natLinux != null || l.natOsx != null || l.natWindows != null) continue; // It's native.
			libsPath += new File(libsBase, l._package.replace(".", sep) + sep + l.name + sep + l.version + sep + l.name + "-" + l.version + ".jar").getAbsolutePath() + ";";
		}

		String command = "java -cp ";
		command += "\"" + libsPath + jar.getAbsolutePath() + "\" ";
		if (profile.RAM > 0) {
			command += "-Xmx" + (int) (profile.RAM) + "M ";
			command += "-Xms" + (int) (profile.RAM) + "M ";
		}
		command += "-Djava.library.path=\"" + nativesFolder.getAbsolutePath() + "\" ";
		command += mainClass;
		command += " " + finalArguments;

		command.replaceAll("\\\\", "/");
		return command;
	}
}