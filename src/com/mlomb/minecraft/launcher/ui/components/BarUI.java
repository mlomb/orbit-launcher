package com.mlomb.minecraft.launcher.ui.components;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;

public class BarUI extends BasicScrollBarUI {

	private static final int SCROLL_BAR_ALPHA_ROLLOVER = 150;
	private static final int SCROLL_BAR_ALPHA = 100;
	private static final int THUMB_BORDER_SIZE = 2;
	private static final int THUMB_SIZE = 8;
	private static final Color THUMB_COLOR = Color.BLACK;

	@Override
	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
		g.setColor(new Color(230, 230, 230));

		int roundSize = 20;
		g.fillRoundRect(0, 0, trackBounds.width, trackBounds.height, roundSize, roundSize);
		g.drawRoundRect(0, 0, trackBounds.width, trackBounds.height, roundSize, roundSize);

		g.fillRect(0, 0, trackBounds.width / 2, trackBounds.height);

	}

	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {

		int alpha = isThumbRollover() ? SCROLL_BAR_ALPHA_ROLLOVER : SCROLL_BAR_ALPHA;
		int orientation = scrollbar.getOrientation();
		int arc = THUMB_SIZE;
		int x = thumbBounds.x + THUMB_BORDER_SIZE;
		int y = thumbBounds.y + THUMB_BORDER_SIZE;

		int width = orientation == JScrollBar.VERTICAL ?
				THUMB_SIZE : thumbBounds.width - (THUMB_BORDER_SIZE * 2);
		width = Math.max(width, THUMB_SIZE);

		int height = orientation == JScrollBar.VERTICAL ?
				thumbBounds.height - (THUMB_BORDER_SIZE * 2) : THUMB_SIZE;
		height = Math.max(height, THUMB_SIZE);

		Graphics2D graphics2D = (Graphics2D) g.create();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.setColor(new Color(THUMB_COLOR.getRed(),
				THUMB_COLOR.getGreen(), THUMB_COLOR.getBlue(), alpha));
		graphics2D.fillRoundRect(x, y, width, height, arc, arc);
		graphics2D.dispose();
	}

	@Override
	protected JButton createDecreaseButton(int orientation) {
		return new ScrollBarButton();
	}

	@Override
	protected JButton createIncreaseButton(int orientation) {
		return new ScrollBarButton();
	}

	private static class ScrollBarButton extends JButton {
		private ScrollBarButton() {
			setOpaque(false);
			setFocusable(false);
			setFocusPainted(false);
			setBorderPainted(false);
			setBorder(BorderFactory.createEmptyBorder());
		}
	}
}