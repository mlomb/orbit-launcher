package com.mlomb.minecraft.launcher.ui.components;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.mlomb.minecraft.launcher.Launcher;

public class Screenshoots {

	private static File DIR;

	public Screenshoots() {
		DIR = new File(Launcher.DIRECTORY, "screenshots");
		if (!DIR.exists()) {
			DIR.mkdirs();
		}
	}

	public static int countScreenshoots() {
		int count = 0;
		for (File file : DIR.listFiles()) {
			if (file.isFile()) {
				count++;
			}
		}
		return count;
	}

	public static void openDirectory() {
		try {
			Desktop.getDesktop().open(DIR);
		} catch (IOException e) {
			Console.appendln("Can't open [" + DIR.getAbsolutePath() + "] Reason: " + e.getMessage());
			e.printStackTrace();
		}
	}
}