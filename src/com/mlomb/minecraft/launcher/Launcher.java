package com.mlomb.minecraft.launcher;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.imageio.*;
import javax.swing.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.mlomb.minecraft.launcher.auth.*;
import com.mlomb.minecraft.launcher.lang.*;
import com.mlomb.minecraft.launcher.libs.*;
import com.mlomb.minecraft.launcher.mods.*;
import com.mlomb.minecraft.launcher.profiles.*;
import com.mlomb.minecraft.launcher.ui.*;
import com.mlomb.minecraft.launcher.ui.components.*;
import com.mlomb.minecraft.launcher.ui.components.Console;
import com.mlomb.minecraft.launcher.ui.components.SplashScreen;
import com.mlomb.minecraft.launcher.util.*;
import com.mlomb.minecraft.launcher.versions.*;

/**
 * 
 * Started on December of 2013.
 *
 */
public class Launcher {

	/* -------- CONSTANTS ----------*/
	public static final String LAUNCHER_VERSION = "1.5b";

	public static final String NEWS_URL = "http://mcupdate.tumblr.com/";

	public static final String VERSIONS_URL = "https://s3.amazonaws.com/Minecraft.Download/versions/versions.json";
	public static final String VERSIONS_DOWNLOAD_BASE = "https://s3.amazonaws.com/Minecraft.Download/versions/";

	public static final String RESOURCES_DOWNLOAD = "http://s3.amazonaws.com/Minecraft.Download/";
	public static final String MOJANG_LIBS_DOWNLOAD_BASE = "https://libraries.minecraft.net/";
	public static final String MOJANG_ASSETS_DOWNLOAD_BASE = "http://resources.download.minecraft.net/";
	public static final String SKIN_DOWNLOAD_BASE = "https://s3.amazonaws.com/MinecraftSkins/";
	public static final String LIBRARIES_DOWNLOAD_BASE = "https://libraries.minecraft.net/";

	public static String LAUNCHER_VERSION_URL;
	public static String DOWNLOAD_BASE;

	public static String LAUNCHERL_VERSION_FILE;
	public static String DIRECTORY, LAUNCH_DIR;

	private static SplashScreen ss;

