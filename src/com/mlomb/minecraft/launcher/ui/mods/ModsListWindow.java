package com.mlomb.minecraft.launcher.ui.mods;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.FocusManager;

import com.mlomb.minecraft.launcher.*;
import com.mlomb.minecraft.launcher.mods.*;
import com.mlomb.minecraft.launcher.mods.Mod;
import com.mlomb.minecraft.launcher.ui.*;
import com.mlomb.minecraft.launcher.ui.components.*;
import com.mlomb.minecraft.launcher.util.*;
import com.mlomb.minecraft.launcher.util.DownloadTask.DownloadTaskCallback;

public class ModsListWindow extends JDialog {

	private Dimension size = new Dimension(800, 500);
	private int posX, posY;
	private Point mousePos = new Point(0, 0), mouseClickPoint = null;
	private JScrollPane sp;
	private DefaultListModel model;
	private JTextField field;
	private JCheckBox owned;
	private JLabel noResults;

	public ArrayList<Integer> mods_active;
	public ProfileWindow profile;
	private ExecutorService executor;
	private Mods mods;

	public ModsListWindow(final JDialog parent, final ProfileWindow p, ArrayList<Integer> mods_active) {
		super(parent, parent.getTitle() + " - Lista", true);
		setSize(size);
		setLocationRelativeTo(parent);
		setResizable(true);
		setMinimumSize(new Dimension(400, 500));
		setUndecorated(true);
		setBackground(new Color(0, 0, 0, 0));

		if (mods_active == null) this.mods_active = new ArrayList<Integer>();
		else this.mods_active = mods_active;
		mods = new Mods(p.version);

		profile = p;

		try {
			BufferedImage imgs2 = ImageIO.read(ModsListWindow.class.getResource("/background_modsl.png"));
			ImageIcon icon2 = new ImageIcon(imgs2);
			setContentPane(new JLabel(icon2));
		} catch (Exception e) {
		}
		getContentPane().setLayout(new BorderLayout());
		Util.installEscapeCloseOperation(this);

		model = new DefaultListModel();
		if (mods.getModList() != null) {
			for (Mod m : mods.getModList())
				model.addElement(m);
		}
		final JList list = new JList(model);
		sp = new JScrollPane(list);
		list.setLayout(new BorderLayout());
		list.setCellRenderer(new JListRenderer());
		list.setFixedCellHeight(100);
		list.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {
				mousePos = e.getPoint();
				list.repaint(0, 0, list.getWidth(), list.getHeight());
			}

			public void mouseDragged(MouseEvent e) {
			}
		});
		list.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseClickPoint = e.getPoint();
					list.repaint(0, 0, list.getWidth(), list.getHeight());
				}
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
				mouseClickPoint = null;
			}
		});
		final ModsListWindow this_ = this;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onDispose();
			}
		});
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				posX = e.getX();
				posY = e.getY();
				this_.repaint();
			}
		});
		addMouseMotionListener(new MouseAdapter()
		{
			public void mouseDragged(MouseEvent evt)
			{
				setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
			}
		});
		list.setOpaque(false);
		list.setBackground(new Color(0, 0, 0, 0));
		//list.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.red));

		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.setOpaque(false);
		sp.setBackground(new Color(0, 0, 0, 0));
		sp.getViewport().setOpaque(false);
		sp.getVerticalScrollBar().setOpaque(false);
		sp.getVerticalScrollBar().setUI(new BarUI());
		sp.getVerticalScrollBar().setPreferredSize(new Dimension(12, size.height));

		JPanel p2 = new JPanel();
		p2.setBackground(new Color(0, 0, 0, 0));
		p2.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(125, 125, 125)));
		p2.setPreferredSize(new Dimension(200, size.height));
		p2.setLayout(null);

		JButton back = new JButton("Volver");
		back.setHorizontalTextPosition(SwingConstants.CENTER);
		back.setForeground(Color.white);
		back.setFont(GUI.font.deriveFont(14f).deriveFont(Font.BOLD));
		back.setContentAreaFilled(false);
		back.setBorder(BorderFactory.createEmptyBorder());
		back.setSize(180, 30);
		back.setLocation(13, 10);
		back.setIcon(Icons.orange);
		back.setRolloverIcon(Icons.orange_h);
		back.setPressedIcon(Icons.orange_h);
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
				onDispose();
			}
		});
		back.setLayout(null);
		JLabel backIcon = new JLabel(Icons.back);
		backIcon.setSize(18, 18);
		backIcon.setLocation(5, back.getHeight() / 2 - backIcon.getHeight() / 2);
		back.add(backIcon);
		back.setFocusable(false);
		p2.add(back);

		JButton modpack = new JButton("Pack de mods");
		modpack.setHorizontalTextPosition(SwingConstants.CENTER);
		modpack.setForeground(Color.white);
		modpack.setFont(GUI.font.deriveFont(12f).deriveFont(Font.BOLD));
		modpack.setContentAreaFilled(false);
		modpack.setBorder(BorderFactory.createEmptyBorder());
		modpack.setSize(180, 30);
		modpack.setLocation(13, size.height - 40);
		modpack.setFocusable(false);
		modpack.setIcon(Icons.red_large);
		modpack.setRolloverIcon(Icons.red_large_h);
		modpack.setPressedIcon(Icons.red_large_h);
		modpack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(this_, "W.I.P.");
			}
		});
		modpack.setLayout(null);
		JLabel modpackIcon = new JLabel(Icons.box);
		modpackIcon.setSize(18, 18);
		modpackIcon.setLocation(5, modpack.getHeight() / 2 - modpackIcon.getHeight() / 2);
		modpack.add(modpackIcon);
		p2.add(modpack);

		JButton external = new JButton("Agregar externo...");
		external.setHorizontalTextPosition(SwingConstants.CENTER);
		external.setForeground(Color.white);
		external.setFont(GUI.font.deriveFont(10f));
		external.setContentAreaFilled(false);
		external.setBorder(BorderFactory.createEmptyBorder());
		external.setSize(180, 30);
		external.setLocation(13, size.height - 80);
		external.setFocusable(false);
		external.setIcon(Icons.violet);
		external.setRolloverIcon(Icons.violet_h);
		external.setPressedIcon(Icons.violet_h);
		external.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(this_, "W.I.P.");
			}
		});
		external.setLayout(null);
		JLabel externalIcon = new JLabel(Icons.external);
		externalIcon.setSize(18, 18);
		externalIcon.setLocation(5, external.getHeight() / 2 - externalIcon.getHeight() / 2);
		external.add(externalIcon);
		p2.add(external);

		final JLabel modsf = new JLabel((mods.getModList() == null ? "?" : Util.coolFormat(mods.getModList().length, 0)) + " mods (" + profile.version.id + ")");
		modsf.setSize(180, 20);
		modsf.setLocation(13, 45);
		modsf.setFont(GUI.font);
		modsf.setForeground(Color.white);
		modsf.setHorizontalAlignment(SwingConstants.CENTER);
		p2.add(modsf);

		JLabel filter = new JLabel("filtrar");
		filter.setSize(180, 20);
		filter.setLocation(13, 70);
		filter.setFont(GUI.font);
		filter.setForeground(Color.white);
		p2.add(filter);

		/*
			final JComboBox combo1 = new JComboBox();
			combo1.addItem("Popularidad");
			combo1.addItem("Nombre");
			combo1.setSize(180, 30);
			combo1.setFont(GUI.font);
			combo1.setLocation(13, 150);
			combo1.setOpaque(false);
			combo1.setBackground(new Color(0, 0, 0, 0));
			combo1.setBorder(BorderFactory.createEmptyBorder());
			combo1.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				this_.repaint();
				combo1.showPopup();
				this_.repaint();
			}

			@Override
			public void focusLost(FocusEvent e) {
				this_.repaint();
				combo1.hidePopup();
				this_.repaint();
			}
			});
			combo1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				this_.repaint();
			}
			});
			combo1.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
				this_.repaint();
			}

			public void mouseReleased(MouseEvent arg0) {
			}
			});
			combo1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				this_.repaint();

			}
			});
			p2.add(combo1);
			*/

		owned = new JCheckBox();
		updateOwned();
		owned.setSize(180, 13);
		owned.setLocation(10, 140);
		owned.setOpaque(false);
		owned.setRolloverEnabled(false);
		owned.setFont(GUI.font);
		owned.setBackground(new Color(0, 0, 0, 0));
		owned.setFocusable(false);
		owned.setForeground(Color.WHITE);
		p2.add(owned);

		/*
		JRadioButton b = new JRadioButton();
		b.setText("Popularidad");
		b.setSize(180, 16);
		b.setLocation(10, 160);
		b.setOpaque(false);
		b.setRolloverEnabled(false);
		b.setFont(GUI.font);
		b.setBackground(new Color(0, 0, 0, 0));
		b.setFocusable(false);
		b.setForeground(Color.WHITE);
		p2.add(b);
		*/

		JPanel wrappingPanel = new JPanel(new BorderLayout());
		wrappingPanel.setBorder(new RoundedCornerBorder());
		wrappingPanel.setOpaque(false);
		wrappingPanel.setSize(180, 27);
		wrappingPanel.setLocation(13, 100);
		field = new JTextField() {
			@Override
			protected void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);

				if (getText().isEmpty() && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setBackground(Color.gray);
					g2.setFont(getFont().deriveFont(Font.ITALIC));
					g2.drawString("Buscar", 30, 17);
					g2.dispose();
				}
			}
		};
		field.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 5));
		field.setBackground(Color.BLACK);
		field.setForeground(new Color(200, 200, 200));
		field.setCaretColor(field.getForeground());
		field.setFont(GUI.font);
		field.setLayout(null);
		JLabel icon = new JLabel(Icons.search);
		icon.setSize(20, 20);
		icon.setLocation(2, 0);
		field.add(icon);
		wrappingPanel.add(field);
		p2.add(wrappingPanel);

		getContentPane().add(p2, BorderLayout.WEST);
		getContentPane().add(sp, BorderLayout.CENTER);

		noResults = new JLabel("Noy hay resultados");
		noResults.setHorizontalAlignment(SwingConstants.CENTER);
		noResults.setForeground(Color.white);
		noResults.setOpaque(false);
		noResults.setVisible(false);
		noResults.setFont(GUI.font);
		noResults.setBackground(new Color(0, 0, 0, 0));
		field.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					field.setText("");
					return;
				}
				filter();
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}
		});

		owned.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				filter();
			}
		});

		executor = Executors.newFixedThreadPool(5);

		final JLabel Llbl = new JLabel("Cargando mods...");
		Llbl.setHorizontalAlignment(SwingConstants.CENTER);
		Llbl.setForeground(Color.white);
		Llbl.setOpaque(false);
		Llbl.setFont(GUI.font);
		Llbl.setBackground(new Color(0, 0, 0, 0));
		list.add(Llbl, BorderLayout.CENTER);

		Thread t = new Thread() {
			public void run() {
				mods.load();
				if (mods.getModList() == null) Llbl.setText("No se pudieron cargar los mods.");
				else {
					list.remove(Llbl);
					list.add(noResults, BorderLayout.CENTER);
					modsf.setText((mods.getModList() == null ? "?" : Util.coolFormat(mods.getModList().length, 0)) + " mods (" + profile.version.id + ")");
					filter();
				}
			}
		};
		t.start();

		setVisible(true);
	}

	private void updateOwned() {
		owned.setText("Activos (" + (mods_active == null ? 0 : mods_active.size()) + ")");
		repaint();
	}

	protected void onDispose() {
		profile.mods = mods_active;
		profile.updateMods();
		if (executor != null) executor.shutdown();
	}

	protected void filter() {
		model.removeAllElements();
		if (mods.getModList() != null) {
			for (Mod m : mods.getModList())
				if ((field.getText().trim().length() == 0 || m.name.toLowerCase().contains(field.getText().trim().toLowerCase())) && (owned.isSelected() ? mods_active.contains((Integer) m.id) : true)) model.addElement(m);
		}
		noResults.setVisible(model.getSize() == 0);
	}

	class JListRenderer implements ListCellRenderer {

		private ArrayList<Integer> downloadingImages = new ArrayList<Integer>();
		private HashMap<Integer, ImageIcon> images = new HashMap<Integer, ImageIcon>();

		public Component getListCellRendererComponent
				(final JList list, Object value, final int index, final boolean isSelected,
						final boolean cellHasFocus) {

			final Mod mod = ((Mod) (value));

			int y = mousePos.y;
			final int sidepanelWidth = 150;
			final int hoverY = (int) Math.ceil(y / list.getFixedCellHeight());
			Point relativeMouse = new Point(mousePos.x, y - hoverY * list.getFixedCellHeight());
			Point relativeMouseClick = null;
			if (mouseClickPoint != null) relativeMouseClick = new Point(mouseClickPoint.x, mouseClickPoint.y - (int) Math.ceil(mouseClickPoint.y / list.getFixedCellHeight()) * list.getFixedCellHeight());
			JPanel p = new JPanel() {
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					Color selColor = new Color(1.0f, 1.0f, 1.0f, 0.1f);
					/*
					if (mods_active.contains(mod.id)) {
						if (hoverY == index) g.setColor(new Color(81, 139, 212, 50)); // 46, 204, 113, 50
						else g.setColor(new Color(81, 139, 212, 30)); // 46, 204, 113, 30
						g.fillRect(0, 0, getWidth() - sidepanelWidth, getHeight());
					} else 
					*/
					if (hoverY == index) {
						g.setColor(selColor);
						g.fillRect(0, 0, getWidth() - sidepanelWidth, getHeight());
					}
					/*
					Random rand = new Random();
					float r = rand.nextFloat();
					float gg = rand.nextFloat();
					float b = rand.nextFloat();
					g.setColor(new Color(r, gg, b));
					*/
					g.setColor(new Color(230, 230, 230));
					int w = sidepanelWidth;
					int h = list.getFixedCellHeight();
					int xoff = list.getWidth() - w;

					if (sp.getVerticalScrollBar().isShowing()) {
						g.fillRect(xoff, 0, w, h);
						g.drawRect(xoff, 0, w, h);
					} else {
						if (index == 0 || index == 4) {
							int r = 12;
							g.drawRoundRect(xoff, 0, w, h, r, r);
							g.fillRoundRect(xoff, 0, w, h, r, r);
							g.fillRect(xoff, 0, r, h);

							if (index == 0) g.fillRect(xoff + w - r, h - r, r, r);
							else if (index == 4) g.fillRect(xoff + w - r, 0, r, r);
						} else {
							g.fillRect(xoff, 0, w, h);
							g.drawRect(xoff, 0, w, h);
						}
					}

					/*
					Graphics2D g2d = (Graphics2D) g;
					GradientPaint gp1 = new GradientPaint(0, 0, new Color(0, 0, 0, 0), 6, 0, g.getColor(), false);
					g2d.setPaint(gp1);
					g2d.fillRect(xoff - 5, 0, 5, h);
					*/

				}
			};
			p.setLayout(null);
			p.setBackground(list.getBackground());

			JLabel downloads = new JLabel("" + Util.coolFormat(mod.downloads, 0));
			downloads.setIcon(Icons.download);
			downloads.setFont(GUI.font.deriveFont(12f).deriveFont(Font.BOLD));
			downloads.setHorizontalAlignment(SwingConstants.RIGHT);
			downloads.setBounds(list.getWidth() - sidepanelWidth - (list.getWidth() - sidepanelWidth) / 2 - 5, list.getFixedCellHeight() / 2 - 64 / 2, (list.getWidth() - sidepanelWidth) / 2, 20);
			downloads.setForeground(Color.white);
			p.add(downloads);

			JLabel label = new JLabel();
			label.setBounds(5, list.getFixedCellHeight() / 2 - 64 / 2, 128, 64);
			p.add(label);
			if (mod.photo != null) {
				File path = new File(Launcher.DIRECTORY, "cache/");
				final File filepath = new File(path, mod.photo);
				if (!filepath.exists() && !downloadingImages.contains((Integer) mod.id)) {
					if (!path.exists()) path.mkdirs();
					if (!filepath.getParentFile().exists()) filepath.getParentFile().mkdirs();
					downloadingImages.add(mod.id);
					executor.submit(new DownloadTask("http://download.olc.pvporbit.com/d/mods/" + mod.photo, filepath, new DownloadTaskCallback() {
						public void onComplete() {
							ImageIcon img;
							try {
								img = new ImageIcon(ImageIO.read(filepath));
								images.put(mod.id, img);
							} catch (IOException e) {
							}
							list.repaint();
						}
					}));
				} else if (filepath.exists()) {
					ImageIcon img;
					try {
						img = new ImageIcon(ImageIO.read(filepath));
						if (img.getIconWidth() != label.getWidth() || img.getIconHeight() != label.getWidth()) {
							double ratio = (double) img.getIconWidth() / (double) img.getIconHeight();
							int newH = (int) (label.getWidth() / ratio);
							img = Icons.resize(img, label.getWidth(), newH);
						}
						images.put(mod.id, img);
					} catch (IOException e) {
					}
				}
			}
			label.setIcon((images.containsKey((Integer) mod.id) ? images.get((Integer) mod.id) : Icons.noimage));

			JLabel title = new JLabel(mod.name);
			title.setFont(GUI.font.deriveFont(15f).deriveFont(Font.BOLD));
			title.setBounds(label.getWidth() + label.getX() + 5, list.getFixedCellHeight() / 2 - 64 / 2, list.getWidth() - (label.getWidth() + label.getX() + 5) - sidepanelWidth, 20);
			title.setForeground(Color.white);
			p.add(title);

			JTextArea desc = new JTextArea(mod.desc);
			desc.setFont(GUI.font.deriveFont(11f));
			desc.setLineWrap(true);
			desc.setWrapStyleWord(true);
			desc.setBackground(new Color(0, 0, 0, 0));
			desc.setBounds(label.getWidth() + label.getX() + 5, list.getFixedCellHeight() / 2 - 64 / 2 + 20, list.getWidth() - (label.getWidth() + label.getX() + 5) - sidepanelWidth, 40);
			desc.setForeground(Color.white);
			p.add(desc);

			int btn_width = 120;

			JButton send = new JButton("Agregar");
			send.setHorizontalTextPosition(SwingConstants.CENTER);
			send.setForeground(Color.white);
			send.setFont(GUI.font.deriveFont(11f).deriveFont(Font.BOLD));
			send.setContentAreaFilled(false);
			send.setBorder(BorderFactory.createEmptyBorder());
			send.setSize(btn_width, 30);
			send.setLocation(list.getWidth() - send.getWidth() - 10, list.getFixedCellHeight() / 2 - send.getHeight() / 2 - 20);
			if (hoverY == index) {
				if (relativeMouseClick != null && isHover(relativeMouseClick, send)) {
					mouseClickPoint = null;
					if (mods_active.contains((Integer) mod.id)) mods_active.remove((Integer) mod.id);
					else mods_active.add(mod.id);
					updateOwned();
				}
				ImageIcon ic;
				if (!mods_active.contains((Integer) mod.id)) {
					if (isHover(relativeMouse, send)) send.setIcon(Icons.green_h);
					else send.setIcon(Icons.green);
					ic = Icons.add_mod;
				} else {
					send.setText("Quitar");
					if (isHover(relativeMouse, send)) send.setIcon(Icons.red_h);
					else send.setIcon(Icons.red);
					ic = Icons.del_mod;
				}

				send.setLayout(null);
				JLabel addIcon = new JLabel(ic);
				addIcon.setSize(18, 18);
				addIcon.setLocation(5, send.getHeight() / 2 - addIcon.getHeight() / 2);
				send.add(addIcon);
			} else {
				send.setIcon(Icons.grey);
				if (mods_active.contains((Integer) mod.id)) {
					send.setText("Quitar");
					send.setIcon(Icons.red);
				}
			}
			p.add(send);

			JButton view = new JButton("Ver");
			view.setHorizontalTextPosition(SwingConstants.CENTER);
			view.setForeground(Color.white);
			view.setFont(GUI.font.deriveFont(11f).deriveFont(Font.BOLD));
			view.setContentAreaFilled(false);
			view.setBorder(BorderFactory.createEmptyBorder());
			view.setSize(btn_width, 30);
			view.setLocation(list.getWidth() - view.getWidth() - 10, list.getFixedCellHeight() / 2 - view.getHeight() / 2 + 20);
			if (hoverY == index) {
				view.setLayout(null);
				JLabel viewIcon = new JLabel(Icons.view);
				viewIcon.setSize(18, 18);
				viewIcon.setLocation(5, send.getHeight() / 2 - viewIcon.getHeight() / 2);
				view.add(viewIcon);
				if (isHover(relativeMouse, view)) view.setIcon(Icons.blue_h);
				else view.setIcon(Icons.blue);
			}
			else view.setIcon(Icons.grey);
			p.add(view);

			//p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
			return p;
		}

		private boolean isHover(Point pos, JButton btn) {
			return (pos.getX() > btn.getX() && pos.getX() < btn.getX() + btn.getWidth() && pos.getY() > btn.getY() && pos.getY() < btn.getY() + btn.getHeight());
		}
	}
}
