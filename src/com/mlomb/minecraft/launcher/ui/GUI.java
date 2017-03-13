package com.mlomb.minecraft.launcher.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.mlomb.minecraft.launcher.*;
import com.mlomb.minecraft.launcher.auth.*;
import com.mlomb.minecraft.launcher.lang.*;
import com.mlomb.minecraft.launcher.profiles.*;
import com.mlomb.minecraft.launcher.ui.components.*;
import com.mlomb.minecraft.launcher.ui.components.Console;
import com.mlomb.minecraft.launcher.util.*;
import com.mlomb.minecraft.launcher.versions.*;

/**
 * @author Martin Lombardo
 */
public class GUI {
	private static final long serialVersionUID = 1L;

	private GUI() {
	}

	final static Dimension initialSize = new Dimension(900, 530);

	public static JFrame frame;
	public static ComponentResizer cr;
	public static OutsidePanel outpanel;

	public static JDialog dialog;
	private static ProfileWindow editor;
	// Components
	private static JTabbedPane tabs;
	private static JPanel console;
	private static JPanel mcopt;
	private static JPanel news;
	private static JPanel profEditor;
	private static JPanel panelUserInfo;
	private static JPanel adv;
	private static JPanel lch;
	private static DefaultTableModel profilesTable;
	private static WebDisplay w;
	private static WebDisplay w2;
	private static JLabel info;
	private static JLabel version;
	private static JLabel mods;
	private static JLabel imgFace;
	private static JLabel timePlayed;
	private static JLabel mobsKills;
	private static JLabel deaths;
	private static JLabel cmWalk;
	private static JLabel lbl1;
	private static JLabel lbl2;
	private static JComboBox profList;
	private static JLabel task;
	private static JLabel task2;
	private static JButton login;
	private static JLabel labelSc;
	private static JPanel panelLogin;
	private static JButton disconnect;
	private static JButton play;
	private static GridBagLayout panelLoginLayout;

	public static JCheckBox remember;
	public static JProgressBar progress;
	public static JTextField usr;
	public static JPasswordField pwd;

	public static Font font;

