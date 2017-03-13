package com.mlomb.minecraft.launcher.util;

import javax.swing.*;

public class Icons {
	public static ImageIcon noimage, grey, grey_h, green, green_h, blue, blue_h, red, red_h, red_large, red_large_h, violet, violet_h, orange, orange_h, add_mod, del_mod, external, download, search, back, view, box, cb, cb_tick;

	public Icons() {
		noimage = load("/noimage.png");
		grey = load("/buttons/grey.png");
		grey_h = load("/buttons/grey_hover.png");
		green = load("/buttons/green.png");
		green_h = load("/buttons/green_hover.png");
		blue = load("/buttons/blue.png");
		blue_h = load("/buttons/blue_hover.png");
		orange = load("/buttons/180x30_orange.png");
		orange_h = load("/buttons/180x30_orange_hover.png");
		red_large = load("/buttons/180x30_red.png");
		red_large_h = load("/buttons/180x30_red_hover.png");
		violet = load("/buttons/180x30_violet.png");
		violet_h = load("/buttons/180x30_violet_hover.png");
		red = load("/buttons/red.png");
		red_h = load("/buttons/red_hover.png");
		cb = load("/icons/cb.png", 32, 32);
		view = load("/icons/view.png", 18, 18);
		external = load("/icons/external.png", 18, 18);
		cb_tick = load("/icons/cb_tick.png", 32, 32);
		box = load("/icons/box.png", 16, 16);
		add_mod = load("/icons/add_mod.png", 18, 18);
		del_mod = load("/icons/del_mod.png", 18, 18);
		download = load("/icons/download.png", 28, 28);
		search = load("/icons/search.png", 20, 20);
		back = load("/icons/back.png", 20, 20);
	}

	private ImageIcon load(String iconName) {
		return new ImageIcon(this.getClass().getResource(iconName));
	}

	private ImageIcon load(String iconName, int w, int h) {
		return resize(load(iconName), w, h);
	}

	public static ImageIcon resize(ImageIcon imgIcon, int w, int h) {
		java.awt.Image image = imgIcon.getImage();
		java.awt.Image newimg = image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(newimg);
	}
}