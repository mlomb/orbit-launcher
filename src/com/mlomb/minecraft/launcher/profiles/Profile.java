package com.mlomb.minecraft.launcher.profiles;

import java.util.*;

public class Profile {

	public String name, version;
	public int resolutionX, resolutionY;
	public double RAM;
	public boolean experimental;
	public ArrayList<Integer> mods;

	public Profile(String name, String version, boolean experimental, int resolutionX, int resolutionY, double RAM, ArrayList<Integer> mods) {
		this.name = name;
		this.version = version;
		this.experimental = experimental;
		this.resolutionX = resolutionX;
		this.resolutionY = resolutionY;
		this.RAM = RAM;
		this.mods = mods;
	}

	public Profile() {
	}
}