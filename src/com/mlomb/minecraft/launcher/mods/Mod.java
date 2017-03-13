package com.mlomb.minecraft.launcher.mods;

public class Mod {

	public String name, desc, photo, file, md5;
	public long downloads;

	public int id;

	public enum ModStatus {
		READY, NEED_DOWNLOAD
	}

	public Mod(int id, String name, String file, String desc, long downloads, String photo, String md5) {
		this.id = id;
		this.name = name;
		this.file = file;
		this.desc = desc;
		this.downloads = downloads;
		this.photo = photo;
		this.md5 = md5;
	}
}