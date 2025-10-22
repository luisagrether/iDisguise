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

    @LastUpdated(60001)
	public String CONSOLE_USE_COMMAND = ChatColor.RED + "This command is for players only.";
    @LastUpdated(60001)
	public String CURRENTLY_DISGUISED_AS_PLAYER = ChatColor.GOLD + "You are currently disguised as player: %targetSkin%";
	@LastUpdated(60001)
	public String ODISGUISE_CURRENTLY_DISGUISED_AS_PLAYER = ChatColor.GOLD + "%player% is currently disguised as player: %targetSkin%";
    @LastUpdated(60001)
	public String CURRENTLY_DISGUISED_AS_ENTITY = ChatColor.GOLD + "You are currently disguised as: %entityType%";
	@LastUpdated(60001)
	public String ODISGUISE_CURRENTLY_DISGUISED_AS_ENTITY = ChatColor.GOLD + "%player% is currently disguised as: %entityType%";
    @LastUpdated(60001)
	public String CURRENTLY_NOT_DISGUISED = ChatColor.GOLD + "You are not disguised.";
	@LastUpdated(60001)
	public String ODISGUISE_CURRENTLY_NOT_DISGUISED = ChatColor.GOLD + "%player% is not disguised.";
    @LastUpdated(60001)
	public String DISGUISE_TYPE_NOT_SUPPORTED = ChatColor.RED + "Currently not supported!";
    @LastUpdated(60001)
	public String DISGUISE_TYPE_NOT_FOUND = ChatColor.RED + "Disguise type not found: %entityType%";
	@LastUpdated(60001)
	public String DISGUISE_NO_PERMISSION = ChatColor.RED + "You don't have permission.";
	@LastUpdated(60001)
	public String DISGUISE_EVENT_CANCELLED = ChatColor.RED + "Another plugin prevents you from disguising.";
	@LastUpdated(60001)
	public String ODISGUISE_EVENT_CANCELLED = ChatColor.RED + "Another plugin prevents %player% from disguising.";
    @LastUpdated(60001)
	public String PLAYER_DISGUISE_ACCOUNT_NAME_MISSING = ChatColor.RED + "Please feed me an account name as well.";
    @LastUpdated(60001)
	public String PLAYER_DISGUISE_ACCOUNT_NAME_INVALID = ChatColor.RED + "This account name is invalid.";
	@LastUpdated(60002)
	public String PLAYER_DISGUISE_ACCOUNT_NAME_BLACKLISTED = ChatColor.RED + "This account name is blacklisted.";
    @LastUpdated(60001)
	public String DISGUISED_SUCCESSFULLY = ChatColor.GOLD + "Disguised successfully!";
    @LastUpdated(60001)
	public String DISGUISE_ERROR = ChatColor.RED + "Something went wrong with the disguise.";
    @LastUpdated(60001)
	public String DISGUISE_STATEMENT_ERROR = ChatColor.RED + "Something went wrong with the additional statement: %statement%";
    @LastUpdated(60001)
	public String DISGUISE_STATEMENT_FORBIDDEN = ChatColor.RED + "This statement is forbidden: %statement%";
	@LastUpdated(60001)
	public String DISGUISE_STATEMENT_UNKNOWN = ChatColor.RED + "This statement is unknown or uses invalid syntax: %statement%";
    @LastUpdated(60001)
	public String UNDISGUISED_SUCCESSFULLY = ChatColor.GOLD + "Undisguised successfully!";
    @LastUpdated(60001)
	public String UNDISGUISE_NOT_DISGUISED = ChatColor.RED + "You are not disguised.";
	@LastUpdated(60001)
	public String UNDISGUISE_OTHER_NOT_DISGUISED = ChatColor.RED + "%player% is not disguised.";
	@LastUpdated(60001)
	public String UNDISGUISE_EVENT_CANCELLED = ChatColor.RED + "Another plugin prevents you from undisguising.";
	@LastUpdated(60001)
	public String UNDISGUISE_OTHER_EVENT_CANCELLED = ChatColor.RED + "Another plugin prevents %player% from undisguising.";
	@LastUpdated(60001)
	public String ODISGUISE_NO_PLAYERNAME_GIVEN = ChatColor.RED + "Please feed me the name of an online player.";
	@LastUpdated(60001)
	public String ODISGUISE_PLAYER_NOT_FOUND = ChatColor.RED + "Can't find player: %player%";
	@LastUpdated(60001)
	public String ODISGUISE_NOTIFICATION = ChatColor.GOLD + "You were disguised by %sender%.";
	@LastUpdated(60001)
	public String UNDISGUISE_NOTIFICATION = ChatColor.GOLD + "You were undisguised by %sender%.";

	@LastUpdated(60001)
	public String HELP_PAGE_TITLE = ChatColor.DARK_GREEN + "===== " + ChatColor.BOLD + "iDisguise" + ChatColor.RESET + ChatColor.DARK_GREEN + " Help Page %number% ====="; 
	@LastUpdated(60001)
	public String HELP_DISGUISE_MOB = ChatColor.GOLD.toString() + ChatColor.BOLD + "Disguise as a mob: " + ChatColor.ITALIC + "/disguise <entity-type>\\\\"
							        + ChatColor.GOLD + "Example: " + ChatColor.ITALIC + "/disguise zombie\\\\"
							        + ChatColor.GOLD + "Use [TAB] to get a list of all entity types.\\\\"
							        + ChatColor.GOLD + "After disguising, you will be able to see your own disguise\\\\"
									+ ChatColor.GOLD + "for five seconds.";
	@LastUpdated(60001)
	public String HELP_DISGUISE_PLAYER = ChatColor.GOLD.toString() + ChatColor.BOLD + "Disguise as a player: " + ChatColor.ITALIC + "/disguise player <name>\\\\"
							           + ChatColor.GOLD + "Example: " + ChatColor.ITALIC + "/disguise player Notch\\\\"
							           + ChatColor.GOLD + "You can always see your own player disguise.";
	@LastUpdated(60001)
	public String HELP_DISGUISE_CHECK = ChatColor.GOLD.toString() + ChatColor.BOLD + "Check your disguise: " + ChatColor.ITALIC + "/disguise\\\\"
									  + ChatColor.GOLD + "You will be able to see your own disguise for another five\\\\"
									  + ChatColor.GOLD + "seconds.";
	@LastUpdated(60001)
	public String HELP_UNDISGUISE = ChatColor.GOLD.toString() + ChatColor.BOLD + "Undisguise: " + ChatColor.ITALIC + "/undisguise";
	@LastUpdated(60001)
	public String HELP_DISGUISE_ALTER = ChatColor.GOLD.toString() + ChatColor.BOLD + "Alter your disguise: " + ChatColor.ITALIC + "/disguise <type> <statement> ...\\\\"
									  + ChatColor.GOLD + "Example: " + ChatColor.ITALIC + "/disguise horse setColor(WHITE) setBaby()\\\\"
									  + ChatColor.GOLD + "Use [TAB] after entering the entity type to get a list of all\\\\"
									  + ChatColor.GOLD + "possible statements.";
	@LastUpdated(60001)
	public String HELP_INGAME_HELP = ChatColor.GOLD.toString() + ChatColor.BOLD + "In-game help: " + ChatColor.ITALIC + "/disguise ? [page]";
	@LastUpdated(60001)
	public String HELP_DISGUISE_PERMISSION = ChatColor.GOLD + "By default, only server operators can use the commands.\\\\"
										   + ChatColor.GOLD + "If you enable " + ChatColor.ITALIC + "commands.use-permission-nodes" + ChatColor.RESET + ChatColor.GOLD + " in the\\\\"
										   + ChatColor.GOLD + ChatColor.ITALIC + "config.yml" + ChatColor.RESET + ChatColor.GOLD + ", disguising requires the permission node\\\\"
										   + ChatColor.GOLD + ChatColor.BOLD + "iDisguise.disguise.<entity-type>\\\\"
										   + ChatColor.GOLD + "(e.g., " + ChatColor.ITALIC + "iDisguise.disguise.SNOW_GOLEM" + ChatColor.RESET + ChatColor.GOLD + " for the snow golem)\\\\"
										   + ChatColor.GOLD + "or the wildcard " + ChatColor.BOLD + "iDisguise.disguise.*";
	@LastUpdated(60001)
	public String HELP_ODISGUISE = ChatColor.GOLD.toString() + ChatColor.BOLD + "Disguise another player: " + ChatColor.ITALIC + "/odisguise <player>\\\\"
	                             + ChatColor.GOLD + ChatColor.BOLD + ChatColor.ITALIC + "<entity-type>";
	@LastUpdated(60001)
	public String HELP_UNDISGUISE_OTHER = ChatColor.GOLD.toString() + ChatColor.BOLD + "Undisguise another player: " + ChatColor.ITALIC + "/undisguise <player>";
	@LastUpdated(60001)
	public String HELP_ODISGUISE_INFO = ChatColor.GOLD + "Player disguise and statements work as well.";
	@LastUpdated(60001)
	public String HELP_INGAME_HELP_2 = ChatColor.GOLD.toString() + ChatColor.BOLD + "In-game help: " + ChatColor.ITALIC + "/odisguise ? [page]";
	@LastUpdated(60001)
	public String HELP_ODISGUISE_PERMISSION = ChatColor.GOLD + "By default, only server operators can use these commands.\\\\"
											+ ChatColor.GOLD + "If you enable " + ChatColor.ITALIC + "commands.use-permission-nodes" + ChatColor.RESET + ChatColor.GOLD + " in the\\\\"
											+ ChatColor.GOLD + ChatColor.ITALIC + "config.yml" + ChatColor.RESET + ChatColor.GOLD + ", these commands require the permission node\\\\"
											+ ChatColor.GOLD + ChatColor.BOLD + "iDisguise.others" + ChatColor.RESET + ChatColor.GOLD + " AND the respective disguise-specific\\\\"
											+ ChatColor.GOLD + "permission node.";

    @LastUpdated(60001)
	public String UPDATE_AVAILABLE = ChatColor.GOLD + "[iDisguise] An update is available: %version%";
	@LastUpdated(60001)
	public String UPDATE_ALREADY_DOWNLOADED = ChatColor.GOLD + "[iDisguise] Update already downloaded. Restart the server to apply.";
	@LastUpdated(60001)
	public String UPDATE_DOWNLOADING = ChatColor.GOLD + "[iDisguise] Downloading update...";
	@LastUpdated(60001)
	public String UPDATE_DOWNLOAD_SUCCEEDED = ChatColor.GOLD + "[iDisguise] Download succeeded. Restart the server to apply.";
	@LastUpdated(60001)
	public String UPDATE_DOWNLOAD_FAILED = ChatColor.RED + "[iDisguise] Download failed.";
	@LastUpdated(60001)
	public String UPDATE_OPTION = ChatColor.GOLD + "[iDisguise] You can enable automatic updates in the config file.";
	
	private iDisguise plugin;
	
	public Language(iDisguise plugin) {
		this.plugin = plugin;
	}
	
	public void loadData() {
		File languageFile = new File(plugin.getDataFolder(), "language.yml");
		FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(languageFile);
		try {
			int fileVersion = UpdateCheck.extractVersionNumber(fileConfiguration.getString("version", plugin.getNameAndVersion()));
			for(Field field : getClass().getDeclaredFields()) {
				if(field.getType().equals(String.class)) {
					if(field.isAnnotationPresent(LastUpdated.class) && field.getAnnotation(LastUpdated.class).value() > fileVersion) continue;
					
					if(fileConfiguration.isString(field.getName().toLowerCase(Locale.ENGLISH).replace('_', '-'))) {
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
