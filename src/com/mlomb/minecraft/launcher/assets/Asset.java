package com.mlomb.minecraft.launcher.assets;

public class Asset {

	public String path, realFilename, hash;
	public long size;

	public Asset(String path, String realFilename, String hash, long size) {
		this.path = path;
		this.realFilename = realFilename;
		this.hash = hash;
		this.size = size;
	}
}