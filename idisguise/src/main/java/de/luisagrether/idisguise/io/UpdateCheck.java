package de.luisagrether.idisguise.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.luisagrether.idisguise.iDisguise;
import de.luisagrether.util.ObjectUtil;
import de.luisagrether.util.StringUtil;

public class UpdateCheck implements Runnable {
	
	public static final int PROJECT_ID = 46941;
	public static final String API_URL = "https://api.curseforge.com/servermods/files?projectIds=";
	public static final String API_CHECKSUM = "md5";
	public static final String API_DOWNLOAD_URL = "downloadUrl";
	public static final String API_FILE_NAME = "fileName";
	public static final String API_GAME_VERSION = "gameVersion";
	public static final String API_NAME = "name";
	public static final String API_RELEASE_TYPE = "releaseType";
	
	private iDisguise plugin;
	private String pluginVersion;
	private String latestVersion;
	private CommandSender toBeNotified;
	private String downloadUrl;
	private String checksum;
	private boolean autoDownload;
	
	public UpdateCheck(iDisguise plugin, CommandSender toBeNotified, boolean autoDownload) {
		this.plugin = plugin;
		this.pluginVersion = plugin.getNameAndVersion();
		this.toBeNotified = toBeNotified;
		this.autoDownload = autoDownload;
	}
	
	public void run() {
		checkForUpdate();
		if(isUpdateAvailable()) {
			toBeNotified.sendMessage(plugin.getLanguage().UPDATE_AVAILABLE.replace("%version%", latestVersion));
			if(autoDownload) {
				downloadUpdate();
			} else {
				toBeNotified.sendMessage(plugin.getLanguage().UPDATE_OPTION);
			}
		}
	}
	
	private boolean isUpdateAvailable() {
		if(latestVersion != null && !pluginVersion.equals(latestVersion)) {
			try {
				int current = extractVersionNumber(pluginVersion);
				int latest = extractVersionNumber(latestVersion);
				return latest > current;
			} catch(NumberFormatException e) {
			} catch(ArrayIndexOutOfBoundsException e) {
			}
		}
		return false;
	}
	
	private void checkForUpdate() {
		BufferedReader reader = null;
		try {
			URL url = new URL(API_URL + PROJECT_ID);
			URLConnection connection = url.openConnection();
			connection.addRequestProperty("User-Agent", pluginVersion.replace(' ', '/') + " (by LuisaGrether)");
			connection.setDoOutput(true);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String response = reader.readLine();
			JSONArray array = (JSONArray)JSONValue.parse(response);
			latestVersion = null;
			int latestVersionNumber = 0;
			for(Object obj : array) {
				JSONObject object = (JSONObject)obj;
				int versionNumber = extractVersionNumber((String)object.get(API_NAME));
				if(versionNumber > latestVersionNumber) {
					latestVersionNumber = versionNumber;
					latestVersion = (String)object.get(API_NAME);
					downloadUrl = ((String)object.get(API_DOWNLOAD_URL));
					checksum = (String)object.get(API_CHECKSUM);
				}
			}
		} catch(Exception e) {
			plugin.getLogger().log(Level.WARNING, "Update checking failed: " + e.getClass().getSimpleName());
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
				}
			}
		}
	}
	
	private void downloadUpdate() {
		File oldFile = plugin.getFile();
		File newFile = new File(plugin.getServer().getUpdateFolderFile(), oldFile.getName());
		if(newFile.exists()) {
			toBeNotified.sendMessage(plugin.getLanguage().UPDATE_ALREADY_DOWNLOADED);
		} else {
			InputStream input = null;
			OutputStream output = null;
			try {
				URL url = new URL(downloadUrl);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.addRequestProperty("User-Agent", pluginVersion.replace(' ', '/') + " (by LuisaGrether)");
				connection.setDoOutput(true);
				if(ObjectUtil.equals(connection.getResponseCode(), 301, 302)) {
					downloadUrl = connection.getHeaderField("Location");
					downloadUpdate();
					return;
				} else if(connection.getResponseCode() != 200) {
					toBeNotified.sendMessage(plugin.getLanguage().UPDATE_DOWNLOAD_FAILED);
					plugin.getLogger().log(Level.WARNING, "Update download failed: HTTP error");
					return;
				}
				toBeNotified.sendMessage(plugin.getLanguage().UPDATE_DOWNLOADING);
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				input = new DigestInputStream(connection.getInputStream(), messageDigest);
				plugin.getServer().getUpdateFolderFile().mkdir();
				output = new FileOutputStream(newFile);
				int fetched;
				byte[] data = new byte[4096];
				while((fetched = input.read(data)) > 0) {
					output.write(data, 0, fetched);
				}
				input.close();
				output.close();
				if(StringUtil.bytesToHex(messageDigest.digest()).equalsIgnoreCase(this.checksum)) {
					toBeNotified.sendMessage(plugin.getLanguage().UPDATE_DOWNLOAD_SUCCEEDED);
				} else {
					newFile.delete();
					toBeNotified.sendMessage(plugin.getLanguage().UPDATE_DOWNLOAD_FAILED);
					plugin.getLogger().log(Level.WARNING, "Update download failed: checksum is bad");
				}
			} catch(IOException | NoSuchAlgorithmException e) {
				toBeNotified.sendMessage(plugin.getLanguage().UPDATE_DOWNLOAD_FAILED);
				plugin.getLogger().log(Level.WARNING, "Update download failed: " + e.getClass().getSimpleName());
			} finally {
				if(input != null) {
					try {
						input.close();
					} catch(IOException e) {
					}
				}
				if(output != null) {
					try {
						output.close();
					} catch(IOException e) {
					}
				}
			}
		}
	}
	
	public static int extractVersionNumber(String versionString) {
		try {
			String[] numbers = versionString.split(" |-")[1].split("\\.");
			int versionNumber = 0;
			for(int i = 0; i < numbers.length; i++) {
				if(numbers[i].length() > 2)
					return 0;
				
				versionNumber += Integer.parseInt(numbers[i]) * Math.pow(10.0, 2 * (numbers.length - i - 1));
			}
			if(versionString.contains("SNAPSHOT"))
				versionNumber--;
			return versionNumber;
		} catch(ArrayIndexOutOfBoundsException|NumberFormatException e) {
			return 0;
		}
	}
	
}
