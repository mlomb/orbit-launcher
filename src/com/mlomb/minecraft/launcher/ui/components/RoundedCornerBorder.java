package com.mlomb.minecraft.launcher.ui.components;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.border.*;

public class RoundedCornerBorder extends AbstractBorder {
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int r = height - 15;
		RoundRectangle2D round = new RoundRectangle2D.Float(x, y, width - 1, height - 1, r, r);
		Container parent = c.getParent();
		if (parent != null) {
			g2.setColor(parent.getBackground());
			Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
			corner.subtract(new Area(round));
			g2.fill(corner);
		}
		g2.setColor(new Color(21, 126, 251));
		g2.draw(round);
		g2.dispose();
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(2, 2, 2, 2);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.left = insets.right = 8;
		insets.top = insets.bottom = 4;
		return insets;
	}
}