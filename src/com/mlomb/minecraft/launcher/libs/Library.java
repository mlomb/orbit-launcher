package com.mlomb.minecraft.launcher.libs;

import java.io.File;

import com.mlomb.minecraft.launcher.Launcher;
import com.mlomb.minecraft.launcher.util.Http;
import com.mlomb.minecraft.launcher.util.OS;

public class Library {

	public String _package, name, version;
	public String natLinux, natWindows, natOsx;

	public File file;
	public String downloadPath;
	public boolean isNative = false;

	public Library(String _package, String name, String version, String natLinux, String natWindows, String natOsx) {
		this._package = _package;
		this.name = name;
		this.version = version;
		this.natLinux = natLinux;
		this.natWindows = natWindows;
		this.natOsx = natOsx;

		String sep = System.getProperty("file.separator");

		String finalPath = _package.replace(".", sep) + sep + name + sep + version;
		File base = new File(new File(Launcher.DIRECTORY, "libraries"), finalPath);

		String natives = null;

		switch (OS.getOS()) {
		case "windows":
			natives = natWindows;
			break;
		case "linux":
			natives = natLinux;
			break;
		case "osx":
			natives = natOsx;
			break;
		}

		if (natives == null) {
			isNative = false;
			downloadPath = Launcher.LIBRARIES_DOWNLOAD_BASE + _package.replace(".", "/") + "/" + name + "/" + version + "/" + name + "-" + version + ".jar";
			file = new File(base, name + "-" + version + ".jar");
		} else {
			isNative = true;
			downloadPath = Launcher.LIBRARIES_DOWNLOAD_BASE + _package.replace(".", "/") + "/" + name + "/" + version + "/" + name + "-" + version + "-" + natives + ".jar";
			file = new File(base, name + "-" + version + "-" + natives + ".jar");
		}
	}

	public boolean download() {
		return Http.downloadFile(downloadPath, file);
	}
}