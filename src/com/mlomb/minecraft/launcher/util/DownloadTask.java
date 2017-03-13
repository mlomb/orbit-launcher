package com.mlomb.minecraft.launcher.util;

import java.io.*;
import java.net.*;

import com.mlomb.minecraft.launcher.ui.*;
import com.mlomb.minecraft.launcher.ui.components.Console;

public class DownloadTask implements Runnable {

	private String url;
	private File dest;

	private DownloadTaskCallback cb;

	public DownloadTask(String url, String filepath) {
		this.url = url;
		this.dest = new File(filepath);
	}

	public DownloadTask(String url, File filepath) {
		this.url = url;
		this.dest = filepath;
	}

	public DownloadTask(String url, String filepath, DownloadTaskCallback cb) {
		this.url = url;
		this.dest = new File(filepath);
		this.cb = cb;
	}

	public DownloadTask(String url, File filepath, DownloadTaskCallback cb) {
		this.url = url;
		this.dest = filepath;
		this.cb = cb;
	}

	public void run() {
		downloadFile(url, dest, false);
		if (cb != null) cb.onComplete();
	}

	public interface DownloadTaskCallback {
		public void onComplete();
	}

	public static boolean downloadFile(String urlCode, String destination, boolean showInfo) {
		return downloadFile(urlCode, new File(destination), null, showInfo);
	}

	public static boolean downloadFile(String urlCode, File destination, boolean showInfo) {
		return downloadFile(urlCode, destination, null, showInfo);
	}

	public static boolean downloadFile(String urlCode, File destination, String realFilename, boolean showInfo) {
		BufferedOutputStream bout = null;
		FileOutputStream fos = null;
		BufferedInputStream in = null;

		try {
			if (showInfo) {
				if (realFilename == null) GUI.setTask(null, destination.getName());
				else GUI.setTask(null, realFilename);
			}
			URL url = new URL(urlCode);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			int filesize = connection.getContentLength();
			float totalDataRead = 0;
			in = new BufferedInputStream(connection.getInputStream());
			fos = new FileOutputStream(destination);
			bout = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[1024];
			int i = 0;
			while ((i = in.read(data, 0, 1024)) >= 0) {
				totalDataRead = totalDataRead + i;
				bout.write(data, 0, i);
				float Percent = (totalDataRead * 100) / filesize;
				if (showInfo) GUI.progress.setValue((int) Percent);
			}
			if (showInfo) GUI.progress.setValue(0);
			if (bout != null) bout.close();
			if (in != null) in.close();
			if (fos != null) fos.close();
			return true;
		} catch (IOException e) {
			try {
				if (bout != null) bout.close();
				if (in != null) in.close();
				if (fos != null) fos.close();
			} catch (Exception ex) {
			}
			String msg = " - - - Unknow";
			if (e.getMessage().contains("403")) {
				msg = " - - - java.io.IOException: Server returned HTTP response code: 403";
			} else if (e.getMessage().contains(urlCode)) {
				msg = " - - - java.io.FileNotFoundException: File not found in the server.";
			}
			Console.appendln("Error downloading " + destination.getName() + msg);
			return false;
		} finally {
			try {
				if (bout != null) bout.close();
				if (in != null) in.close();
				if (fos != null) fos.close();
			} catch (Exception ex) {
			}
		}
	}
}
