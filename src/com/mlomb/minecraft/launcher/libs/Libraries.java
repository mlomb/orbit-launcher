package com.mlomb.minecraft.launcher.libs;

import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;

import com.mlomb.minecraft.launcher.lang.Lang;
import com.mlomb.minecraft.launcher.ui.GUI;
import com.mlomb.minecraft.launcher.ui.components.Console;
import com.mlomb.minecraft.launcher.util.Http;
import com.mlomb.minecraft.launcher.util.OS;
import com.mlomb.minecraft.launcher.util.Util;
import com.mlomb.minecraft.launcher.versions.Version;

public class Libraries {

	public ArrayList<Library> libsToDownload = new ArrayList<Library>();

	public boolean checkLibraries(Version version) {
		for (Library l : version.libraries) {
			GUI.setTask(null, l.name);
			if (!l.file.getParentFile().exists()) l.file.getParentFile().mkdirs();

			if (l.isNative) {
				boolean needByOS = false;
				switch (OS.getOS()) {
				case "windows":
					if (l.natWindows != null) needByOS = true;
					break;
				case "linux":
					if (l.natLinux != null) needByOS = true;
					break;
				case "osx":
					if (l.natOsx != null) needByOS = true;
					break;
				}
				if (!needByOS) continue;
			}

			if (l.file.exists() && !l.file.isDirectory()) {
				try {
					String sha1 = Http.performGet(new URL(l.downloadPath + ".sha1"), Proxy.NO_PROXY);
					if (Util.verifyChecksum(l.file, sha1.substring(0, sha1.length() - 1))) continue;
				} catch (Exception e) {
					Console.appendln("Error checking MD5 of " + l.file.getName() + ": " + e.getMessage());
				}
			}
			libsToDownload.add(l);
		}
		if (libsToDownload.size() >= 1) {
			Console.appendln("Need to download " + libsToDownload.size() + " libraries.");
			return true;
		}
		return false;
	}

	public boolean downloadLibraries() {
		GUI.setTask(Lang.getText("downloading") + " (" + libsToDownload.size() + ")", "-");
		Console.appendln("Downloading libraries and natives: ");

		int ok = 0, error = 0;

		for (Library l : libsToDownload) {
			GUI.setTask(Lang.getText("downloading") + " (" + (libsToDownload.size() - (ok + error)) + ")", "-");
			if (l.download()) ok++;
			else
				error++;
		}
		Console.appendln("Finish downloading libraries and natives: | OK: " + ok + "  ERROR: " + error);
		GUI.setTask("-", "-");
		if (error == 0) return true;
		return false;

		/*
		for (String path : needDownload) {
			String url = null;
			if (path.contains("|")) {
				url = path.split("\\|")[1] + path.split("\\|")[0] + ".pack.xz";
				path = path.split("\\|")[0] + ".pack.xz";
			}
			Launcher.getGUI().setTask(null, path.split("/")[path.split("/").length - 1]);
			File locationFile = new File(LIBS_PATH + "/" + path);

			if (!locationFile.getParentFile().exists()) {
				locationFile.getParentFile().mkdirs();
			}

			if (path.startsWith("net/minecraftforge/")) {
				try {
					Util.copyFile(new File(Launcher.DIRECTORY + "/modsFiles/forge/" + VERSION + "_Forge.jar"), new File(LIBS_PATH + "/" + path.substring(0, path.length() - 8)));
					continue;
				} catch (IOException e) {
					Launcher.getConsole().append("Can't copy forge file(lib): 'net/minecraftforge/forge': " + e.getMessage());
					continue;
				}
			}

			System.out.println("PATH:" + path + "   ---- URL: " + url);

			// Download
			if (!downloadLibrary(path, locationFile, false, url)) {
				// Fail
				if (path.endsWith(".pack.xz")) {
					downloadLibrary(path.substring(0, path.length() - 8), locationFile, false, url.substring(0, url.length() - 8));
				}
				continue;
			} else {
				// Works
				Launcher.getConsole().append(path.split("/")[path.split("/").length - 1] + " downloaded succesfully.");
				Launcher.getGUI().progress.setValue(0);
				// Unpack XZ
				if (!path.endsWith(".pack.xz")) continue;
				try {
					Util.xz(LIBS_PATH + "/" + path, LIBS_PATH + "/" + path.substring(0, path.length() - 3));
					Util.unpackPack200(LIBS_PATH + "/" + path.substring(0, path.length() - 3), new File(LIBS_PATH + "/" + path.substring(0, path.length() - 8)).getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// Download
		}
		*/
	}
}