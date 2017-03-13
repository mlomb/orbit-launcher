package com.mlomb.minecraft.launcher.util;

import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.math.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.jar.*;
import java.util.jar.Pack200.Unpacker;
import java.util.zip.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.imageio.*;
import javax.swing.*;

import org.apache.commons.io.*;
import org.tukaani.xz.*;

import com.mlomb.minecraft.launcher.*;
import com.mlomb.minecraft.launcher.ui.*;
import com.mlomb.minecraft.launcher.ui.components.Console;

public class Util {

	@SuppressWarnings("rawtypes")
	public static void unZip(String zipFilePath, String outputFolder) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFilePath);
		} catch (IOException e1) {
			Console.appendln("Can't unZip file: " + zipFilePath);
			return;
		}
		File destination = new File(outputFolder);
		Enumeration files = zipFile.entries();
		File f = null;
		FileOutputStream fos = null;

		while (files.hasMoreElements()) {
			InputStream eis = null;
			try {
				ZipEntry entry = (ZipEntry) files.nextElement();
				eis = zipFile.getInputStream(entry);
				byte[] buffer = new byte[1024];
				int bytesRead = 0;

				f = new File(destination.getAbsolutePath() + File.separator + entry.getName());

				if (entry.isDirectory()) {
					f.mkdirs();
					continue;
				} else {
					f.getParentFile().mkdirs();
					f.createNewFile();
				}

				fos = new FileOutputStream(f);
				long totalDataRead = 0;
				long filesize = entry.getSize();

				while ((bytesRead = eis.read(buffer)) != -1) {
					totalDataRead += bytesRead;
					fos.write(buffer, 0, bytesRead);
					float Percent = (totalDataRead * 100) / filesize;
					GUI.progress.setValue((int) Percent);
				}
				fos.close();
				eis.close();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} finally {
				try {
					if (fos != null) fos.close();
					if (eis != null) eis.close();
				} catch (IOException e) {
				}
			}
		}
		try {
			if (zipFile != null) zipFile.close();
			if (fos != null) fos.close();
		} catch (IOException e) {
		}
		GUI.progress.setValue(0);
	}

	@SuppressWarnings("rawtypes")
	public static void unZipSpecific(String zipFilePath, String outputFile, String fileName) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFilePath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		File destination = new File(outputFile);
		Enumeration files = zipFile.entries();
		File f = null;
		FileOutputStream fos = null;

		while (files.hasMoreElements()) {
			InputStream eis = null;
			try {
				ZipEntry entry = (ZipEntry) files.nextElement();

				if (!entry.getName().equals(fileName)) {
					continue;
				}
				eis = zipFile.getInputStream(entry);
				byte[] buffer = new byte[1024];
				int bytesRead = 0;

				f = new File(destination.getAbsolutePath());

				if (entry.isDirectory()) {
					f.mkdirs();
					continue;
				} else {
					f.getParentFile().mkdirs();
					f.createNewFile();
				}

				fos = new FileOutputStream(f);
				long totalDataRead = 0;
				long filesize = entry.getSize();

				while ((bytesRead = eis.read(buffer)) != -1) {
					totalDataRead += bytesRead;
					fos.write(buffer, 0, bytesRead);
					float Percent = (totalDataRead * 100) / filesize;
					Launcher.getGUI().progress.setValue((int) Percent);
				}
				fos.close();
				eis.close();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} finally {
				try {
					if (fos != null) fos.close();
					if (eis != null) eis.close();
				} catch (IOException e) {
				}
			}
		}
		Launcher.getGUI().progress.setValue(0);
		try {
			if (zipFile != null) zipFile.close();
			if (fos != null) fos.close();
		} catch (IOException e) {
		}
	}

	public static void deleteZipEntry(File zipFile, String[] files) throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();
		tempFile.deleteOnExit();
		boolean renameOk = zipFile.renameTo(tempFile);
		if (!renameOk) { throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath()); }
		byte[] buf = new byte[1024];

		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			boolean toBeDeleted = false;
			for (String f : files) {
				if (f.equals(name)) {
					toBeDeleted = true;
					break;
				}
			}
			if (!toBeDeleted) {
				zout.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = zin.read(buf)) > 0) {
					zout.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		zin.close();
		zout.close();
		tempFile.delete();
	}

	public static int numberOfFilesExcludingSubd(File srcDir) {
		if (!srcDir.exists()) {
			srcDir.mkdirs();
		}
		int count = 0;
		File[] listFiles = srcDir.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].isFile()) {
				count++;
			}
		}
		return count;
	}

	public static boolean deleteDir(File file) {
		try {
			if (file.isDirectory()) {
				if (file.list().length == 0) file.delete();
				else {
					String files[] = file.list();
					for (String temp : files) {
						File fileDelete = new File(file, temp);
						deleteDir(fileDelete);
					}
					if (file.list().length == 0) {
						file.delete();
					}
				}
			} else {
				file.delete();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void downloadSkin(String url, String imgSrc) {
		BufferedImage image = null;
		try {
			URL imageUrl = new URL(url);
			image = ImageIO.read(imageUrl);
			image = image.getSubimage(8, 8, 8, 8);
			if (image != null) {
				File file = new File(imgSrc);
				ImageIO.write(image, "PNG", file);
				Console.appendln("Player skin downloaded!");
			}
		} catch (Exception ex) {
			if (ex.getMessage().equalsIgnoreCase("Can't get input stream from URL!")) {
				Console.appendln("Player has not skin.");
				new File(imgSrc).delete();
			} else {
				Console.appendln("Error donwloading skin image: " + ex.getMessage());
			}
		}
	}

	public static int numberOfFiles(File srcDir) {
		if (!srcDir.exists()) {
			srcDir.mkdirs();
		}
		int count = 0;
		File[] listFiles = srcDir.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].isDirectory()) {
				count += numberOfFiles(listFiles[i]);
			} else if (listFiles[i].isFile()) {
				count++;
			}
		}
		return count;
	}

	public static String getMD5WayTwo(final File file) {
		DigestInputStream stream = null;
		try {
			stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5"));
			final byte[] buffer = new byte[65536];
			for (int read = stream.read(buffer); read >= 1; read = stream.read(buffer)) {
			}
		} catch (Exception ignored) {
			return null;
		} finally {
			try {
				if (stream != null) stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return String.format("%1$032x", new BigInteger(1, stream.getMessageDigest().digest()));
	}

	public static String getMD5Checksum(String filename) throws Exception {
		byte[] b = createChecksum(filename);
		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	private static byte[] createChecksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}

	public static HttpURLConnection verifyMD5Hash(final String localMd5, URL url) throws IOException {
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		connection.setRequestProperty("Expires", "0");
		connection.setRequestProperty("Pragma", "no-cache");
		if (localMd5 != null) connection.setRequestProperty("If-None-Match", localMd5);
		connection.connect();
		return connection;
	}

	public static long getFileSize(File file) {
		return file.length();
	}

	public static void copyFile(File source, File dest) throws IOException {
		if (!dest.getParentFile().exists()) dest.createNewFile();
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (is != null) is.close();
			if (os != null) os.close();
		}
	}

	/// ZIP
	private static final byte[] BUFFER = new byte[4096 * 1024];

	private static void copy(InputStream input, OutputStream output) throws IOException {
		int bytesRead;
		while ((bytesRead = input.read(BUFFER)) != -1) {
			output.write(BUFFER, 0, bytesRead);
		}
	}

	public static void patch(String zip1, String zip2, String outputPath) throws Exception {
		ZipFile originalZip = new ZipFile(zip1);
		ZipFile newZip = new ZipFile(zip2);

		ZipOutputStream moddedZip = new ZipOutputStream(new FileOutputStream(outputPath));

		Enumeration<? extends ZipEntry> entries = originalZip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry e = entries.nextElement();

			String name = e.getName();
			if (newZip.getEntry(name) == null) {
				moddedZip.putNextEntry(e);
				if (!e.isDirectory()) {
					copy(originalZip.getInputStream(e), moddedZip);
				}
				moddedZip.closeEntry();
			}
		}

		Enumeration<? extends ZipEntry> newentries = newZip.entries();
		while (newentries.hasMoreElements()) {
			ZipEntry e = newentries.nextElement();
			moddedZip.putNextEntry(e);
			if (!e.isDirectory()) {
				copy(newZip.getInputStream(e), moddedZip);
			}
			moddedZip.closeEntry();
		}

		originalZip.close();
		newZip.close();
		moddedZip.close();
	}

	@SuppressWarnings("rawtypes")
	public static boolean findFileInZip(String path, String key) throws IOException {
		ZipFile sourceZipFile = new ZipFile(path);
		Enumeration e = sourceZipFile.entries();
		boolean found = false;
		Launcher.getConsole().append("Trying to search " + key + " in " + sourceZipFile.getName());

		while (e.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) e.nextElement();
			if (entry.getName().toLowerCase().indexOf(key) != -1) {
				found = true;
				Launcher.getConsole().append("Found " + entry.getName());
				break;
			}
		}

		try {
			sourceZipFile.close();
		} catch (Exception e1) {
		}
		if (!found) {
			Launcher.getConsole().append("File: " + key + " Not Found Inside Zip File: " + sourceZipFile.getName());
		} else {
			return true;
		}
		return false;
	}

	public static void xz(String pathfile, String destination) throws IOException {
		Launcher.getConsole().append("Unpacking xz...");
		File output = new File(destination);
		InputStream packFile;
		try {
			packFile = new FileInputStream(pathfile);
			byte[] data = IOUtils.toByteArray(packFile);
			if (output.exists()) output.delete();
			byte decompressed[] = readFully(new XZInputStream(new ByteArrayInputStream(data)));
			String end = new String(decompressed, decompressed.length - 4, 4);
			if (!end.equals("SIGN")) {
				Launcher.getConsole().append((new StringBuilder()).append("Unpacking failed, signature missing ").append(end).toString());
				return;
			} else {
				int x = decompressed.length;
				int len = decompressed[x - 8] & 0xff | (decompressed[x - 7] & 0xff) << 8 | (decompressed[x - 6] & 0xff) << 16 | (decompressed[x - 5] & 0xff) << 24;
				byte checksums[] = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);
				FileOutputStream jarBytes = new FileOutputStream(output);
				JarOutputStream jos = new JarOutputStream(jarBytes);
				Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);
				jos.putNextEntry(new JarEntry("checksums.sha1"));
				jos.write(checksums);
				jos.closeEntry();
				jos.close();
				jarBytes.close();
				Launcher.getConsole().append("File xz unpacked.");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Launcher.getConsole().append("Can't unpack zx file: " + e.getMessage());
		}
	}

	public static byte[] readFully(InputStream stream) throws IOException {
		byte data[] = new byte[4096];
		ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
		int len;
		do {
			len = stream.read(data);
			if (len > 0) entryBuffer.write(data, 0, len);
		} while (len != -1);
		return entryBuffer.toByteArray();
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void unpackPack200(String in, String out) {
		try {
			Launcher.getConsole().append("Unpacking: " + new File(in).getName());
			Unpacker unpacker = Pack200.newUnpacker();
			File file = new File(in);
			FileOutputStream fos = new FileOutputStream(out);
			JarOutputStream jostream = new JarOutputStream(fos);
			unpacker.unpack(file, jostream);
			jostream.close();
			Launcher.getConsole().append(new File(in).getName() + " unpacked!");
		} catch (IOException e) {
			Launcher.getConsole().append("Error unpacking: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Not use, please.
	public static void removeJarFiles(File jarFile, String[] files) throws IOException {
		File tempFolder = new File(jarFile.getParentFile(), "temp/");
		tempFolder.mkdirs();
		unzipJar(tempFolder.getAbsolutePath(), jarFile.getAbsolutePath()); // Decompress JAR
		// Delete files
		for (String file : files) {
			File f = new File(tempFolder, file);
			if (f.isDirectory()) {
				Util.deleteDir(f);
			} else {
				if (f.exists()) {
					f.delete();
				}
			}
		}
		jarFile.delete(); // Delete OLD Jar
		// Re-Create JAR
		//	zipFolder(tempFolder.getAbsolutePath(), jarFile.getAbsolutePath());
	}

	public static void unzipJar(String destinationDir, String jarPath) throws IOException {
		File file = new File(jarPath);
		JarFile jar = new JarFile(file);

		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();

			String fileName = destinationDir + File.separator + entry.getName();
			File f = new File(fileName);

			if (fileName.endsWith("/")) {
				f.mkdirs();
			}

		}

		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();

			String fileName = destinationDir + File.separator + entry.getName();
			File f = new File(fileName);
			if (!f.exists()) {
				if (f.isDirectory()) f.mkdirs();
				else f.getParentFile().mkdirs();
			}

			if (!fileName.endsWith("/")) {
				InputStream is = jar.getInputStream(entry);
				FileOutputStream fos = new FileOutputStream(f);

				while (is.available() > 0) {
					fos.write(is.read());
				}

				fos.close();
				is.close();
			}
		}

		jar.close();
	}

	public static String getRedeableTime(long ms) {
		int seconds = (int) ((ms / 1000) % 60);
		int minutes = (int) (((ms / 1000) / 60) % 60);
		int hours = (int) ((((ms / 1000) / 60) / 60) % 24);

		String sec, min, hrs;
		if (seconds < 10) sec = "0" + seconds;
		else sec = "" + seconds;
		if (minutes < 10) min = "0" + minutes;
		else min = "" + minutes;
		if (hours < 10) hrs = "0" + hours;
		else hrs = "" + hours;

		if (hours == 0) return min + ":" + sec;
		else return hrs + ":" + min + ":" + sec;
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	private static final long K = 1024;
	private static final long M = K * K;
	private static final long G = M * K;
	private static final long T = G * K;

	public static String convertToStringRepresentation(final long value) {
		final long[] dividers = new long[] { T, G, M, K, 1 };
		final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
		if (value < 1) throw new IllegalArgumentException("Invalid file size: " + value);
		String result = null;
		for (int i = 0; i < dividers.length; i++) {
			final long divider = dividers[i];
			if (value >= divider) {
				result = format(value, divider, units[i]);
				break;
			}
		}
		return result;
	}

	private static String format(final long value, final long divider, final String unit) {
		final double result = divider > 1 ? (double) value / (double) divider : (double) value;
		return new DecimalFormat("#,##0.#").format(result) + " " + unit;
	}

	public static boolean verifyChecksum(File file, String testChecksum) throws NoSuchAlgorithmException, IOException {
		String fileHash = getMD5Checsum(file);
		return fileHash.equals(testChecksum);
	}

	public static String getMD5Checsum(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		FileInputStream fis = new FileInputStream(file);

		byte[] data = new byte[1024];
		int read = 0;
		while ((read = fis.read(data)) != -1) {
			sha1.update(data, 0, read);
		}
		;
		byte[] hashBytes = sha1.digest();

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < hashBytes.length; i++) {
			sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		String fileHash = sb.toString();

		fis.close();

		return fileHash;
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	private static char[] c = new char[] { 'k', 'M', 'B', 'T' };

	/**
	 * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
	 * @param n the number to format
	 * @param iteration in fact this is the class from the array c
	 * @return a String representing the number n formatted in a cool looking way.
	 */
	public static String coolFormat(double n, int iteration) {
		if (n <= 999) return (int) n + "";
		double d = ((long) n / 100) / 10.0;
		boolean isRound = (d * 10) % 10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
		return (d < 1000 ? //this determines the class, i.e. 'k', 'm' etc
		((d > 99.9 || isRound || (!isRound && d > 9.99) ? //this decides whether to trim the decimals
		(int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
		) + "" + c[iteration])
				: coolFormat(d, iteration + 1));

	}

	private static final KeyStroke escapeStroke =
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	public static final String dispatchWindowClosingActionMapKey =
			"com.spodding.tackline.dispatch:WINDOW_CLOSING";

	public static void installEscapeCloseOperation(final JDialog dialog) {
		Action dispatchClosing = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				dialog.dispatchEvent(new WindowEvent(
						dialog, WindowEvent.WINDOW_CLOSING
						));
			}
		};
		JRootPane root = dialog.getRootPane();
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				escapeStroke, dispatchWindowClosingActionMapKey
				);
		root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing
				);
	}
}