package com.mlomb.minecraft.launcher.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JWindow;

/**
 * This class is open source.
 *
 */
public class SplashScreen extends JWindow {
	private static final long serialVersionUID = 1L;

	Image bi;
	ImageIcon ii;

	boolean loaded = false;

	public SplashScreen(Image img) {
		try {
			bi = img;
			ii = new ImageIcon(bi);
			setSize(ii.getIconWidth(), ii.getIconHeight());
			setLocationRelativeTo(null);
			setBackground(new Color(0, 255, 0, 0));
			loaded = true;
		} catch (Exception exception) {
			Console.append("Can't load splashscreen: " + exception.getMessage());
		}
	}

	public void showSplashScreen(boolean flag) {
		if (!loaded) {
			System.err.println("Splash screen image isn't loaded.");
			return;
		}
		setVisible(flag);
	}

	public void disposeSplashScreen() {
		dispose();
	}

	public void paint(Graphics g) {
		g.drawImage(bi, 0, 0, null);
	}
}