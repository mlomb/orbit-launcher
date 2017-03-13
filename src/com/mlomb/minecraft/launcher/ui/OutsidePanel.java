package com.mlomb.minecraft.launcher.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

public class OutsidePanel extends JPanel {

	private JFrame frame;
	private JPanel mainPanel;

	public OutsidePanel(JFrame frame) {
		this.frame = frame;
		setLayout(new BorderLayout());

		JPanel topPanel = new BorderPanel();
		topPanel.setPreferredSize(new Dimension(0, 30));

		JLabel lbl = new JLabel("HEY!");
		topPanel.add(lbl, FlowLayout.LEFT);

		add(topPanel, BorderLayout.NORTH);
		mainPanel = new JPanel();
		add(mainPanel, BorderLayout.CENTER);
		setBorder(new LineBorder(Color.GRAY, 1));
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	class BorderPanel extends JPanel {

		private JLabel label;
		int pX, pY;

		public BorderPanel() {
			label = new JLabel(" X ");
			label.setOpaque(true);
			label.setBackground(Color.RED);
			label.setForeground(Color.WHITE);

			setLayout(new FlowLayout());
			add(label, FlowLayout.RIGHT);

			label.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					System.exit(0);
				}
			});
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent me) {
					pX = me.getX();
					pY = me.getY();
				}

				public void mouseDragged(MouseEvent me) {
					frame.setLocation(frame.getLocation().x + me.getX() - pX,
							frame.getLocation().y + me.getY() - pY);
					frame.repaint();
				}
			});

			addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent me) {

					frame.setLocation(frame.getLocation().x + me.getX() - pX,
							frame.getLocation().y + me.getY() - pY);
					frame.repaint();
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			int w = getWidth();
			int h = getHeight();
			Color color1 = new Color(22, 22, 22);
			Color color2 = new Color(45, 45, 45);
			GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, w, h);
		}
	}
}
