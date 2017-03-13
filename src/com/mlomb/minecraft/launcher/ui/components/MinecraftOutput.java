package com.mlomb.minecraft.launcher.ui.components;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MinecraftOutput {

	private static JTextArea output;
	private static JScrollPane scrPane;

	private MinecraftOutput() {
	}

	public static void appendln(String text) {
		output.append(text + "\n");
		output.setCaretPosition(output.getDocument().getLength());
	}

	public static void append(String text) {
		output.append(text);
	}

	static {
		output = new JTextArea();
		output.setMargin(new Insets(10, 10, 10, 10));
		output.setEditable(false);
		//console.setLineWrap(true);
		output.setFont(new Font("Consolas", Font.BOLD, 14));
		scrPane = new JScrollPane(output);
	}

	public static JScrollPane getComponent() {
		return scrPane;
	}

	public static void clear() {
		output.setText("");
	}
}