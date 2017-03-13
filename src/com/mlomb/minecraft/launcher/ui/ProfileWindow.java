package com.mlomb.minecraft.launcher.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.management.*;
import java.text.*;
import java.util.*;

import javax.imageio.*;
import javax.management.*;
import javax.swing.*;
import javax.swing.event.*;

import com.mlomb.minecraft.launcher.*;
import com.mlomb.minecraft.launcher.lang.*;
import com.mlomb.minecraft.launcher.profiles.*;
import com.mlomb.minecraft.launcher.ui.components.Console;
import com.mlomb.minecraft.launcher.ui.mods.*;
import com.mlomb.minecraft.launcher.versions.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProfileWindow extends JPanel {
	private static final long serialVersionUID = 1L;

	private Profile profile;
	private JFrame parent;

	// Components
	public JTextField name;

	public JComboBox verList;
	private JCheckBox cbExperimental;

	private JComboBox maxR;
	private JCheckBox cbRam;

	private JSpinner x;
	private JSpinner y;

	private JCheckBox cbRes;

	private JButton btn1;
	private JButton btn2;

	private JLabel info1;
	private JLabel info2;
	private JButton btninfo2;
	private JButton btninfo3;

	public boolean showExperimentalVersions;
	private boolean sb;

	public Version version;
	public ArrayList<Integer> mods = new ArrayList<Integer>();

	public ProfileWindow(Profile p, JFrame pr) {
		final ProfileWindow this_ = this;
		this.parent = pr;
		this.profile = p;

		initWindow();

		JPanel basicPanel = new JPanel(new GridLayout(0, 1));
		basicPanel.setBorder(BorderFactory.createTitledBorder(Lang.getText("basic")));
		this.add(basicPanel);

		JPanel panel1 = new JPanel();
		JLabel lbl1 = new JLabel(Lang.getText("profilename") + ":");
		panel1.add(lbl1);

		name = new JTextField();
		name.setPreferredSize(new Dimension(210, 20));
		panel1.add(name);

		basicPanel.add(panel1);

		JPanel cb = new JPanel();
		cbExperimental = new JCheckBox();
		if (profile != null) cbExperimental.setSelected(profile.experimental);
		cbExperimental.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showExperimentalVersions = !(e.getStateChange() == 2);
				showExperimentalVersions(getSelectedVersion());
			}
		});
		cbExperimental.setText(Lang.getText("activeexperimentals"));
		cb.add(cbExperimental);
		basicPanel.add(cb);

		JPanel panel2 = new JPanel();

		JLabel lbl2 = new JLabel(Lang.getText("version") + ":");
		panel2.add(lbl2);

		verList = new JComboBox();
		verList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (sb || e.getStateChange() != ItemEvent.SELECTED) return;
				Version newV = getVersion();
				if (mods != null && mods.size() != 0) {
					int c = JOptionPane.showConfirmDialog(this_, "Si cambias de versión los mods que tengas activos se quitaran.\nDeseas cambiar de versión y quitar los mods?", "Advertencia", JOptionPane.YES_NO_OPTION);
					if (c == JOptionPane.YES_OPTION) mods.clear();
					else {
						int index = 1;
						for (Version v : Versions.versions.values()) {
							if (!v.type.equals("release") && !showExperimentalVersions) continue;
							if (version.id.equals(v.id)) {
								System.out.println("I:" + index);
								sb = true;
								verList.setSelectedIndex(index);
								sb = false;
							}
							index++;
						}
						return;
					}
				}
				version = newV;
			}
		});
		verList.setPreferredSize(new Dimension(220, 20));
		panel2.add(verList);
		try {
			ImageIcon img = new ImageIcon(ImageIO.read(Launcher.class.getResource("/icons/no.png")));
			btninfo2 = new JButton();
			btninfo2.setIcon(img);
			btninfo2.setBorder(BorderFactory.createEmptyBorder());
			btninfo2.setContentAreaFilled(false);
			btninfo2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(parent, Lang.getText("cantchangeforge"));
				}
			});
			panel2.add(btninfo2);

			ImageIcon img2 = new ImageIcon(ImageIO.read(Launcher.class.getResource("/icons/question.png")));
			btninfo3 = new JButton();
			btninfo3.setIcon(img2);
			btninfo3.setBorder(BorderFactory.createEmptyBorder());
			btninfo3.setContentAreaFilled(false);
			btninfo3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(parent, Lang.getText("verexp"));
				}
			});
			panel2.add(btninfo3);
		} catch (IOException e1) {
			Console.appendln("Can't load icon: " + e1.getMessage());
		}

		basicPanel.add(panel2);

		JPanel panelClose = new JPanel(new BorderLayout());

		btn1 = new JButton(Lang.getText("cancel"));
		btn2 = new JButton(Lang.getText("saveprofile"));
		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btn1.setEnabled(false);
				btn2.setEnabled(false);
				GUI.destroyProfileDialog();
			}
		});
		btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		x = new JSpinner();
		y = new JSpinner();
		x.setPreferredSize(new Dimension(75, 20));
		y.setPreferredSize(new Dimension(75, 20));
		JPanel panel4 = new JPanel(new GridLayout(0, 1));
		panel4.setBorder(BorderFactory.createTitledBorder(Lang.getText("advanced")));
		JPanel panel3 = new JPanel();
		cbRes = new JCheckBox(Lang.getText("changeresolution") + ":");
		cbRes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				if (cbRes.isSelected()) {
					x.setEnabled(true);
					y.setEnabled(true);
				} else {
					x.setEnabled(false);
					y.setEnabled(false);
				}
			}
		});
		panel3.add(cbRes);
		panel3.add(x);
		panel3.add(y);

		JPanel panel5 = new JPanel();
		cbRam = new JCheckBox(Lang.getText("dedicatedram") + ":");

		long maxRam = -1;
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			maxRam = (long) mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "TotalPhysicalMemorySize");
		} catch (AttributeNotFoundException | InstanceNotFoundException | MalformedObjectNameException | MBeanException | ReflectionException e2) {
		}
		maxRam /= 209715200; // 209715200 = 200MB

		int index = 3;
		int b = 0;
		maxR = new JComboBox();
		for (int i = 1; i < maxRam; i += 1) {
			if (i > maxRam) break;
			if (p != null) {
				if (i * 200 == p.RAM) {
					index = b;
				}
			}
			b++;
			String f;
			if (i < 5) f = (i * 200) + " MB";
			else {
				DecimalFormat decimalFormat = new DecimalFormat("#.#");
				double gb = (double) (i * 200) / 1000;
				f = decimalFormat.format(gb) + " GB";
			}
			maxR.addItem(f);
		}
		maxR.setSelectedIndex(index);
		if (p != null) {
			name.setText(p.name);
			showExperimentalVersions = p.experimental;
			showExperimentalVersions(p.version);

			if (profile.resolutionX <= 0 || profile.resolutionY <= 0) {
				cbRes.setSelected(false);
				x.setValue(1024);
				x.setEnabled(false);
				y.setValue(768);
				y.setEnabled(false);
			} else {
				cbRes.setSelected(true);
				x.setValue(profile.resolutionX);
				y.setValue(profile.resolutionY);
			}
			if (profile.RAM <= 0) {
				cbRam.setSelected(false);
				maxR.setEnabled(false);
			} else {
				cbRam.setSelected(true);
				maxR.setEnabled(true);
			}
		} else {
			name.setText(Lang.getText("newprofile"));
			showExperimentalVersions = false;
			showExperimentalVersions(null);

			x.setValue(1024);
			x.setEnabled(false);

			y.setValue(768);
			y.setEnabled(false);

			maxR.setEnabled(false);
		}

		cbRam.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				if (cbRam.isSelected()) {
					maxR.setEnabled(true);
				} else {
					maxR.setEnabled(false);
				}
			}
		});

		panel5.add(cbRam);
		panel5.add(maxR);

		JLabel lblInfo = new JLabel("(" + Lang.getText("youhaveram").replace("%ram%", ((maxRam * 200) / 1000) + " GB") + ")");
		panel5.add(lblInfo);

		try {
			ImageIcon img = new ImageIcon(ImageIO.read(Launcher.class.getResource("/icons/question.png")));
			JButton btninfo1 = new JButton();
			btninfo1.setIcon(img);
			btninfo1.setBorder(BorderFactory.createEmptyBorder());
			btninfo1.setContentAreaFilled(false);
			btninfo1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(parent, Lang.getText("rammessage"));
				}
			});
			panel5.add(btninfo1);
		} catch (IOException e1) {
			Console.appendln("Can't load question icon: " + e1.getMessage());
		}

		panel4.add(panel3);
		panel4.add(panel5);
		panel4.setPreferredSize(new Dimension(325, 90));
		this.add(panel4);

		JPanel modsPanel = new JPanel(new GridLayout(0, 1));
		modsPanel.setEnabled(true); // TODO Enable.
		modsPanel.setPreferredSize(new Dimension(325, 75));
		modsPanel.setBorder(BorderFactory.createTitledBorder(Lang.getText("mods")));

		info1 = new JLabel();
		info2 = new JLabel();
		modsPanel.add(info1);
		//modsPanel.add(info2);
		JButton addMods = new JButton(Lang.getText("amrmods"));
		addMods.setEnabled(true); // TODO Enable.
		addMods.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showModsDialog();
			}
		});
		addMods.setEnabled(true);
		modsPanel.add(addMods);

		this.add(modsPanel);

		panelClose.add(btn1, BorderLayout.WEST);
		panelClose.add(btn2, BorderLayout.EAST);
		this.add(panelClose);

		version = getVersion();
		if (p != null) mods = p.mods;
		updateMods();
	}

	public void disable() {
		btn2.setEnabled(false);
	}

	public void enable() {
		btn2.setEnabled(true);
	}

	protected void save() {
		if (name.getText().length() < 1) {
			JOptionPane.showMessageDialog(this, Lang.getText("pnshort"), Lang.getText("title"), JOptionPane.INFORMATION_MESSAGE);
			enable();
			return;
		}
		if (name.getText().equals(Lang.getText("default"))) {
			JOptionPane.showMessageDialog(this, Lang.getText("cantdefault"), Lang.getText("title"), JOptionPane.INFORMATION_MESSAGE);
			enable();
			return;
		}
		for (Profile aux : Profiles.profiles.values()) {
			if (profile != null) break;
			if (aux.name.equals(name.getText())) {
				JOptionPane.showMessageDialog(this, Lang.getText("profilenameinuse"), Lang.getText("title"), JOptionPane.INFORMATION_MESSAGE);
				enable();
				return;
			}
		}
		int ram = getSelectedRam();
		String ver = getSelectedVersion();
		// -
		boolean np = false;
		if (profile == null) {
			np = true;
			profile = new Profile();
			Profiles.profiles.put(name.getText(), profile);
		}
		if (cbRes.isSelected()) {
			profile.resolutionX = (int) x.getValue();
			profile.resolutionY = (int) y.getValue();
		} else {
			profile.resolutionX = 0;
			profile.resolutionY = 0;
		}
		if (cbRam.isSelected()) profile.RAM = ram;
		else profile.RAM = 0;

		profile.experimental = cbExperimental.isSelected();
		profile.version = ver;
		if (!np && !name.getText().equals(profile.name)) {
			String oldName = new String(profile.name);
			profile.name = name.getText();
			Profiles.profiles.put(name.getText(), Profiles.profiles.remove(oldName));
		} else {
			profile.name = name.getText();
		}
		profile.mods = mods;
		// Save
		Profiles.save();
		GUI.updateProfiles();
		GUI.setSelectedProfile(name.getText());
		GUI.destroyProfileDialog();
	}

	private int getSelectedRam() {
		return ((maxR.getSelectedIndex() + 1) * 200);
	}

	protected String getSelectedVersion() {
		String version = null;
		String n = (String) verList.getSelectedItem();
		if (n.equals(Lang.getText("lastupdate"))) version = "-1";
		else if (n.contains("Snapshot: ")) version = n.split("Snapshot: ")[1];
		else if (n.contains("Alpha: ")) version = n.split("Alpha: ")[1];
		else if (n.contains("Beta: ")) version = n.split("Beta: ")[1];
		else if (n.contains("Release: ")) version = n.split("Release: ")[1];
		return version;
	}

	private void initWindow() {
		setPreferredSize(new Dimension(350, 345));
		setBorder(BorderFactory.createTitledBorder(Lang.getText("profiles")));
		setLayout(new FlowLayout());
	}

	public void showExperimentalVersions(String selected) {
		verList.removeAllItems();
		int selectedIndex = -1;

		verList.addItem(Lang.getText("lastupdate"));

		for (Version v : Versions.versions.values()) {
			if (v.type.equals("release")) {
				verList.addItem("Release: " + v.id);
			} else if (showExperimentalVersions && v.type.equals("old_beta")) {
				verList.addItem("Beta: " + v.id);
			} else if (showExperimentalVersions && v.type.equals("old_alpha")) {
				verList.addItem("Alpha: " + v.id);
			} else if (showExperimentalVersions && v.type.equals("snapshot")) {
				verList.addItem("Snapshot: " + v.id);
			}

			if (selected != null && selected.equals(v.id)) selectedIndex = verList.getItemCount() - 1;
		}
		if (selectedIndex != -1) verList.setSelectedIndex(selectedIndex);
	}

	public void showModsDialog() {
		if (version == null) {
			JOptionPane.showMessageDialog(parent, Lang.getText("cantselectlast"), Lang.getText("title"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		new ModsListWindow(GUI.dialog, this, mods);
	}

	private Version getVersion() {
		if (verList.getSelectedIndex() == 0) return null;

		int index = 1;
		for (Version v : Versions.versions.values()) {
			if (!v.type.equals("release") && !showExperimentalVersions) continue;
			if (verList.getSelectedIndex() == index) return v;
			index++;
		}
		return null;
	}

	public void updateMods() {
		int cant = mods == null ? 0 : mods.size();

		if (cant == 1) {
			info1.setText(Lang.getText("onemod"));
		} else if (cant == 0) {
			info1.setText(Lang.getText("nomodsinstalled"));
		} else {
			info1.setText(Lang.getText("multiplemods").replace("%mods%", cant + ""));
		}
		if (cant > 0) {
			cbExperimental.setEnabled(false);
			verList.setEnabled(false);

			btninfo2.setVisible(true);
			btninfo3.setVisible(false);
		} else {
			cbExperimental.setEnabled(true);
			verList.setEnabled(true);

			btninfo2.setVisible(false);
			btninfo3.setVisible(true);
		}
	}
}