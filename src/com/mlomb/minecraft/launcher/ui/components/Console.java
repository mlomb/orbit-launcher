package com.mlomb.minecraft.launcher.ui.components;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Console {

	private static JTextArea console;
	private static JScrollPane scrPane;

	private Console() {
	}

	public static void appendln(String text) {
		console.append(text + "\n");
		console.setCaretPosition(console.getDocument().getLength());
	}

	public static void append(String text) {
		console.append(text);
	}

	static {
		console = new JTextArea();
		console.setMargin(new Insets(10, 10, 10, 10));
		console.setEditable(false);
		//console.setLineWrap(true);
		console.setFont(new Font("Consolas", Font.BOLD, 14));
		scrPane = new JScrollPane(console);
	}

	public static JScrollPane getComponent() {
		return scrPane;
	}
}