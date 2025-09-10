package de.luisagrether.idisguise.io;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.luisagrether.idisguise.iDisguise;

public class Language {

    public String CONSOLE_USE_COMMAND = ChatColor.RED + "This command is for players only.";
    public String CURRENTLY_DISGUISED_AS_PLAYER = ChatColor.GOLD + "You are currently disguised as player: %targetSkin%";
    public String CURRENTLY_DISGUISED_AS_ENTITY = ChatColor.GOLD + "You are currently disguised as: %entityType%";
    public String CURRENTLY_NOT_DISGUISED = ChatColor.GOLD + "You are not disguised.";
    public String DISGUISE_TYPE_NOT_SUPPORTED = ChatColor.RED + "Currently not supported!";
    public String DISGUISE_TYPE_NOT_FOUND = ChatColor.RED + "Disguise type not found: %entityType%";
	public String DISGUISE_NO_PERMISSION = ChatColor.RED + "You don't have permission.";
    public String PLAYER_DISGUISE_ACCOUNT_NAME_MISSING = ChatColor.RED + "Please feed me an account name as well.";
    public String PLAYER_DISGUISE_ACCOUNT_NAME_INVALID = ChatColor.RED + "This account name is invalid.";
    public String DISGUISED_SUCCESSFULLY = ChatColor.GOLD + "Disguised successfully!";
    public String DISGUISE_ERROR = ChatColor.RED + "Something went wrong with your disguise.";
    public String DISGUISE_STATEMENT_ERROR = ChatColor.RED + "Something went wrong with your additional statement: %statement%";
    public String DISGUISE_STATEMENT_FORBIDDEN = ChatColor.RED + "This statement is forbidden: %statement%";
    public String UNDISGUISED_SUCCESSFULLY = ChatColor.GOLD + "Undisguised successfully!";
    public String UNDISGUISE_NOT_DISGUISED = ChatColor.RED + "You are not disguised.";

    public String UPDATE_AVAILABLE = ChatColor.GOLD + "[iDisguise] An update is available: %version%";
	public String UPDATE_ALREADY_DOWNLOADED = ChatColor.GOLD + "[iDisguise] Update already downloaded. Restart the server to apply.";
	public String UPDATE_DOWNLOADING = ChatColor.GOLD + "[iDisguise] Downloading update...";
	public String UPDATE_DOWNLOAD_SUCCEEDED = ChatColor.GOLD + "[iDisguise] Download succeeded. Restart the server to apply.";
	public String UPDATE_DOWNLOAD_FAILED = ChatColor.RED + "[iDisguise] Download failed.";
	public String UPDATE_OPTION = ChatColor.GOLD + "[iDisguise] You can enable automatic updates in the config file.";
	
	private iDisguise plugin;
	
	public Language(iDisguise plugin) {
		this.plugin = plugin;
	}
	
	public void loadData() {
		File languageFile = new File(plugin.getDataFolder(), "language.yml");
		FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(languageFile);
		try {
			int fileVersion = UpdateCheck.extractVersionNumber(fileConfiguration.getString("version", "iDisguise 6.0.1"));
			for(Field field : getClass().getDeclaredFields()) {
				if(field.getType().equals(String.class)) {
					if((!field.isAnnotationPresent(LastUpdated.class) || field.getAnnotation(LastUpdated.class).value() <= fileVersion) && fileConfiguration.isString(field.getName().toLowerCase(Locale.ENGLISH).replace('_', '-'))) {
						field.set(this, fileConfiguration.getString(field.getName().toLowerCase(Locale.ENGLISH).replace('_', '-')));
					}
				}
			}
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "An error occured while loading the language file.", e);
		}
	}
	
	public void saveData() {
		File languageFile = new File(plugin.getDataFolder(), "language.yml");
		FileConfiguration fileConfiguration = new YamlConfiguration();
		try {
			for(Field field : getClass().getDeclaredFields()) {
				if(field.getType().equals(String.class)) {
					fileConfiguration.set(field.getName().toLowerCase(Locale.ENGLISH).replace('_', '-'), field.get(this));
				}
			}
			fileConfiguration.set("version", plugin.getNameAndVersion());
			fileConfiguration.save(languageFile);
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "An error occured while saving the language file.", e);
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface LastUpdated {
		int value();
	}
	
}