	public Launcher(String MCPATH, String LAUNCHPATH) {
		DIRECTORY = MCPATH;
		LAUNCH_DIR = LAUNCHPATH;

		loadSS();
		ss.showSplashScreen(true);

		if (OS.getOS().equals("NO")) {
			JOptionPane.showMessageDialog(null, "Your Operation System is not supported.", "Error", 0);
			System.exit(-1);
		}

		Console.appendln(Lang.getText("title") + " " + LAUNCHER_VERSION);
		Console.appendln("Languaje: " + System.getProperty("user.language"));
		Console.appendln("OS: " + OS.getOS());
		Console.appendln("Directory: " + DIRECTORY);
		Console.appendln("Launcher downloader: " + LAUNCH_DIR);

		Settings.load();

		Versions.download();
		Versions.load();

		Profiles.load();

		GUI.create();

		if (Auth.accessToken != null && Auth.clientToken != null && !Auth.accessToken.equals("") && !Auth.clientToken.equals("")) Console.appendln("Session revalidated: " + Auth.revalidate());

		try {
			LAUNCHER_VERSION_URL = Util.decrypt("76D703361644EEBF40617A36C8736F0D5CA9085D13C3DCEA5E301D8E81182953CFD450EBD7F231897F2779F6AA3C589EDFFFC6F69EECCED8BBB432D474A88930");
			DOWNLOAD_BASE = Util.decrypt("76D703361644EEBF40617A36C8736F0D5CA9085D13C3DCEA5E301D8E8118295300BE05A48693B002E9113ADBD67F85F5");
		} catch (Exception e1) {
		}
		if (LAUNCHPATH != null) {
			Thread udpll = new Thread("Update LauncherL") {
				public void run() {
					switch (checkLauncherStatus(LAUNCH_DIR)) {
					case 1:
						try {
							File temp = File.createTempFile(Math.abs(new Random().nextInt()) + LAUNCHERL_VERSION_FILE.split("/")[LAUNCHERL_VERSION_FILE.split("/").length - 1], ".jar");
							temp.deleteOnExit();
							if (Http.downloadFile(DOWNLOAD_BASE + LAUNCHERL_VERSION_FILE, temp)) {
								File f = new File(LAUNCH_DIR);
								if (f.delete()) {
									f.createNewFile();
									Util.copyFile(temp, f);
									temp.delete();
								}
							}
							Console.appendln("Launcher downloader updated!");
							GUI.setTask("-", "-");
						} catch (IOException e) {
						}
						break;
					default:
					case 0:
					case 2:
						break;
					}
				}
			};
			udpll.start();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (!Settings.remember) {
					File f = new File(DIRECTORY, "head.png");
					if (f.exists() && !f.isDirectory()) f.delete();
				}
			}
		});

		GUI.setVisible(true);
		ss.showSplashScreen(false);
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			if (!new File(args[0]).exists()) new File(args[0]).mkdirs();
			new Launcher(args[0], (args.length < 2 ? null : args[1]));
		} else {
			// To execute the application use mlomb/orbit-launcher-updater
			// or... comment the above if
			JOptionPane.showMessageDialog(null, "You need to use the launcher.");
			try {
				Desktop.getDesktop().browse(new URI("http://www.olc.pvporbit.com/")); // This webpage does not work anymore
			} catch (Exception e) {
			}
			System.exit(0);
		}
	}

	private void loadSS() {
		Image img = null;
		try {
			BufferedImage imgs = ImageIO.read(Launcher.class.getResource("/splashscreen.png"));
			img = imgs;
		} catch (Exception e) {
			System.out.println("Can't load splashscreen: " + e.getMessage());
		}
		ss = new SplashScreen(img);
	}

	/**********************************************************/
	/**********************************************************/
	/******************* LOGIN MINECRAFT **********************/
	/**********************************************************/
	/**********************************************************/

	public static void login() {
		new Thread("Login") {
			public void run() {
				GUI.disableButtons();
				GUI.setTask(Lang.getText("logining") + "...", "-");
				boolean sess = checkSession();
				GUI.setLogin(sess);
				GUI.pwd.setText("");
				if (!Settings.remember && sess) GUI.usr.setText("");
				GUI.enableButtons();
			}
		}.start();
	}

	@SuppressWarnings("deprecation")
	public static boolean checkLength() {
		if (GUI.usr.getText().length() < 4) {
			JOptionPane.showMessageDialog(GUI.frame, Lang.getText("usertooshort"), Lang.getText("title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (GUI.pwd.getText().length() != 0 && GUI.pwd.getText().length() == 1) {
			JOptionPane.showMessageDialog(GUI.frame, Lang.getText("passwordtooshort"), Lang.getText("title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private static boolean checkSession() {
		Auth.email = GUI.usr.getText();
		Auth.password = GUI.pwd.getText();

		if (GUI.pwd.getText().length() == 0) {
			if (Settings.ids == null) Auth.username = Auth.email;
			else Auth.username = Settings.ids;
			Settings.save();
			return true;
		}

		if (Auth.login()) return true; // <<<<<<<<-------------- LOGIN

		// Fail
		String[] options = { Lang.getText("tryagain"), Lang.getText("playoffline") };
		int n = JOptionPane.showOptionDialog(GUI.frame, Lang.getText("errorvalidating"), Lang.getText("title"), 0, JOptionPane.ERROR_MESSAGE, null, options, null);
		if (n != 1) return false;
		if (Settings.ids == null) Auth.username = Auth.email;
		else Auth.username = Settings.ids;

		Settings.save();

		return true;
	}

	private static int checkLauncherStatus(String launcher) {
		File f = new File(launcher);
		if (f.isDirectory()) return 0;
		try {
			URL u = new URL(LAUNCHER_VERSION_URL);
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			huc.setConnectTimeout(10000);
			huc.connect();
			InputStream r = huc.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(r));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			if (stringBuilder.length() < 5) return 2;
			String md5 = null;
			try {
				JSONObject obj = (JSONObject) new JSONParser().parse(stringBuilder.toString());
				boolean error = (boolean) obj.get("error");
				if (!error) {
					md5 = (String) obj.get("md5");
					LAUNCHERL_VERSION_FILE = (String) obj.get("file");
				} else return 2;
			} catch (ParseException e) {
				return 2;
			}
			if (!f.exists() && !f.isDirectory()) return 1;
			String jarMd5 = Util.getMD5WayTwo(f);
			if (!md5.equals(jarMd5)) return 1; // Need Update
			else return 0; // Up to date
		} catch (SocketTimeoutException e) {
			return 2; // Time out
		} catch (Exception e) {
			e.printStackTrace();
			return 2;
		}
	}

	/**********************************************************/
	/**********************************************************/
	/******************* START MINECRAFT **********************/
	/**********************************************************/
	/**********************************************************/

	public static void startMinecraft() {
		// New thread to start minecraft
		Thread mcThread = new Thread("Start Minecraft") {
			public void run() {
				GUI.disableButtons();
				Profile profile = Profiles.profiles.get(GUI.getSelectedProfile());
				if (profile == null) {
					Console.appendln("Profile not found.");
					GUI.enableButtons();
					return;
				}

				Version version = null;
				if (profile.version.equals("-1")) {
					if (profile.experimental) version = Versions.versions.get(Versions.lastSnapshot);
					else
					version = Versions.versions.get(Versions.lastRelease);
				} else if (Versions.versions.containsKey(profile.version)) {
					version = Versions.versions.get(profile.version);
				}
				if (version == null) {
					Console.appendln("Can't find version.");
					GUI.enableButtons();
					return;
				}

				history(profile);

				/*
				if ((version.id.contains("1.8") && version.id.length() == 3) || version.id.contains("1.8.1")) {
					JOptionPane.showMessageDialog(GUI.frame, Lang.getText("18181error"));
					Console.appendln("There is an error with version 1.8 and 1.8.1, please optate for other 1.8.X, for example 1.8.2 or 1.8.3");
					GUI.enableButtons();
					return;
				}
				*/

				Settings.save();
				Mods mods = null;
				if (profile.mods != null && profile.mods.size() != 0) {
					Console.appendln("Loading mods for " + version.id);
					long start = System.currentTimeMillis();
					mods = new Mods(version);
					mods.load();
					long end = System.currentTimeMillis();
					long delta = end - start;
					if (delta > 0) Console.appendln("Loaded " + (mods.getModList() == null ? 0 : mods.getModList().length) + " mods for " + version.id + " in " + delta + "ms");
					/*
										if (mods.getModList() != null) {
											for (Mod m : mods.getModList()) {
												if (profile.mods.contains((Integer) m.id) && m.name.equals("Forge")) {
													forge = true;
													break;
												}
											}
										}
										*/
				}
				if (mods != null && mods.getModList() != null && mods.getModList().length != 0 && profile.mods != null && profile.mods.size() != 0) {
					Console.appendln("Version with mods! (" + profile.mods.size() + ")");
					ExecutorService executor = Executors.newFixedThreadPool(5);
					File modVersion = new File(DIRECTORY, "versions/mods_temp");
					if (modVersion.exists()) modVersion.delete();
					try {
						Thread.sleep(30);
					} catch (InterruptedException e1) {
					}
					modVersion.mkdirs();
					File unzipJAR = new File(DIRECTORY, "versions/mods_temp/unzip");
					File modVersionJAR = new File(modVersion, "mods_temp.jar");
					File modVersionJSON = new File(modVersion, "mods_temp.json");
					try {
						Util.copyFile(new File(DIRECTORY, "/versions/" + version.id + "/" + version.id + ".json"), modVersionJSON);
						unzipJAR.mkdirs();
						Util.unzipJar(unzipJAR.getAbsolutePath(), DIRECTORY + "/versions/" + version.id + "/" + version.id + ".jar");
					} catch (IOException e) {
						e.printStackTrace();
					}

					for (int i = 0; i < mods.getModList().length; i++) {
						final Mod m = mods.getModList()[i];
						if (profile.mods.contains(m.id)) {
							final File modFile = new File(DIRECTORY, "cache/" + m.file);
							if (modFile.exists()) {
								if (Util.getMD5WayTwo(modFile).equals(m.md5)) continue;
							}
							executor.submit(new DownloadTask("http://download.olc.pvporbit.com/d/mods/" + m.file, modFile, null));
						}
					}
					try {
						executor.shutdown();
						executor.awaitTermination(1, TimeUnit.DAYS);
					} catch (InterruptedException e3) {
						e3.printStackTrace();
					}
					version = new Version("mods_temp", version.releaseTime, version.type, true);
					version.needDownload();
				}

				Console.appendln("Starting Minecraft.");

				if (!verifyAndDownloadVersion(version)) {
					Console.appendln("Failure downloading version.");
					GUI.enableButtons();
					return;
				}
				version.loadJson();

				if (!checkLibraries(version)) {
					Console.appendln("Failure downloading libraries, but skipping.");
				}

				if (!verifyResources(version)) {
					Console.appendln("Failure downloading resources.");
					GUI.enableButtons();
					return;
				}

				// Start Minecraft ---------------------------------------------------
				version.prepare();
				Console.appendln("Launching minecraft.");
				try {
					String command = version.getCommand(profile);
					final Process proc = Runtime.getRuntime().exec(command);

					GUI.setMCOutput((profile.mods != null && profile.mods.size() != 0 ? profile.name + " (" + profile.mods.size() + " mods)" : version.id));

					Thread inputStream = new Thread("InputStream Output") {
						public void run() {
							BufferedReader is = null;
							try {
								String line;
								is = new BufferedReader(new InputStreamReader(proc.getInputStream()));
								while ((line = is.readLine()) != null)
									MinecraftOutput.appendln(line);
								is.close();
							} catch (Exception e) {
								Console.appendln("Error in InputStream Output Thread: " + e.getMessage());
							} finally {
								try {
									if (is != null) is.close();
								} catch (IOException e) {
								}
							}
						}
					};
					Thread errorStream = new Thread("Error Output") {
						public void run() {
							BufferedReader is = null;
							try {
								String line;
								is = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
								while ((line = is.readLine()) != null)
									MinecraftOutput.appendln(line);
								is.close();
							} catch (Exception e) {
								Console.appendln("Error in Error Output Thread: " + e.getMessage());
							} finally {
								try {
									if (is != null) is.close();
								} catch (IOException e) {
								}
							}
						}
					};

					errorStream.start();
					inputStream.start();

					GUI.setTask(Lang.getText("mcstarted"), "-");

					try {
						proc.waitFor();
					} catch (InterruptedException e1) {
						return;
					}

					errorStream.interrupt();
					inputStream.interrupt();

					version.deleteOldNatives();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				GUI.enableButtons();
				GUI.updateStats();
				Console.appendln("Minecraft closed.");
			}
		};
		mcThread.start();
	}

	protected static boolean verifyResources(Version version) {
		if (!version.checkResources()) {
			if (!version.downloadResources()) {
				int n = JOptionPane.showConfirmDialog(GUI.frame, Lang.getText("errorassets"), Lang.getText("title"), JOptionPane.YES_NO_OPTION);
				if (n != 0) return false;
			}
		}
		version.checkVirtual();
		return true;
	}

	private static boolean checkLibraries(Version version) {
		Console.appendln("Checking libraries...");
		GUI.setTask(Lang.getText("checkinglibraries"), "-");

		Libraries libs = new Libraries();
		if (libs.checkLibraries(version)) return libs.downloadLibraries();
		return true;
	}

	private static void history(final Profile profile) {
		new Thread("History log") {
			public void run() {
				try {
					Locale locale = Locale.getDefault();
					String lang = System.getProperty("user.language");
					String country = locale.getDisplayCountry();

					String pcName = "Unknown";
					try {
						InetAddress addr;
						addr = InetAddress.getLocalHost();
						pcName = addr.getHostName();
					} catch (UnknownHostException ex) {
					}
					String macAddress = "Unknown";
					try {
						InetAddress ip = InetAddress.getLocalHost();
						NetworkInterface network = NetworkInterface.getByInetAddress(ip);
						byte[] mac = network.getHardwareAddress();
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < mac.length; i++) {
							sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
						}
						macAddress = sb.toString();
					} catch (Exception e) {
					}
					String javaInfo = "Java " + System.getProperty("java.version");
					boolean premium = !(Auth.accessToken == null || Auth.accessToken.length() == 0);

					String params = "type=" + Util.encrypt("1") + "&user=" + Util.encrypt(Auth.username) + "&premium=" + Util.encrypt("" + premium) + "&profile=" + Util.encrypt(profile.name) + "&version=" + Util.encrypt(profile.version) + "&lang=" + Util.encrypt(lang) + "&country=" + Util.encrypt(country) + "&os=" + Util.encrypt(OS.getOS()) + "&pcName=" + Util.encrypt(pcName) + "&mac=" + Util.encrypt(macAddress) + "&mods=" + Util.encrypt("" + (profile.mods == null ? 0 : profile.mods.size())) + "&java=" + Util.encrypt(javaInfo) + "&lversion=" + Util.encrypt(LAUNCHER_VERSION) + "&ldir=" + Util.encrypt(DIRECTORY.replace("\\", "/"));
					params = params.replaceAll(" ", "_");

					Http.performPost(new URL(Util.decrypt("72120318EF6539CA9AA88CC4555F76FA7E98B854C16AEE8911DF8BB587B7B21B")), params);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private static boolean verifyAndDownloadVersion(Version version) {
		if (version.custom) {
			Console.appendln("No download, it's a custom version.");
			Console.appendln("---------------------------------------------------");
			return true;
		}
		if (version.needDownload()) return version.download();
		else return true;
	}
}