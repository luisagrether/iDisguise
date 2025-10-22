package de.luisagrether.idisguise.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

import de.luisagrether.idisguise.iDisguise;
import de.luisagrether.util.StringUtil;

public class Config {
	
	/* Configuration options start here */
    public static final String DISGUISE_TYPE_BLACKLIST_PATH = "disguise.disguise-type-blacklist";
	public static final String PLAYER_DISGUISE_BLACKLIST_PATH = "disguise.player-disguise-blacklist";
    public static final String STATEMENT_WHITELIST_PATH = "commands.statement-whitelist";
	public static final String USE_PERMISSION_NODES_PATH = "commands.use-permission-nodes";
	public static final String UPDATE_CHECK_PATH = "updates.check";
	public static final String UPDATE_DOWNLOAD_PATH = "updates.download";
	
	@LastUpdated(60001)
    public List<String> DISGUISE_TYPE_BLACKLIST = new ArrayList<>(Arrays.asList(
		"ARROW", "BLOCK_DISPLAY", "BREEZE_WIND_CHARGE", "COMPLEX_PART", "DRAGON_FIREBALL", "EGG", "ENDER_CRYSTAL", "END_CRYSTAL", "ENDER_DRAGON", "ENDER_PEARL",
		"ENDER_SIGNAL", "EVOKER_FANGS", "EXPERIENCE_ORB", "EXPERIENCE_BOTTLE", "EYE_OF_ENDER", "FALLING_BLOCK", "FIREBALL", "FIREWORK", "FIREWORK_ROCKET",
		"FISHING_BOBBER", "FISHING_HOOK", "GLOW_ITEM_FRAME", "INTERACTION", "ITEM_DISPLAY", "ITEM_FRAME", "LEASH_HITCH", "LEASH_KNOT", "LIGHTNING",
		"LIGHTNING_BOLT", "LINGERING_POTION", "LLAMA_SPIT", "MARKER", "MINECART_CHEST", "CHEST_MINECART", "MINECART_COMMAND", "COMMAND_BLOCK_MINECART",
		"MINECART_FURNACE", "FURNACE_MINECART", "MINECART_HOPPER", "HOPPER_MINECART", "MINECART_MOB_SPAWNER", "SPAWNER_MINECART", "MINECART_TNT", "TNT_MINECART",
		"OMINOUS_ITEM_SPAWNER", "PAINTING", "PRIMED_TNT", "SHULKER_BULLET", "SMALL_FIREBALL", "SNOWBALL", "SPECTRAL_ARROW", "SPLASH_POTION",
		"TEXT_DISPLAY", "THROWN_EXP_BOTTLE", "TIPPED_ARROW", "TRIDENT", "UNKNOWN", "WEATHER", "WIND_CHARGE", "WITHER_SKULL"
	));
	@LastUpdated(60002)
	public List<String> PLAYER_DISGUISE_BLACKLIST = Arrays.asList("put player name here");
	@LastUpdated(60002)
	public List<String> STATEMENT_WHITELIST = Arrays.asList(
		"setCustomName", "setGlowing", "setFireTicks", "setFreezeTicks", "setSilent", "setAdult", "setBaby", "setVariant", "setPlayingDead", "setHasNectar",
		"setCatType", "setCollarColor", "setCarryingChest", "setFoxType", "setSleeping", "setLeftHorn", "setRightHorn", "setColor", "setStyle",	"setMainGene",
		"setOnBack", "setRolling","setSneezing", "setDisplayBlock", "setDisplayBlockData", "setRabbitType", "setState", "setSaddle", "setShivering", "setAngry",
		"setSkeletonType", "setBoatType", "setAwake", "setProfession", "setVillagerType", "setSize", "setPuffState", "setVillager", "setItemStack", "setSitting",
		"setCarriedBlock", "setCarriedMaterial", "setSheared", "setBodyColor", "setPattern", "setPatternColor", "setVillagerProfession", "setPowered"
	);
	public boolean USE_PERMISSION_NODES = false;
	public boolean UPDATE_CHECK = true;
	public boolean UPDATE_DOWNLOAD = false;
	/* Configuration options end here */
	