	public static void create() {
		// Window
		initWindow();

		new Icons();

		// Tabs
		tabs = new JTabbedPane(1);

		// Console
		console = new JPanel();
		console.setLayout(new BorderLayout());
		console.add(Console.getComponent());

		// MCOutput
		mcopt = new JPanel();
		mcopt.setLayout(new BorderLayout());
		mcopt.add(MinecraftOutput.getComponent());

		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new File("res/Minecraftia.ttf"));
			font = font.deriveFont(14f);
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}

		// Profile editor
		profEditor = new JPanel();
		profEditor.setLayout(new BorderLayout());
		String txt1 = Lang.getText("profilename"), txt2 = Lang.getText("version"), txt3 = Lang.getText("mods");
		String[] columnas = { txt1, txt2, txt3 };
		profilesTable = new DefaultTableModel(null, columnas) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable table = new JTable(profilesTable);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		final JPopupMenu popup;
		popup = new JPopupMenu();
		JMenuItem newTABS = new JMenuItem(Lang.getText("new"));
		JMenuItem editTABS = new JMenuItem(Lang.getText("edit"));
		JMenuItem removeTABS = new JMenuItem(Lang.getText("delete"));

		try {
			ImageIcon img1 = new ImageIcon(ImageIO.read(Launcher.class.getResource("/icons/add.png")));
			newTABS.setIcon(img1);
			ImageIcon img2 = new ImageIcon(ImageIO.read(Launcher.class.getResource("/icons/edit.png")));
			editTABS.setIcon(img2);
			ImageIcon img3 = new ImageIcon(ImageIO.read(Launcher.class.getResource("/icons/cross.png")));
			removeTABS.setIcon(img3);
		} catch (Exception e2) {
			Console.appendln("Can't load icons: " + e2.getMessage());
		}

		newTABS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newProfile();
			}
		});
		removeTABS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component c = (Component) e.getSource();
				JPopupMenu popup = (JPopupMenu) c.getParent();
				JTable table = (JTable) popup.getInvoker();
				removeProfile((String) table.getValueAt(table.getSelectedRow(), 0));
				updateProfiles();
			}
		});
		editTABS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component c = (Component) e.getSource();
				JPopupMenu popup = (JPopupMenu) c.getParent();
				JTable table = (JTable) popup.getInvoker();
				Profile p = Profiles.profiles.get((table.getValueAt(table.getSelectedRow(), 0)));
				if (p == null) return;
				showProfileDialog(p);
			}
		});
		popup.add(newTABS);
		popup.add(editTABS);
		popup.add(removeTABS);
		table.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JTable source = (JTable) e.getSource();
					int row = source.rowAtPoint(e.getPoint());
					int column = source.columnAtPoint(e.getPoint());
					if (!source.isRowSelected(row)) source.changeSelection(row, column, false, false);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		JScrollPane scrollTable = new JScrollPane(table);
		profEditor.add(scrollTable);

		// News TODO Remake HTTP system.
		w = new WebDisplay(null);
		news = new JPanel();
		news.setLayout(new BorderLayout());
		news.add(w.getComponent());
		news.setBorder(null);

		adv = new JPanel();
		adv.setLayout(new FlowLayout());

		/*
		JButton reportBug = new JButton("Report buugg1-4564");
		reportBug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					java.awt.Desktop.getDesktop().browse(new URI("https://mojang.atlassian.net/browse/MC"));
				} catch (IOException | URISyntaxException e1) {
					Console.append("Can't open webpage 'https://mojang.atlassian.net/browse/MC': " + e1.getMessage());
				}
			}
		});
		adv.add(reportBug); // https://mojang.atlassian.net/browse/MC
		*/

		w2 = new WebDisplay(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent he) {
				if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					System.out.println(he.getDescription());
				}
			}
		});
		lch = new JPanel();
		lch.setLayout(new BorderLayout());
		lch.add(w2.getComponent());
		lch.setBorder(null);

		/*
		JPanel pContact = new JPanel();
		pContact.setBorder(BorderFactory.createTitledBorder(Launcher.getLang().getText("Contacto 468416846")));

		JButton btn1 = new JButton(Launcher.getLang().getText("Envianos un mensaje -86464"));
		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Aún no implementado.\nNot yet implemented.");
			}
		});
		JButton btn2 = new JButton(Launcher.getLang().getText("repotar un bug del launcher 4684987"));
		btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Aún no implementado.\nNot yet implemented.");
			}
		});

		pContact.add(btn1);
		pContact.add(btn2);
		hlp.add(pContact);
		*/

		// Panel
		int panelWidth = 220;
		JPanel panelGeneral = new JPanel();
		panelGeneral.setPreferredSize(new Dimension(panelWidth + 20, 0));

		JPanel panelContainer1 = new JPanel();
		panelContainer1.setPreferredSize(new Dimension(panelWidth, 100));

		// Panel sec
		JPanel panelSec = new JPanel(new GridLayout(0, 1));
		version = new JLabel();
		version.setPreferredSize(new Dimension(panelWidth, 15));
		version.setFont(new Font("Arial", Font.PLAIN, 12));
		mods = new JLabel();
		mods.setPreferredSize(new Dimension(panelWidth, 10));
		mods.setFont(new Font("Arial", Font.PLAIN, 12));
		/*
		forge = new JLabel();
		forge.setPreferredSize(new Dimension(panelWidth, 10));
		forge.setFont(new Font("Arial", Font.PLAIN, 12));
		*/
		panelSec.add(version);
		panelSec.add(mods);
		//panelSec.add(forge);

		// Perfil
		JPanel panelProf = new JPanel();
		panelProf.setBorder(BorderFactory.createTitledBorder(Lang.getText("profiles")));
		panelProf.setPreferredSize(new Dimension(panelWidth + 20, 125));

		// ProfList
		profList = new JComboBox();
		profList.setPreferredSize(new Dimension(136, 20));
		profList.setFont(new Font("Arial", Font.PLAIN, 12));

		updateProfiles();
		setSelectedProfile(Settings.selectedProfile);

		profList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (ie.getStateChange() == ItemEvent.SELECTED) setSelectedProfile((String) profList.getSelectedItem());
			}
		});

		JPanel panelSec2 = new JPanel(new GridLayout(0, 3));
		JButton nuevo = new JButton(Lang.getText("new"));
		JButton editar = new JButton(Lang.getText("edit"));
		JButton borrar = new JButton(Lang.getText("delete"));
		nuevo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showProfileDialog(null);
			}
		});
		editar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Profile p = Profiles.profiles.get((profList.getSelectedItem()));
				if (p == null) return;
				showProfileDialog(p);
			}
		});
		borrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeProfile((String) profList.getSelectedItem());
				updateProfiles();
			}
		});

		panelSec2.add(nuevo);
		panelSec2.add(editar);
		panelSec2.add(borrar);
		panelSec2.setPreferredSize(new Dimension(panelWidth, 20));

		panelContainer1.add(new JLabel(Lang.getText("profile") + ": "));
		panelContainer1.add(profList);
		panelContainer1.add(panelSec);
		panelContainer1.add(panelSec2);
		panelContainer1.setPreferredSize(new Dimension(panelWidth, 100));

		panelProf.add(panelContainer1);
		panelGeneral.add(panelProf);

		// Screenshoots
		JPanel panelScreens = new JPanel(new GridLayout(0, 1));
		panelScreens.setBorder(BorderFactory.createTitledBorder(Lang.getText("screenshoots")));
		panelScreens.setPreferredSize(new Dimension(panelWidth + 20, 75));

		new Screenshoots();
		labelSc = new JLabel("- " + Lang.getText("screenshootstaken"));
		labelSc.setFont(new Font(labelSc.getFont().getName(), Font.BOLD, labelSc.getFont().getSize() + 1));

		JButton btnImg = new JButton(Lang.getText("viewscreenshoots"));
		btnImg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Screenshoots.openDirectory();
			}
		});

		panelScreens.add(labelSc);
		panelScreens.add(btnImg);

		panelGeneral.add(panelScreens);

		// Status
		JPanel status = new JPanel(new GridLayout(0, 1));
		status.setBorder(BorderFactory.createTitledBorder(Lang.getText("status")));
		status.setPreferredSize(new Dimension(panelWidth + 20, 75));

		task = new JLabel();
		task2 = new JLabel();
		task.setFont(new Font(task.getFont().getName(), Font.BOLD, task.getFont().getSize()));
		status.add(task);
		status.add(task2);

		setTask("-", "-");

		progress = new JProgressBar(0, 100);
		progress.setValue(0);
		status.add(progress);

		panelGeneral.add(status);
		// Login

		panelLoginLayout = new GridBagLayout();
		panelLogin = new JPanel(panelLoginLayout);
		panelLogin.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 160, 160)), Lang.getText("play"), TitledBorder.LEADING, TitledBorder.TOP, panelLogin.getFont()));
		panelLogin.setPreferredSize(new Dimension(panelWidth + 20, 90));

		lbl1 = new JLabel(Lang.getText("loginue") + ":");
		usr = new JTextField();
		usr.setText(Auth.email);
		usr.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent e) {
				verifyLoginBT();
			}

			public void keyTyped(KeyEvent e) {
			}
		});

		lbl2 = new JLabel(Lang.getText("password") + ":");
		pwd = new JPasswordField();
		pwd.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent e) {
				verifyLoginBT();
			}

			public void keyTyped(KeyEvent e) {
			}
		});

		info = new JLabel();

		remember = new JCheckBox(Lang.getText("remember"));
		remember.setSelected(Settings.remember);
		remember.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				Settings.remember = !Settings.remember;
			}
		});
		play = new JButton(Lang.getText("play") + "!");
		play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Launcher.startMinecraft();
			}
		});
		login = new JButton(Lang.getText("loginOffline"));
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!Launcher.checkLength()) {
					enableButtons();
					return;
				}
				Launcher.login();
			}
		});
		disconnect = new JButton(Lang.getText("disconnect"));
		disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread("Invalidate") {
					public void run() {
						disableButtons();
						setTask(Lang.getText("disconnecting") + "...", "-");
						Auth.invalidate();
						setLogin(false);
						enableButtons();
					}
				}.start();
			}
		});

		panelGeneral.add(panelLogin);

		// Panel Info User
		JPanel panelUserInfo = new JPanel(null);
		panelUserInfo.setBorder(BorderFactory.createTitledBorder(Lang.getText("userinfo")));
		panelUserInfo.setPreferredSize(new Dimension(panelWidth + 20, 90));

		imgFace = new JLabel();
		reloadSkinImage();

		timePlayed = new JLabel();
		mobsKills = new JLabel();
		deaths = new JLabel();
		cmWalk = new JLabel();

		updateStats();

		panelUserInfo.add(imgFace);
		imgFace.setBounds(10, 15, 64, 64);

		panelUserInfo.add(timePlayed);
		timePlayed.setBounds(79, 20, 300, 10);

		panelUserInfo.add(mobsKills);
		mobsKills.setBounds(79, 35, 300, 10);

		panelUserInfo.add(deaths);
		deaths.setBounds(79, 50, 300, 10);

		panelUserInfo.add(cmWalk);
		cmWalk.setBounds(79, 65, 300, 10);

		panelGeneral.add(panelUserInfo);

		// Add
		tabs.addTab(Lang.getText("news"), news);
		tabs.addTab(Lang.getText("console"), console);
		tabs.addTab(Lang.getText("profileeditor"), profEditor);
		//tabs.addTab(Lang.getText("launcher"), lch);
		tabs.addTab(Lang.getText("advanced"), adv);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panelGeneral, BorderLayout.EAST);
		frame.getContentPane().add(tabs, BorderLayout.CENTER);

		w.setPage(Launcher.NEWS_URL);
		//w2.setPage(Launcher.LAUNCHER_NEWS_URL);
		//adv.add(lch);
		setLogin(Auth.login);
		verifyLoginBT();
	}

	protected static void verifyLoginBT() {
		if (pwd.getText().length() > 0) {
			login.setText(Lang.getText("login"));
			pwd.setBackground(new Color(1f, 1f, 1f));
		} else {
			login.setText(Lang.getText("loginOffline"));
			pwd.setBackground(new Color(0.9f, 0.9f, 0.9f));
		}
	}

	public static void enableButtons() {
		setTask("-", "-");
		usr.setEnabled(true);
		pwd.setEnabled(true);
		remember.setEnabled(true);
		login.setEnabled(true);
		play.setEnabled(true);
		disconnect.setEnabled(true);
		verifyLoginBT();
	}

	public static void disableButtons() {
		pwd.setEnabled(false);
		usr.setEnabled(false);
		remember.setEnabled(false);
		login.setEnabled(false);
		play.setEnabled(false);
		disconnect.setEnabled(false);
	}

	protected static void removeProfile(String string) {
		Profile p = Profiles.profiles.get(string);
		if (p == null) return;
		Object[] options = { Lang.getText("cancel"), Lang.getText("delete") };
		int n = JOptionPane.showOptionDialog(frame, Lang.getText("deleteprofileconfirm") + "\"" + p.name + "\"?", Lang.getText("title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (n == 1) {
			Console.appendln("Profile " + p.name + " removed!");
			Profiles.profiles.remove(p.name);
			Profiles.checkNone();
			Profiles.save();
		}
	}

	protected static void newProfile() {
		showProfileDialog(null);
	}

	public static void setSelectedProfile(String selected) {
		for (int i = 0; i < profList.getItemCount(); i++) {
			String name = (String) profList.getItemAt(i);
			if (name.equals(selected)) {
				Profile profile = Profiles.profiles.get(name);
				if (profile == null) continue;
				String v = profile.version;
				if (profile.version.equals("-1")) {
					String last = Versions.lastRelease;
					if (profile.experimental) last = Versions.lastSnapshot;
					v = Lang.getText("lastupdate") + " (" + last + ")";
				}
				mods.setText(Lang.getText("mods") + ": " + (profile.mods == null ? 0 : profile.mods.size()));
				version.setText(Lang.getText("version") + ": " + v);
				break;
			}
		}
		profList.setSelectedItem(selected);

		Settings.save();
	}

	public static void updateProfiles() {
		int rows = profilesTable.getRowCount();
		for (int i = rows - 1; i >= 0; i--)
			profilesTable.removeRow(i);

		profList.removeAllItems();

		for (Profile profile : Profiles.profiles.values()) {
			String v = profile.version;
			if (profile.version.equals("-1")) {
				String last = Versions.lastRelease;
				if (profile.experimental) last = Versions.lastSnapshot;
				v = Lang.getText("lastupdate") + " (" + last + ")";
			}
			Object[] data = { profile.name, v }; // TODO MODS.
			profilesTable.addRow(data);
			profList.addItem(profile.name);
		}
	}

	private static void initWindow() {
		//SubstanceLookAndFeel.setSkin("org.jvnet.substance.skin.RavenGraphiteSkin"); TODO
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
		}
		frame = new JFrame();

		cr = new ComponentResizer();
		cr.setMinimumSize(new Dimension(600, initialSize.height));
		cr.registerComponent(frame);
		cr.setSnapSize(new Dimension(10, 10));

		/*
		frame.setUndecorated(true);
		outpanel = new OutsidePanel(frame);
		frame.add(outpanel);
		*/
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(Lang.getText("title") + " " + Launcher.LAUNCHER_VERSION);
		frame.pack();
		frame.setSize(initialSize);
		frame.setLocationRelativeTo(null);

		try {
			Image img = null;
			try {
				BufferedImage imgs = ImageIO.read(Launcher.class.getResource("/icon.png"));
				img = imgs;
			} catch (Exception e) {
			}
			frame.setIconImage(img);
		} catch (Exception e) {
			System.out.println("Can't load windowIcon: " + e.getMessage());
		}
	}

	public static void updateStats() {
		int countsc = Screenshoots.countScreenshoots();
		String add = "";
		if (countsc < 5) add = "D':";
		else if (countsc < 10) add = "D:";
		else if (countsc < 15) add = ":'C";
		else if (countsc < 20) add = ":C";
		else if (countsc < 25) add = ":'(";
		else if (countsc < 30) add = ":(";
		else if (countsc < 35) add = ":|";
		else if (countsc < 50) add = ":S";
		else if (countsc < 75) add = ":)";
		else if (countsc < 100) add = "C:";
		else add = ":D";
		labelSc.setText(countsc + " " + Lang.getText("screenshootstaken") + "     " + add);
		File folder = new File(Launcher.DIRECTORY, "saves");
		if (!folder.exists()) {
			folder.mkdirs();
			return;
		}
		File[] listOfFiles = folder.listFiles();
		String files;

		int secsPlayed = 0;
		int mobsKilled = 0;
		int gameDeaths = 0;
		double walked = 0;

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
				files = listOfFiles[i].getName();
				File nf = new File(folder.getAbsolutePath() + "/" + files + "/stats");
				if (!nf.exists()) continue;

				File[] listOfFiles2 = nf.listFiles();
				String files2;
				for (int i2 = 0; i2 < listOfFiles2.length; i2++) {
					if (listOfFiles2[i2].isFile()) {
						files2 = listOfFiles2[i2].getName();
						if (files2.endsWith(".json")) {
							// File .json
							JSONParser jsonParser = new JSONParser();
							FileReader fileReader = null;

							try {
								fileReader = new FileReader(nf.getAbsolutePath() + "/" + files2);

								JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
								if (jsonObject.get("stat.playOneMinute") != null) secsPlayed += Integer.parseInt(jsonObject.get("stat.playOneMinute") + "") / 20;

								if (jsonObject.get("stat.mobKills") != null) mobsKilled += Integer.parseInt(jsonObject.get("stat.mobKills") + "");

								if (jsonObject.get("stat.deaths") != null) gameDeaths += Integer.parseInt(jsonObject.get("stat.deaths") + "");

								if (jsonObject.get("stat.walkOneCm") != null) walked += Integer.parseInt(jsonObject.get("stat.walkOneCm") + "");
							} catch (Exception e) {
							} finally {
								jsonParser = null;
								try {
									if (fileReader != null) fileReader.close();
								} catch (IOException e) {
								}
							}
						}
					}
				}
			}
		}

		timePlayed.setText(Lang.getText("timeplayed") + ": " + Util.getRedeableTime(secsPlayed * 1000));
		mobsKills.setText(Lang.getText("mobskilled") + ": " + mobsKilled);
		deaths.setText(Lang.getText("deaths") + ": " + gameDeaths);
		cmWalk.setText(Lang.getText("walked") + ": " + Util.round(walked / 100, 2) + "m");
	}

	public static void reloadSkinImage() {
		try {
			File faceFile = new File(Launcher.DIRECTORY, "head.png");
			BufferedImage img;
			if (faceFile.exists()) {
				img = ImageIO.read(faceFile);
			} else {
				img = ImageIO.read(GUI.class.getResource("/defaulthead.png"));
			}

			int size = 64;
			Image imgd = img.getScaledInstance(size, size, Image.SCALE_DEFAULT);
			ImageIcon icon = new ImageIcon(imgd);
			imgFace.setIcon(icon);
		} catch (IOException e1) {
			Console.appendln("Can't load head defaultImage.");
		}
	}

	public static void showProfileDialog(Profile perfil) {
		dialog = new JDialog(frame, Lang.getText("profileeditor"), true);
		editor = new ProfileWindow(perfil, frame);
		dialog.getContentPane().add(editor);
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setResizable(false);
		dialog.setVisible(true);
	}

	public static void destroyProfileDialog() {
		editor = null;
		dialog.dispose();
		dialog = null;
	}

	public static void setTask(String title, String sub) {
		if (title != null) task.setText(Lang.getText("task") + ": " + title);
		if (sub != null) task2.setText(sub);
	}

	public static void setVisible(boolean b) {
		frame.setVisible(b);
	}

	public static String getSelectedProfile() {
		if (profList == null) return null;
		return (String) profList.getSelectedItem();
	}

	public static void setLogin(boolean flag) {
		GridBagConstraints c = new GridBagConstraints();
		if (flag) {
			downloadHead(Auth.username);

			panelLogin.remove(lbl1);
			panelLogin.remove(usr);
			panelLogin.remove(lbl2);
			panelLogin.remove(pwd);
			panelLogin.remove(login);
			panelLogin.remove(remember);

			// Add
			info.setText(Lang.getText("welcome").replace("%name%", Auth.username));
			c.gridx = 0;
			c.gridwidth = 2;
			panelLogin.add(info, c);

			if (Auth.login) {
				play.setText(Lang.getText("play") + "!");
				c.gridwidth = 1;
			} else play.setText(Lang.getText("playoffline"));

			c.gridx = 0;
			c.gridy = 1;
			panelLogin.add(play, c);

			if (Auth.login) {
				c.gridx = 1;
				c.gridy = 1;
			} else {
				c.gridx = 0;
				c.gridy = 2;
			}
			panelLogin.add(disconnect, c);
		} else {
			panelLogin.remove(info);
			panelLogin.remove(play);
			panelLogin.remove(disconnect);

			// Add
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.5;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 0;
			panelLogin.add(lbl1, c);
			c.gridx = 1;
			c.gridy = 0;
			panelLogin.add(usr, c);
			c.gridx = 0;
			c.gridy = 1;
			panelLogin.add(lbl2, c);
			c.gridx = 1;
			c.gridy = 1;
			panelLogin.add(pwd, c);
			c.gridx = 0;
			c.gridy = 2;
			panelLogin.add(login, c);
			c.gridx = 1;
			c.gridy = 2;
			panelLogin.add(remember, c);
		}
		// Update the frame
		frame.setSize(frame.getSize().width + 1, frame.getSize().height + 1);
		frame.setSize(frame.getSize().width - 1, frame.getSize().height - 1);
	}

	public static void downloadHead(final String username) {
		new Thread("Download head of " + username) {
			public void run() {
				Console.appendln("Downloading head of " + username);
				Util.downloadSkin(Launcher.SKIN_DOWNLOAD_BASE + username + ".png", new File(Launcher.DIRECTORY, "head.png").getAbsolutePath());
				GUI.reloadSkinImage();
			}
		}.start();
	}

	public static void setMCOutput(String title) {
		if (tabs.getTabCount() == 5) tabs.remove(4);
		MinecraftOutput.clear();
		tabs.add(Lang.getText("mcoutput") + " - " + title, mcopt);
		tabs.setTabComponentAt(4, new ButtonTabComponent(tabs));
		tabs.setSelectedIndex(4);
	}
}