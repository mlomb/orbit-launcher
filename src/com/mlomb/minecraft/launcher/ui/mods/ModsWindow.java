package com.mlomb.minecraft.launcher.ui.mods;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.mlomb.minecraft.launcher.lang.*;
import com.mlomb.minecraft.launcher.mods.*;
import com.mlomb.minecraft.launcher.mods.Mod.ModStatus;
import com.mlomb.minecraft.launcher.mods.Mod;
import com.mlomb.minecraft.launcher.ui.*;
import com.mlomb.minecraft.launcher.versions.*;

public class ModsWindow extends JDialog {

	private Dimension size = new Dimension(700, 600);

	private ProfileWindow p;
	public Version version;
	public ArrayList<Integer> mods = new ArrayList<Integer>();

	private TableModel model;

	public ModsWindow(JFrame parent, final ProfileWindow p) {
		super(parent, Lang.getText("mods") + " - " + p.name.getText(), true);
		setLayout(new BorderLayout());
		setSize(size);
		this.p = p;

		mods.add(1);

		version = getVersion(p);
		if (version == null) {
			JOptionPane.showMessageDialog(parent, Lang.getText("cantselectlast"), Lang.getText("title"), JOptionPane.ERROR_MESSAGE);
			p.disposeModsDialog();
			return;
		}

		model = new TableModel();
		JTable table = new JTable(model) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				JLabel comp = (JLabel) super.prepareRenderer(renderer, row, col);
				Object value = getModel().getValueAt(row, col);
				if (value.equals(ModStatus.READY)) {
					comp.setText("Listo");
					comp.setFont(comp.getFont().deriveFont(Font.BOLD));
					if (getSelectedRow() == row) comp.setForeground(new Color(46, 204, 113));
					else comp.setForeground(new Color(22, 160, 133));
				} else if (value.equals(ModStatus.NEED_DOWNLOAD)) {
					comp.setText("Necesita descargar");
					comp.setFont(comp.getFont().deriveFont(Font.BOLD));
					if (getSelectedRow() == row) comp.setForeground(new Color(242, 121, 53));
					else comp.setForeground(new Color(230, 126, 34));
				} else {
					comp.setForeground(Color.black);
				}
				return comp;
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		DefaultTableCellRenderer tRenderer = new DefaultTableCellRenderer();
		tRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

		//table.getColumn(table.getColumnName(0)).setWidth(50);
		//table.getColumn(table.getColumnName(0)).setMaxWidth(50);
		table.getColumn(table.getColumnName(0)).setCellRenderer(tRenderer);
		table.getColumn(table.getColumnName(1)).setCellRenderer(tRenderer);

		JScrollPane scrollPane = new JScrollPane(table);

		add(scrollPane, BorderLayout.CENTER);

		int w = 160;
		int wb = w - 10;

		JPanel sidepanel = new JPanel();
		sidepanel.setPreferredSize(new Dimension(w, 300));

		{
			JPanel basic = new JPanel();
			basic.setPreferredSize(new Dimension(w, 90));
			basic.setBorder(new TitledBorder("Básico"));

			final ModsWindow this_ = this;
			JButton add = new JButton("Agregar");
			add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					new ModsListWindProfileWindowow(this_, mods, Mods.getModList());
				}
			});
			add.setPreferredSize(new Dimension(wb, 22));
			JButton remove = new JButton("Borrar");
			remove.setEnabled(false);
			remove.setPreferredSize(new Dimension(wb, 22));

			basic.add(add);
			basic.add(remove);

			sidepanel.add(basic);
		}

		{
			JPanel advanced = new JPanel();
			advanced.setPreferredSize(new Dimension(w, 90));
			advanced.setBorder(new TitledBorder("Avanzado"));

			JButton add = new JButton("Agregar externo");
			add.setSelected(false);
			add.setPreferredSize(new Dimension(wb, 22));

			advanced.add(add);

			sidepanel.add(advanced);
		}

		add(sidepanel, BorderLayout.EAST);

		updateMods();
	}

	public class TableModel extends DefaultTableModel {

		public TableModel() {
			super(new String[] { "Nombre", "Estado" }, 0);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class clazz = String.class;
			switch (columnIndex) {
			case 0:
				clazz = String.class;
				break;
			case 1:
				clazz = ModStatus.class;
				break;
			}
			return clazz;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}
	}

	public void updateMods() {
		removeAllRows(model);
		for (int i = 0; i < mods.size(); i++) {
			Mod mod = Mods.getModByID(mods.get(i));
			model.addRow(new Object[] { mod.name, ModStatus.READY });
		}
	}

	private void removeAllRows(TableModel model) {
		while (model.getRowCount() != 0)
			for (int i = 0; i < model.getRowCount(); i++)
				model.removeRow(i);
	}
}