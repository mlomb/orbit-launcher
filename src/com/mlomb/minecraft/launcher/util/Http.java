package com.mlomb.minecraft.launcher.util;

import java.io.*;
import java.net.*;

import com.mlomb.minecraft.launcher.ui.*;
import com.mlomb.minecraft.launcher.ui.components.Console;

public class Http {

	public static String performGet(final URL url, final Proxy proxy) throws IOException {
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
		connection.setRequestMethod("GET");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		final StringBuilder response = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		reader.close();
		return response.toString();
	}

	public static String performPayloadPOST(String surl, String payload) {
		String line;
		StringBuffer jsonString = new StringBuffer();
		try {
			URL url = new URL(surl);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			writer.write(payload);
			writer.close();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
			}
			br.close();
			connection.disconnect();
		} catch (Exception e) {
			return null;
		}
		return jsonString.toString();
	}

	public static boolean downloadFile(String urlCode, String destination) {
		return downloadFile(urlCode, new File(destination), null);
	}

	public static boolean downloadFile(String urlCode, File destination) {
		return downloadFile(urlCode, destination, null);
	}

	public static boolean downloadFile(String urlCode, File destination, String realFilename) {
		BufferedOutputStream bout = null;
		FileOutputStream fos = null;
		BufferedInputStream in = null;

		try {
			if (realFilename == null) GUI.setTask(null, destination.getName());
			else GUI.setTask(null, realFilename);
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
				GUI.progress.setValue((int) Percent);
			}
			GUI.progress.setValue(0);
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

	public static int performPost(URL url, String parameters) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(parameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		/*
		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		System.out.println(response.toString());
		*/

		return responseCode;
	}
}