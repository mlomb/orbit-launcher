package com.mlomb.minecraft.launcher.ui.components;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.*;

import com.mlomb.minecraft.launcher.lang.*;

public class WebDisplay {
	private final JTextPane web;
	private final JScrollPane scrollPane;

	public WebDisplay(HyperlinkListener list) {
		web = new JTextPane();
		scrollPane = new JScrollPane();

		web.setBorder(null);
		scrollPane.setBorder(null);
		scrollPane.setViewportView(this.web);

		web.setEditable(false);
		web.setContentType("text/html");
		web.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>" + Lang.getText("loadingweb") + "...</h1></center></font></body></html>");
		web.setBackground(Color.white);
		if (list == null) {
			web.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(final HyperlinkEvent he) {
					if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						try {
							openLink(he.getURL().toURI());
						} catch (Exception e) {
							Console.appendln("Can't open link: " + he.getURL() + " : " + e.getMessage());
						}
					}
				}
			});
		} else web.addHyperlinkListener(list);
	}

	public String getWebString(String urlCode) {
		try {
			URL url;
			url = new URL(urlCode);
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
			Matcher m = p.matcher(con.getContentType());
			String charset = m.matches() ? m.group(1) : "ISO-8859-1";
			Reader r = new InputStreamReader(con.getInputStream(), charset);
			StringBuilder buf = new StringBuilder();
			while (true) {
				int ch = r.read();
				if (ch < 0) break;
				buf.append((char) ch);
			}
			String html = buf.toString();
			return html;
		} catch (IOException e) {
			Console.appendln("Error loading webpage ( " + urlCode + " ): " + e);
			e.printStackTrace();
			return "<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>" + Lang.getText("errorweb") + "</h1></center></font></body></html>";
		}

	}

	public static void openLink(final URI link) {
		Console.appendln("Opening link: " + link);
		try {
			final Class<?> desktopClass = Class.forName("java.awt.Desktop");
			final Object o = desktopClass.getMethod("getDesktop", (Class<?>[]) new Class[0]).invoke(null, new Object[0]);
			desktopClass.getMethod("browse", URI.class).invoke(o, link);
		} catch (Throwable e) {
			Console.appendln("Error open link ( " + link + " ): " + e);
		}
	}

	public void setPage(final String url) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					web.setPage(new URL(url));
				} catch (IOException e) {
					Console.appendln("Error loading webpage ( " + url + " ): " + e);
					web.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>" + Lang.getText("errorweb") + "</h1></center></font></body></html>");
				}
			}
		});
	}

	public JScrollPane getComponent() {
		return scrollPane;
	}

	public void setText(String html) {
		web.setText(html);
	}
}