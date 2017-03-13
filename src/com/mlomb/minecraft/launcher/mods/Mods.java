package com.mlomb.minecraft.launcher.mods;

import java.io.*;
import java.nio.charset.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.mlomb.minecraft.launcher.*;
import com.mlomb.minecraft.launcher.util.*;
import com.mlomb.minecraft.launcher.versions.*;

public class Mods {

	private Mod[] mods_list;
	private File cacheFile;

	public Version v;

	public Mods(Version v) {
		this.v = v;
		cacheFile = new File(Launcher.DIRECTORY, "/cache/" + v.id + ".cache");
	}

	public void load() {
		if (!loadFromCache()) {
			Http.downloadFile("http://olc.pvporbit.com/mods/" + v.id, cacheFile);
			loadFromCache();
		}
	}

	public boolean loadFromCache() {
		if (cacheFile.exists() && !cacheFile.isDirectory()) {
			try {
				String json = Util.readFile(cacheFile.getAbsolutePath(), Charset.forName("UTF-8"));

				JSONObject obj = (JSONObject) new JSONParser().parse(json);

				long unixTime = System.currentTimeMillis() / 1000L;
				long delta = unixTime - (long) obj.get("updated");

				if (delta > (long) obj.get("cacheTime")) return false;

				JSONArray mods = (JSONArray) obj.get("mods");
				mods_list = new Mod[Integer.parseInt("" + obj.get("count"))];
				for (int i = 0; i < mods.size(); i++) {
					JSONObject objm = (JSONObject) mods.get(i);
					mods_list[i] = new Mod(Integer.parseInt("" + objm.get("id")), (String) objm.get("name"), (String) objm.get("file"), "", (long) objm.get("downloads"), (String) objm.get("photo"), (String) objm.get("md5"));
				}
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	public boolean download() {
		return false;
	}

	public Mod getModByID(int id) {
		for (int i = 0; i < mods_list.length; i++)
			if (mods_list[i].id == id) return mods_list[i];
		return null;
	}

	public Mod[] getModList() {
		return mods_list;
	}
}