	private iDisguise plugin;
	
	public Config(iDisguise plugin) {
		this.plugin = plugin;
	}
	
	public void loadData() {
		plugin.reloadConfig();
		FileConfiguration fileConfiguration = plugin.getConfig();
		try {
			int fileVersion = UpdateCheck.extractVersionNumber(fileConfiguration.getString("version", plugin.getNameAndVersion()));
			for(Field pathField : getClass().getDeclaredFields()) {
				if(pathField.getName().endsWith("_PATH")) {
					Field valueField = getClass().getDeclaredField(pathField.getName().substring(0, pathField.getName().length() - 5));
					if(valueField.isAnnotationPresent(LastUpdated.class) && valueField.getAnnotation(LastUpdated.class).value() > fileVersion) continue;

					if(fileConfiguration.isSet((String)pathField.get(null))) {
						if(fileConfiguration.isString((String)pathField.get(null))) {
							valueField.set(this, fileConfiguration.getString((String)pathField.get(null), (String)valueField.get(this)));
						} else if(fileConfiguration.isBoolean((String)pathField.get(null))) {
							valueField.setBoolean(this, fileConfiguration.getBoolean((String)pathField.get(null), valueField.getBoolean(this)));
						} else if(fileConfiguration.isDouble((String)pathField.get(null))) {
							valueField.setDouble(this, fileConfiguration.getDouble((String)pathField.get(null), valueField.getDouble(this)));
						} else if(fileConfiguration.isInt((String)pathField.get(null))) {
							valueField.setInt(this, fileConfiguration.getInt((String)pathField.get(null), valueField.getInt(this)));
						} else if(fileConfiguration.isList((String)pathField.get(null))) {
							if(valueField.getName().equals("DISGUISE_TYPE_BLACKLIST")) {
								for(String value : (List<String>)fileConfiguration.getList((String)pathField.get(null), (List<String>)valueField.get(this))) {
									if(!DISGUISE_TYPE_BLACKLIST.contains(value)) {
										DISGUISE_TYPE_BLACKLIST.add(value);
									}
								}
							} else if(valueField.getName().equals("PLAYER_DISGUISE_BLACKLIST")) {
								PLAYER_DISGUISE_BLACKLIST = new ArrayList<>();
								for(String value : (List<String>)fileConfiguration.getList((String)pathField.get(null), (List<String>)valueField.get(this))) {
									if(!PLAYER_DISGUISE_BLACKLIST.contains(value.toLowerCase(Locale.ENGLISH))) {
										PLAYER_DISGUISE_BLACKLIST.add(value.toLowerCase(Locale.ENGLISH));
									}
								}
							} else {
								valueField.set(this, fileConfiguration.getList((String)pathField.get(null), (List<String>)valueField.get(this)));
							}
						}
					}
				}
			}
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "An error occured while loading the config file.", e);
		}
	}
	
	public void saveData() {
		File configurationFile = new File(plugin.getDataFolder(), "config.yml");
		String config = StringUtil.readFrom(plugin.getResource("config.yml"));
		try {
			for(Field pathField : getClass().getDeclaredFields()) {
				if(pathField.getName().endsWith("_PATH")) {
					Field valueField = getClass().getDeclaredField(pathField.getName().substring(0, pathField.getName().length() - 5));
					if(valueField.getType() == List.class) {
						StringBuilder builder = new StringBuilder();
						for(Object object : ((List)valueField.get(this))) {
							builder.append("\n   - " + object.toString());
						}
						config = config.replace(valueField.getName(), builder.toString());
					} else {
						config = config.replace(valueField.getName(), valueField.get(this).toString());
					}
				}
			}
			config = config.replace("VERSION", plugin.getNameAndVersion());
			OutputStream output = new FileOutputStream(configurationFile);
			output.write(config.getBytes());
			output.close();
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "An error occured while saving the config file.", e);
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface LastUpdated {
		int value();
	}
	
}
