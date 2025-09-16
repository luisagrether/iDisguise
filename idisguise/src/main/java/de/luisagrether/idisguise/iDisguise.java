package de.luisagrether.idisguise;

import java.beans.Statement;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Multimap;

import de.luisagrether.idisguise.api.DisguiseAPI;
import de.luisagrether.idisguise.api.EventCancelledException;
import de.luisagrether.idisguise.api.PlayerDisguiseAsPlayerEvent;
import de.luisagrether.idisguise.api.PlayerDisguiseEvent;
import de.luisagrether.idisguise.api.PlayerInteractDisguisedPlayerEvent;
import de.luisagrether.idisguise.api.PlayerUndisguiseEvent;
import de.luisagrether.idisguise.io.Config;
import de.luisagrether.idisguise.io.Language;
import de.luisagrether.idisguise.io.UpdateCheck;
import de.luisagrether.util.StringUtil;

public class iDisguise extends JavaPlugin implements Listener, DisguiseAPI {
	
	public static final Pattern INT_VAL = Pattern.compile("[+-]?[0-9]+");
	public static final Pattern DOUBLE_VAL = Pattern.compile("[+-]?[0-9]*\\.[0-9]+");
	public static final Pattern ENUM_VAL = Pattern.compile("(?:([A-Za-z0-9.]+)\\.){0,1}([A-Za-z0-9_]+)");
	public static final Pattern ITEMSTACK_VAL = Pattern.compile("([A-Za-z0-9_]+),([0-9]+)");
	public static final Pattern STRING_VAL = Pattern.compile("\".*\"");
	public static final Pattern ACCOUNTNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
	private static String PACKAGE_VERSION;
	private static int[] MINECRAFT_VERSION;
	private static boolean LEGACY_INJECTION = false;
	private static Method LegacyInjector_inject = null;
	private static Method LegacyInjector_toggleIntercept = null;
	private static boolean LEGACY_DISABLE_AI = false;
	private static Method CraftEntity_getHandle = null;
	private static Class<?> EntityInsentient = null;
	private static Method EntityInsentient_setNoAI = null;
	private static boolean PLAYER_DISGUISE_AVAILABLE = false;
	private static boolean LEGACY_PROFILES = false;
	private static boolean PLAYER_DISGUISE_VIEWSELF = false;
	private static boolean LEGACY_PLAYER_DISGUISE_VIEWSELF = false;
	private static Field CraftOfflinePlayer_profile = null;
	private static Method CraftPlayer_getHandle = null;
	private static Method CraftPlayer_getProfile = null;
	private static Method CraftServer_getServer = null;
	private static Method MinecraftServer_getMinecraftSessionService = null;
	private static Method MinecraftSessionService_fetchProfile = null;
	private static Method MinecraftSessionService_fillProfileProperties = null;
	private static Method ProfileResult_getProfile = null;
	private static Constructor<?> GameProfile_new = null;
	private static Method GameProfile_getProperties = null;
	private static Class<?> EntityPlayer = null;
	private static Field EntityPlayer_playerConnection = null;
	private static Method PlayerConnection_sendPacket = null;
	private static Constructor<?> PacketUpdatePlayerInfo_new = null;
	private static Constructor<?> PacketRemovePlayerInfo_new = null;
	private static Object UpdatePlayerInfo_ADD_PLAYER = null;
	private static Object UpdatePlayerInfo_REMOVE_PLAYER = null;
	private static boolean LEGACY_MATERIALS = false;
	private static Method Material_createBlockData = null;
	private static Class<?> BlockData = null;
	private static Class<?> MaterialData = null;
	private static Constructor<?> MaterialData_new = null;

	static {
		PACKAGE_VERSION = Bukkit.getServer().getClass().getName();
		PACKAGE_VERSION = PACKAGE_VERSION.substring(0, PACKAGE_VERSION.lastIndexOf("."));
		PACKAGE_VERSION = PACKAGE_VERSION.substring(PACKAGE_VERSION.lastIndexOf(".") + 1);
		String[] v = PACKAGE_VERSION.substring(1).split("_");
		MINECRAFT_VERSION = new int[v.length];
		for(int i = 0; i < v.length; i++) {
			MINECRAFT_VERSION[i] = Integer.parseInt(v[i].replace("R", ""));
		}
		if(MINECRAFT_VERSION[1] < 18) {
			LEGACY_INJECTION = true;
			if(MINECRAFT_VERSION[1] >= 14) {
				try {
					Class<?> LegacyInjector = Class.forName("de.luisagrether.idisguise.impl.EntityTracker_" + PACKAGE_VERSION);
					LegacyInjector_inject = LegacyInjector.getMethod("inject", Entity.class, Player.class);
					LegacyInjector_toggleIntercept = LegacyInjector.getMethod("toggleIntercept", Entity.class, Player.class, boolean.class);
				} catch(ClassNotFoundException|NoSuchMethodException e) {
				}
			} else if(MINECRAFT_VERSION[1] >= 8) {
				try {
					Class<?> LegacyInjector = Class.forName("de.luisagrether.idisguise.impl.EntityTrackerEntry_" + PACKAGE_VERSION);
					LegacyInjector_inject = LegacyInjector.getMethod("inject", Entity.class, Player.class);
					LegacyInjector_toggleIntercept = LegacyInjector.getMethod("toggleIntercept", Entity.class, Player.class, boolean.class);
				} catch(ClassNotFoundException|NoSuchMethodException e) {
				}
			}
		}
		try {
			LivingEntity.class.getDeclaredMethod("setAI", boolean.class);
			// everything fine
		} catch(NoSuchMethodException e) {
			try {
				Class<?> CraftEntity = Class.forName("org.bukkit.craftbukkit." + PACKAGE_VERSION + ".entity.CraftEntity");
				CraftEntity_getHandle = CraftEntity.getMethod("getHandle");
				EntityInsentient = Class.forName("net.minecraft.server." + PACKAGE_VERSION + ".EntityInsentient");
				EntityInsentient_setNoAI = EntityInsentient.getDeclaredMethod("k", boolean.class);
				EntityInsentient_setNoAI.setAccessible(true);
				LEGACY_DISABLE_AI = true;
			} catch(ClassNotFoundException|NoSuchMethodException e2) {
				LEGACY_INJECTION = true;
				LegacyInjector_inject = null;
			}
		}
		try {
			Class<?> CraftOfflinePlayer = Class.forName("org.bukkit.craftbukkit." + PACKAGE_VERSION + ".CraftOfflinePlayer");
			CraftOfflinePlayer_profile = CraftOfflinePlayer.getDeclaredField("profile");
			CraftOfflinePlayer_profile.setAccessible(true);
			Class<?> CraftPlayer = Class.forName("org.bukkit.craftbukkit." + PACKAGE_VERSION + ".entity.CraftPlayer");
			CraftPlayer_getHandle = CraftPlayer.getMethod("getHandle");
			CraftPlayer_getProfile = CraftPlayer.getMethod("getProfile");
			Class<?> CraftServer = Bukkit.getServer().getClass();
			CraftServer_getServer = CraftServer.getMethod("getServer");
			Class<?> MinecraftServer = CraftServer_getServer.getReturnType();
			for(Method method : MinecraftServer.getMethods()) {
				if(method.getReturnType().getSimpleName().equals("MinecraftSessionService")) {
					MinecraftServer_getMinecraftSessionService = method;
					break;
				}
			}
			if(MinecraftServer_getMinecraftSessionService == null) throw new NoSuchMethodException("Method MinecraftServer.getMinecraftSessionService() not found.");
			
			Class<?> MinecraftSessionService = MinecraftServer_getMinecraftSessionService.getReturnType();
			for(Method method : MinecraftSessionService.getMethods()) {
				if(method.getName().equals("fetchProfile")) {
					MinecraftSessionService_fetchProfile = method;
					LEGACY_PROFILES = false;
					break;
				}
				if(method.getName().equals("fillProfileProperties")) {
					MinecraftSessionService_fillProfileProperties = method;
					LEGACY_PROFILES = true;
					break;
				}
			}
			if(MinecraftSessionService_fetchProfile == null && MinecraftSessionService_fillProfileProperties == null) throw new NoSuchMethodException("Method MinecraftSessionService.fetchProfile() not found.");
			
			Class<?> GameProfile = null;
			if(MinecraftSessionService_fetchProfile != null) {
				Class<?> ProfileResult = MinecraftSessionService_fetchProfile.getReturnType();
				ProfileResult_getProfile = ProfileResult.getMethod("profile");
				GameProfile = ProfileResult_getProfile.getReturnType();

			} else if(MinecraftSessionService_fillProfileProperties != null) {
				GameProfile = MinecraftSessionService_fillProfileProperties.getReturnType();
			}
			GameProfile_new = GameProfile.getConstructor(UUID.class, String.class);
			GameProfile_getProperties = GameProfile.getDeclaredMethod("getProperties");
			PLAYER_DISGUISE_AVAILABLE = true;
		} catch(ClassNotFoundException|NoSuchFieldException|NoSuchMethodException e) {
			e.printStackTrace();
		}
		try {
			EntityPlayer = CraftPlayer_getHandle.getReturnType();
			for(Field field : EntityPlayer.getFields()) {
				if(field.getType().getSimpleName().equals("PlayerConnection")) {
					EntityPlayer_playerConnection = field;
					break;
				}
			}
			if(EntityPlayer_playerConnection == null) throw new NoSuchFieldException("Field EntityPlayer.playerConnection not found.");

			Class<?> PlayerConnection = EntityPlayer_playerConnection.getType();
			for(Method method : PlayerConnection.getMethods()) {
				if(StringUtil.equals(method.getName(), "sendPacket", "send") && method.getParameterTypes().length == 1 /*&& method.getParameterTypes()[0].getSimpleName().equals("Packet")*/) {
					PlayerConnection_sendPacket = method;
					break;
				}
			}
			if(PlayerConnection_sendPacket == null) throw new NoSuchMethodException("Method PlayerConnection.sendPacket() not found.");

			try {
				Class<?> PacketRemovePlayerInfo = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
				PacketRemovePlayerInfo_new = PacketRemovePlayerInfo.getConstructor(List.class);
				Class<?> PacketUpdatePlayerInfo = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
				Class<?> UpdatePlayerInfo = null;
				for(Class<?> clazz : PacketUpdatePlayerInfo.getDeclaredClasses()) {
					if(clazz.isEnum()) {
						UpdatePlayerInfo = clazz;
						break;
					}
				}
				PacketUpdatePlayerInfo_new = PacketUpdatePlayerInfo.getConstructor(UpdatePlayerInfo, EntityPlayer);
				UpdatePlayerInfo_ADD_PLAYER = UpdatePlayerInfo.getEnumConstants()[0];
				PLAYER_DISGUISE_VIEWSELF = true;
			} catch(ClassNotFoundException e) {
				try {
					Class<?> PacketUpdatePlayerInfo = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo");
					Class<?> UpdatePlayerInfo = null;
					for(Class<?> clazz : PacketUpdatePlayerInfo.getDeclaredClasses()) {
						if(clazz.isEnum()) {
							UpdatePlayerInfo = clazz;
							break;
						}
					}
					PacketUpdatePlayerInfo_new = PacketUpdatePlayerInfo.getConstructor(UpdatePlayerInfo, Array.newInstance(EntityPlayer, 0).getClass());
					UpdatePlayerInfo_ADD_PLAYER = UpdatePlayerInfo.getEnumConstants()[0];
					UpdatePlayerInfo_REMOVE_PLAYER = UpdatePlayerInfo.getEnumConstants()[UpdatePlayerInfo.getEnumConstants().length - 1];
					PLAYER_DISGUISE_VIEWSELF = true;
					LEGACY_PLAYER_DISGUISE_VIEWSELF = true;
				} catch(ClassNotFoundException e2) {
					try {
						Class<?> PacketUpdatePlayerInfo = Class.forName("net.minecraft.server." + PACKAGE_VERSION + ".PacketPlayOutPlayerInfo");
						Class<?> UpdatePlayerInfo = null;
						for(Class<?> clazz : PacketUpdatePlayerInfo.getDeclaredClasses()) {
							if(clazz.isEnum()) {
								UpdatePlayerInfo = clazz;
								break;
							}
						}
						if(UpdatePlayerInfo == null) {
							UpdatePlayerInfo = Class.forName("net.minecraft.server." + PACKAGE_VERSION + ".EnumPlayerInfoAction");
						}
						PacketUpdatePlayerInfo_new = PacketUpdatePlayerInfo.getConstructor(UpdatePlayerInfo, Array.newInstance(EntityPlayer, 0).getClass());
						UpdatePlayerInfo_ADD_PLAYER = UpdatePlayerInfo.getEnumConstants()[0];
						UpdatePlayerInfo_REMOVE_PLAYER = UpdatePlayerInfo.getEnumConstants()[UpdatePlayerInfo.getEnumConstants().length - 1];
						PLAYER_DISGUISE_VIEWSELF = true;
						LEGACY_PLAYER_DISGUISE_VIEWSELF = true;
					} catch(ClassNotFoundException e3) {
						e.printStackTrace();
					}
				}
			}
		} catch(NoSuchFieldException|NoSuchMethodException e) {
			e.printStackTrace();
		}
		try {
			Material_createBlockData = Material.class.getMethod("createBlockData");
			BlockData = Material_createBlockData.getReturnType();
		} catch(NoSuchMethodException e) {
			System.out.println("Legacy Materials.");
			LEGACY_MATERIALS = true;
			try {
				MaterialData = Class.forName("org.bukkit.material.MaterialData");
				MaterialData_new = MaterialData.getConstructor(Material.class);
			} catch(ClassNotFoundException|NoSuchMethodException e2) {
			}
		}
	}
	
	private static iDisguise INSTANCE;
	
	private boolean debugMode;
	private Config config;
	private Language language;
	private Metrics metrics;
	private Map<EntityType, List<String>> tabCompletions = new HashMap<>();
	private Map<UUID, Entity> disguiseMap = new HashMap<>();
	private Map<UUID, String> playerDisguiseMap = new HashMap<>();
	private Map<String, Object> profileDatabase = new HashMap<>();
	private World dummyWorld;
	
	public iDisguise() { INSTANCE = this; }
	
	public void onEnable() {
		if(LEGACY_INJECTION && LegacyInjector_inject == null) {
			getLogger().severe("This Minecraft server version is not supported!");
			getPluginLoader().disablePlugin(this);
			return;
		}

		if(!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		debugMode = new File(getDataFolder(), "debug").isFile();

		config = new Config(this);
		config.loadData();
		config.saveData();

		language = new Language(this);
		language.loadData();
		language.saveData();

		for(EntityType type : EntityType.values()) {
			if(config.DISGUISE_TYPE_BLACKLIST.contains(type.name())) continue;
			
			List<String> completions = new ArrayList<>();

			if(type == EntityType.PLAYER) {
				completions.add("<ACCOUNT-NAME>");
				completions.add("Notch");
				completions.add("jeb_");
				completions.add("Dinnerbone");
				completions.add("LuisaGrether");
			} else {
				Class<?> clazz = type.getEntityClass();
				for(Method method : clazz.getMethods()) {
					String methodName = method.getName();
					if(!config.STATEMENT_WHITELIST.contains(methodName)) continue;

					if(!LEGACY_MATERIALS && methodName.equals("setDisplayBlock")) continue;
					if(LEGACY_MATERIALS && methodName.equals("setDisplayBlockData")) continue;

					if(method.isAnnotationPresent(Deprecated.class)) continue;

					if(method.getParameterTypes().length == 0) {
						completions.add(methodName + "()");
					} else if(method.getParameterTypes().length == 1) {
						if(method.getParameterTypes()[0] == boolean.class) {
							completions.add(methodName + "(true)");
							completions.add(methodName + "(false)");
						} else if(method.getParameterTypes()[0] == int.class) {
							completions.add(methodName + "(<int>)");
						} else if(method.getParameterTypes()[0] == double.class || method.getParameterTypes()[0] == float.class) {
							completions.add(methodName + "(<float>)");
						} else if(method.getParameterTypes()[0] == ItemStack.class) {
							try {
								Method name = Enum.class.getDeclaredMethod("name");
								for(Material value : Material.class.getEnumConstants()) {
									completions.add(methodName + "(" + name.invoke(value) + ")");
									completions.add(methodName + "(" + name.invoke(value) + ",<int>)");
								}
							} catch(Exception e) {
							}
						} else if(method.getParameterTypes()[0] == (LEGACY_MATERIALS ? MaterialData : BlockData)) {
							try {
								Method name = Enum.class.getDeclaredMethod("name");
								for(Material value : Material.class.getEnumConstants()) {
									if(((String)name.invoke(value)).startsWith(Material.LEGACY_PREFIX)) continue;
									if(value.isBlock()) {
										completions.add(methodName + "(" + name.invoke(value) + ")");
									}
								}
							} catch(Exception e) {
							}
						} else if(method.getParameterTypes()[0].isEnum()) {
							try {
								Method name = Enum.class.getDeclaredMethod("name");
								for(Object value : method.getParameterTypes()[0].getEnumConstants()) {
									completions.add(methodName + "(" + name.invoke(value) + ")");
								}
							} catch(Exception e) {
							}
						} else if(method.getParameterTypes()[0] == String.class) {
							completions.add(methodName + "(\"YOUR TEXT\")");
						} else {
							try {
								if(Class.forName("org.bukkit.util.OldEnum").isAssignableFrom(method.getParameterTypes()[0])) {
									for(Field field : method.getParameterTypes()[0].getDeclaredFields()) {
										if(field.getType().equals(method.getParameterTypes()[0])) {
											completions.add(methodName + "(" + field.getName() + ")");
										}
									}
								}
							} catch(Exception e) {
							}
						}
					}
				}
			}

			tabCompletions.put(type, completions);
		}
		Bukkit.getPluginManager().registerEvents(this, this);

		if(PLAYER_DISGUISE_AVAILABLE && PLAYER_DISGUISE_VIEWSELF) {
			dummyWorld = Bukkit.getWorld("iDisguise-Dummy");
			if(dummyWorld == null) {
				dummyWorld = Bukkit.createWorld(WorldCreator.name("iDisguise-Dummy"));
			}
		}

		if(config.USE_PERMISSION_NODES) {
			Bukkit.getPluginManager().addPermission(new Permission("iDisguise.disguise.*", PermissionDefault.OP));
			for(EntityType type : EntityType.values()) {
				if(config.DISGUISE_TYPE_BLACKLIST.contains(type.name())) continue;
				Bukkit.getPluginManager().addPermission(new Permission("iDisguise.disguise." + type.name(), PermissionDefault.OP));
			}
		}

		Bukkit.getServicesManager().register(DisguiseAPI.class, this, this, ServicePriority.Normal);

		/* Metrics start */
		metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.SingleLineChart("disguisedPlayers", () -> (disguiseMap.size() + playerDisguiseMap.size())));
		metrics.addCustomChart(new Metrics.SimplePie("storageType", () -> "unavailable"));
		metrics.addCustomChart(new Metrics.SimplePie("updateChecking", () -> config.UPDATE_CHECK ? config.UPDATE_DOWNLOAD ? "check and download" : "check only" : "disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("realisticSoundEffects", () -> "enabled"));
		metrics.addCustomChart(new Metrics.SimplePie("undisguisePermission", () -> "unavailable"));
		metrics.addCustomChart(new Metrics.SimplePie("viewableDisguises", () -> "enabled"));
		metrics.addCustomChart(new Metrics.SimplePie("channelInjector", () -> "unavailable"));
		metrics.addCustomChart(new Metrics.SimplePie("ghostDisguise", () -> "unavailable"));
		/* Metrics end */

		if(config.UPDATE_CHECK) {
			getServer().getScheduler().runTaskLaterAsynchronously(this, new UpdateCheck(this, getServer().getConsoleSender(), config.UPDATE_DOWNLOAD), 20L);
		}

		getLogger().info("Enabled!");
	}
	
	public void onDisable() {
		for(Entity entity : disguiseMap.values()) {
			entity.remove();
		}
		disguiseMap.clear();

		getLogger().info("Disabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(language.CONSOLE_USE_COMMAND);
			return true;
		}

		if(debugMode) {
			for(int i = 0; i < args.length; i++) getLogger().info("args[" + i + "] = \"" + args[i] + "\"");
		}
		String argsJoined = String.join(" ", args);
		List<String> argsSplit = new ArrayList<>();
		int lastSplit = 0;
		int index = 0;
		boolean quotation = false;
		while(index < argsJoined.length()) {
			if(argsJoined.charAt(index) == ' ') {
				if(!quotation) {
					if(index != lastSplit) argsSplit.add(argsJoined.substring(lastSplit, index).replace("\\\"", "\""));
					lastSplit = index + 1;
				}
			} else if(argsJoined.charAt(index) == '"' && (index == 0 || argsJoined.charAt(index - 1) != '\\')) {
				quotation = !quotation;
			}
			index++;
		}
		if(index != lastSplit) argsSplit.add(argsJoined.substring(lastSplit, index));
		args = argsSplit.toArray(new String[0]);
		if(debugMode) {
			for(int i = 0; i < args.length; i++) getLogger().info("new_args[" + i + "] = \"" + args[i] + "\"");
		}

		if(command.getName().equalsIgnoreCase("disguise")) {
			if(args.length == 0) {
				if(isDisguised((Player)sender)) {
					if(getDisguise((Player)sender) == EntityType.PLAYER) {
						sender.sendMessage(language.CURRENTLY_DISGUISED_AS_PLAYER.replace("%targetSkin%", playerDisguiseMap.get(((Player)sender).getUniqueId())));
					} else {
						sender.sendMessage(language.CURRENTLY_DISGUISED_AS_ENTITY.replace("%entityType%", getDisguise((Player)sender).name()));
						((Player)sender).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1, true, false));
						Entity finalEntity = disguiseMap.get(((Player)sender).getUniqueId());
						if(!LEGACY_INJECTION) {
							((Player)sender).showEntity(this, finalEntity);
						} else {
							try {
								LegacyInjector_toggleIntercept.invoke(null, finalEntity, sender, false);
							} catch(Exception e) {
								if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
							}
						}
						Bukkit.getScheduler().runTaskLater(this, () -> {
							if(!LEGACY_INJECTION) {
								((Player)sender).hideEntity(this, finalEntity);
							} else {
								try {
									LegacyInjector_toggleIntercept.invoke(null, finalEntity, sender, true);
								} catch(Exception e) {
									if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
								}
							}
						}, 100L);
					}
				} else {
					sender.sendMessage(language.CURRENTLY_NOT_DISGUISED);
				}
			} else if(args[0].equalsIgnoreCase("player")) {
				if(!PLAYER_DISGUISE_AVAILABLE || config.DISGUISE_TYPE_BLACKLIST.contains("PLAYER")) {
					sender.sendMessage(language.DISGUISE_TYPE_NOT_SUPPORTED);
				} else if(!hasPermission((Player)sender, EntityType.PLAYER)) {
					sender.sendMessage(language.DISGUISE_NO_PERMISSION);
				} else if(args.length == 1) {
					sender.sendMessage(language.PLAYER_DISGUISE_ACCOUNT_NAME_MISSING);
				} else if(!ACCOUNTNAME.matcher(args[1]).matches()) {
					sender.sendMessage(language.PLAYER_DISGUISE_ACCOUNT_NAME_INVALID);
				} else {
					try {
						disguiseAsPlayer((Player)sender, args[1], (success) -> {
							if(success) {
								sender.sendMessage(language.DISGUISED_SUCCESSFULLY);
							} else {
								sender.sendMessage(language.DISGUISE_ERROR);
							}
						});
					} catch(EventCancelledException e) {
						sender.sendMessage(language.DISGUISE_EVENT_CANCELLED);
					}
				}
			} else {
				try {
					EntityType type = EntityType.valueOf(args[0].toUpperCase(Locale.ENGLISH).replace('-', '_'));
					if(config.DISGUISE_TYPE_BLACKLIST.contains(type.name())) {
						sender.sendMessage(language.DISGUISE_TYPE_NOT_SUPPORTED);
					} else if(!hasPermission((Player)sender, EntityType.PLAYER)) {
						sender.sendMessage(language.DISGUISE_NO_PERMISSION);
					} else {
						try {
							Entity entity = disguise((Player)sender, type);
							for(int i = 1; i < args.length; i++) {
								String codeLine = args[i];
								String[] codeFrags = codeLine.split("[()]", -1);
								if(config.STATEMENT_WHITELIST.contains(codeFrags[0])) {
									Statement statement = null;
									Matcher m;
									if(codeFrags[1].length() == 0) {
										statement = new Statement(entity, codeFrags[0], new Object[0]);
									} else if(codeFrags[1].equals("true")) {
										statement = new Statement(entity, codeFrags[0], new Object[] {true});
									} else if(codeFrags[1].equals("false")) {
										statement = new Statement(entity, codeFrags[0], new Object[] {false});
									} else if(INT_VAL.matcher(codeFrags[1]).matches()) {
										statement = new Statement(entity, codeFrags[0], new Object[] {Integer.valueOf(codeFrags[1])});
									} else if(DOUBLE_VAL.matcher(codeFrags[1]).matches()) {
										statement = new Statement(entity, codeFrags[0], new Object[] {Double.valueOf(codeFrags[1])});
									} else if((m = ENUM_VAL.matcher(codeFrags[1])).matches()) {
										Class<?> enumClazz = null;
										if(m.group(1) == null || m.group(1).isEmpty()) {
											for(Method method : entity.getType().getEntityClass().getMethods()) {
												if(method.getName().equals(codeFrags[0]) && method.getParameterTypes().length == 1) {
													enumClazz = method.getParameterTypes()[0];
												}
											}
										} else {
											try {
												enumClazz = Class.forName(m.group(1));
											} catch(ClassNotFoundException e) {
												try {
													enumClazz = Class.forName("org.bukkit.entity." + m.group(1));
												} catch(ClassNotFoundException e2) {
													for(Class<?> innerClazz : entity.getType().getEntityClass().getDeclaredClasses()) {
														if(innerClazz.getSimpleName().equalsIgnoreCase(m.group(1))) {
															enumClazz = innerClazz;
															break;
														}
													}
													if(enumClazz == null) {
														try {
															enumClazz = Class.forName("org.bukkit." + m.group(1));
														} catch(ClassNotFoundException e3) {
														}
													}
												}
											}
										}
										if(enumClazz == (LEGACY_MATERIALS ? MaterialData : BlockData)) {
											try {
												Material material = (Material)Material.class.getDeclaredField(m.group(2)).get(null);
												statement = new Statement(entity, codeFrags[0], new Object[] {
													LEGACY_MATERIALS ? MaterialData_new.newInstance(material) : Material_createBlockData.invoke(material)
												});
											} catch(NoSuchFieldException|IllegalAccessException|InstantiationException|InvocationTargetException e) {
												sender.sendMessage(language.DISGUISE_STATEMENT_ERROR.replace("%statement%", codeLine));
												if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
											}
										} else if(enumClazz == ItemStack.class) {
											try {
												statement = new Statement(entity, codeFrags[0], new Object[] {new ItemStack((Material)Material.class.getDeclaredField(m.group(2)).get(null))});
											} catch(NoSuchFieldException|IllegalAccessException e) {
												sender.sendMessage(language.DISGUISE_STATEMENT_ERROR.replace("%statement%", codeLine));
												if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
											}
										} else if(enumClazz != null) {
											try {
												statement = new Statement(entity, codeFrags[0], new Object[] {enumClazz.getDeclaredField(m.group(2)).get(null)});
											} catch(NoSuchFieldException|IllegalAccessException e) {
												sender.sendMessage(language.DISGUISE_STATEMENT_ERROR.replace("%statement%", codeLine));
												if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
											}
										} else {
											sender.sendMessage(language.DISGUISE_STATEMENT_ERROR.replace("%statement%", codeLine));
										}
									} else if((m = ITEMSTACK_VAL.matcher(codeFrags[1])).matches()) {
										try {
											statement = new Statement(entity, codeFrags[0], new Object[] {new ItemStack((Material)Material.class.getDeclaredField(m.group(1)).get(null), Integer.parseInt(m.group(2)))});
										} catch(NoSuchFieldException|IllegalAccessException e) {
											sender.sendMessage(language.DISGUISE_STATEMENT_ERROR.replace("%statement%", codeLine));
											if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
										}
									} else if(STRING_VAL.matcher(codeFrags[1]).matches()) {
										statement = new Statement(entity, codeFrags[0], new Object[] {codeFrags[1].substring(1, codeFrags[1].length() - 1)});
									}
									if(statement != null) {
										try {
											statement.execute();
											if(statement.getMethodName().equals("setCustomName")) {
												new Statement(entity, "setCustomNameVisible", new Object[] {true}).execute();
											}
										} catch (Exception e) {
											sender.sendMessage(language.DISGUISE_STATEMENT_ERROR.replace("%statement%", codeLine));
											if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
										}
									} else {
										sender.sendMessage(language.DISGUISE_STATEMENT_UNKNOWN.replace("%statement%", codeLine));
									}
								} else {
									sender.sendMessage(language.DISGUISE_STATEMENT_FORBIDDEN.replace("%statement%", codeFrags[0]));
								}
							}
							sender.sendMessage(language.DISGUISED_SUCCESSFULLY);
						} catch(EventCancelledException e) {
							sender.sendMessage(language.DISGUISE_EVENT_CANCELLED);
						}
					}
				} catch(IllegalArgumentException e) {
					if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
					sender.sendMessage(language.DISGUISE_TYPE_NOT_FOUND.replace("%entityType%", args[0]));
				}
			}
		} else if(command.getName().equalsIgnoreCase("undisguise")) {
			if(isDisguised((Player)sender)) {
				try {
					undisguise((Player)sender);
					sender.sendMessage(language.UNDISGUISED_SUCCESSFULLY);
				} catch(EventCancelledException e) {
					sender.sendMessage(language.UNDISGUISE_EVENT_CANCELLED);
				}
			} else {
				sender.sendMessage(language.UNDISGUISE_NOT_DISGUISED);
			}
		}
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<String>();
		if(!(sender instanceof Player)) {
			return completions;
		}

		if(command.getName().equalsIgnoreCase("disguise")) {
			if(args.length < 2) {
				for(EntityType type : EntityType.values()) {
					if(type.equals(EntityType.PLAYER) && !PLAYER_DISGUISE_AVAILABLE) continue;
					if(!config.DISGUISE_TYPE_BLACKLIST.contains(type.name()) && hasPermission((Player)sender, type)) {
						completions.add(type.name());
					}
				}
				if(args.length > 0) {
					for(int i = 0; i < completions.size(); i++) {
						if(!StringUtil.startsWithIgnoreCase(completions.get(i), args[0].replace('-', '_'))) {
							completions.remove(i);
							i--;
						}
					}
				}
			} else {
				try {
					EntityType type = EntityType.valueOf(args[0].toUpperCase(Locale.ENGLISH).replace('-', '_'));
					if(hasPermission((Player)sender, type) && tabCompletions.containsKey(type)) {
						if(!type.equals(EntityType.PLAYER) || args.length < 3) {
							completions.addAll(tabCompletions.get(type));
						}
					}
					for(int i = 0; i < completions.size(); i++) {
						if(!completions.get(i).startsWith(args[args.length - 1])) {
							completions.remove(i);
							i--;
						}
					}
				} catch(IllegalArgumentException e) {
				}
			}
		}
		return completions;
	}

	private boolean hasPermission(Player player, EntityType type) {
		if(!config.USE_PERMISSION_NODES) {
			return player.isOp();
		} else {
			return player.hasPermission("iDisguise.disguise.*") ||
				   player.hasPermission("iDisguise.disguise." + type.name());
		}
	}
	
	public synchronized EntityType getDisguise(Player player) {
		if(disguiseMap.containsKey(player.getUniqueId())) {
			return disguiseMap.get(player.getUniqueId()).getType();
		}
		if(playerDisguiseMap.containsKey(player.getUniqueId())) {
			return EntityType.PLAYER;
		}
		return null;
	}
	
	public synchronized boolean isDisguised(Player player) {
		return disguiseMap.containsKey(player.getUniqueId()) || playerDisguiseMap.containsKey(player.getUniqueId());
	}

	public Entity disguise(Player player, EntityType type, boolean fireEvent) throws EventCancelledException {
		if(config.DISGUISE_TYPE_BLACKLIST.contains(type.name())) throw new UnsupportedOperationException("Currently not supported!");
		if(type == EntityType.PLAYER) throw new UnsupportedOperationException();
		
		if(fireEvent) {
			PlayerDisguiseEvent event = new PlayerDisguiseEvent(player, type);
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled()) {
				throw new EventCancelledException();
			}
		}

		return disguise0(player, type);
	}

	private synchronized Entity disguise0(Player player, EntityType type) {
		if(config.DISGUISE_TYPE_BLACKLIST.contains(type.name())) throw new UnsupportedOperationException("Currently not supported!");
		if(type == EntityType.PLAYER) throw new UnsupportedOperationException();
		
		if(isDisguised(player)) undisguise0(player);

		Entity entity;
		if(type == EntityType.DROPPED_ITEM) {
			entity = player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.STONE));
		} else {
			entity = player.getWorld().spawnEntity(player.getLocation(), type);
		}
		if(entity instanceof LivingEntity) {
			if(!LEGACY_DISABLE_AI) {
				((LivingEntity)entity).setAI(false);
			} else {
				try {
					Object nmsEntity = CraftEntity_getHandle.invoke(entity);
					if(EntityInsentient.isInstance(nmsEntity)) {
						EntityInsentient_setNoAI.invoke(nmsEntity, true);
					}
				} catch(Exception e) {
					if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
				}
			}
		} else if(entity instanceof Item) {
			((Item)entity).setPickupDelay(Integer.MAX_VALUE);
		}
		entity.setMetadata("iDisguise", new FixedMetadataValue(this, player.getUniqueId()));
		if(LEGACY_INJECTION) {
			try {
				LegacyInjector_inject.invoke(null, entity, player);
			} catch(Exception e) {
				if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
			}
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1, true, false));
		if(type.name().equals("SHULKER")) {
			Location to = player.getLocation();
			entity.teleport(new Location(to.getWorld(), to.getBlockX()+0.5, to.getBlockY()+0.0, to.getBlockZ()+0.5));
		} else {
			entity.teleport(player.getLocation());
		}
		disguiseMap.put(player.getUniqueId(), entity);
		for(Player observer : Bukkit.getOnlinePlayers()) {
			if(observer != player) {
				observer.hidePlayer(player);
			}
		}

		Entity finalEntity = entity;
		Bukkit.getScheduler().runTaskLater(this, () -> {
			if(!LEGACY_INJECTION) {
				player.hideEntity(this, finalEntity);
			} else {
				try {
					LegacyInjector_toggleIntercept.invoke(null, finalEntity, player, true);
				} catch(Exception e) {
					if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
				}
			}
		}, 100L);

		return entity;
	}

	public void disguiseAsPlayer(Player player, String targetSkin, boolean fireEvent, @Nullable Consumer<Boolean> callback) throws EventCancelledException {
		if(!PLAYER_DISGUISE_AVAILABLE || config.DISGUISE_TYPE_BLACKLIST.contains("PLAYER")) throw new UnsupportedOperationException("Currently not supported!");
		if(!ACCOUNTNAME.matcher(targetSkin).matches()) throw new IllegalArgumentException("This account name is invalid.");
		
		if(fireEvent) {
			PlayerDisguiseAsPlayerEvent event = new PlayerDisguiseAsPlayerEvent(player, targetSkin);
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled()) {
				throw new EventCancelledException();
			}
		}

		disguiseAsPlayer0(player, targetSkin, callback);
	}

	private void disguiseAsPlayer0(Player player, String targetSkin, @Nullable Consumer<Boolean> callback) {
		if(!PLAYER_DISGUISE_AVAILABLE || config.DISGUISE_TYPE_BLACKLIST.contains("PLAYER")) throw new UnsupportedOperationException("Currently not supported!");
		if(!ACCOUNTNAME.matcher(targetSkin).matches()) throw new IllegalArgumentException("This account name is invalid.");
		
		if(isDisguised(player)) undisguise0(player);

		if(!profileDatabase.containsKey(targetSkin.toLowerCase(Locale.ENGLISH))) {
			retrieveProfile(targetSkin, (success) -> {
				if(success) {
					disguiseAsPlayer0(player, targetSkin, callback);
				} else {
					callback.accept(false);
				}
			});
			return;
		}

		synchronized(this) {
			try {
				Object targetProfile = CraftPlayer_getProfile.invoke(player);
				Multimap sourceMap = (Multimap)GameProfile_getProperties.invoke(profileDatabase.get(targetSkin.toLowerCase(Locale.ENGLISH)));
				Multimap targetMap = (Multimap)GameProfile_getProperties.invoke(targetProfile);
				if(sourceMap.containsKey("textures")) {
					if(targetMap.containsKey("textures")) {
						targetMap.removeAll("textures");
					}
					targetMap.putAll("textures", sourceMap.get("textures"));
				}
				
				playerDisguiseMap.put(player.getUniqueId(), targetSkin);
				for(Player observer : Bukkit.getOnlinePlayers()) {
					if(observer != player) {
						observer.hidePlayer(player);
					}
				}
				for(Player observer : Bukkit.getOnlinePlayers()) {
					if(observer != player) {
						observer.showPlayer(player);
					}
				}

				if(PLAYER_DISGUISE_VIEWSELF) {
					Object entityPlayer = CraftPlayer_getHandle.invoke(player);
					if(!LEGACY_PLAYER_DISGUISE_VIEWSELF) {
						Object PacketRemovePlayerInfo = PacketRemovePlayerInfo_new.newInstance(Arrays.asList(player.getUniqueId()));
						Object PacketUpdatePlayerInfo = PacketUpdatePlayerInfo_new.newInstance(UpdatePlayerInfo_ADD_PLAYER, entityPlayer);
						Object playerConnection = EntityPlayer_playerConnection.get(entityPlayer);
						PlayerConnection_sendPacket.invoke(playerConnection, PacketRemovePlayerInfo);
						PlayerConnection_sendPacket.invoke(playerConnection, PacketUpdatePlayerInfo);
					} else {
						Object entityPlayerArray = Array.newInstance(EntityPlayer, 1);
						Array.set(entityPlayerArray, 0, entityPlayer);
						Object PacketRemovePlayerInfo = PacketUpdatePlayerInfo_new.newInstance(UpdatePlayerInfo_REMOVE_PLAYER, entityPlayerArray);
						Object PacketUpdatePlayerInfo = PacketUpdatePlayerInfo_new.newInstance(UpdatePlayerInfo_ADD_PLAYER, entityPlayerArray);
						Object playerConnection = EntityPlayer_playerConnection.get(entityPlayer);
						PlayerConnection_sendPacket.invoke(playerConnection, PacketRemovePlayerInfo);
						PlayerConnection_sendPacket.invoke(playerConnection, PacketUpdatePlayerInfo);
					}

					Location originalLocation = player.getLocation();
					player.teleport(dummyWorld.getSpawnLocation());
					player.teleport(originalLocation);
				}

				if(callback != null) Bukkit.getScheduler().runTask(iDisguise.this, () -> callback.accept(true));
			} catch(IllegalAccessException|InvocationTargetException|IllegalStateException|InstantiationException e) {
				if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
				if(callback != null) Bukkit.getScheduler().runTask(iDisguise.this, () -> callback.accept(false));
			}
		}
	}

	private void retrieveProfile(String targetSkin, @Nullable Consumer<Boolean> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
			OfflinePlayer sourcePlayer = Bukkit.getOfflinePlayer(targetSkin);
			try {
				Object sourceProfile = sourcePlayer instanceof Player ? CraftPlayer_getProfile.invoke(sourcePlayer) : CraftOfflinePlayer_profile.get(sourcePlayer);
				if(!((Multimap)GameProfile_getProperties.invoke(sourceProfile)).containsKey("textures")) {
					if(!LEGACY_PROFILES) {
						sourceProfile = ProfileResult_getProfile.invoke(MinecraftSessionService_fetchProfile.invoke(MinecraftServer_getMinecraftSessionService.invoke(CraftServer_getServer.invoke(Bukkit.getServer())), sourcePlayer.getUniqueId(), true));
					} else {
						MinecraftSessionService_fillProfileProperties.invoke(MinecraftServer_getMinecraftSessionService.invoke(CraftServer_getServer.invoke(Bukkit.getServer())), sourceProfile, true);
					}
				}
				Object copiedProfile = GameProfile_new.newInstance(sourcePlayer.getUniqueId(), sourcePlayer.getName());
				Multimap sourceMap = (Multimap)GameProfile_getProperties.invoke(sourceProfile);
				Multimap targetMap = (Multimap)GameProfile_getProperties.invoke(copiedProfile);
				targetMap.putAll(sourceMap);
				
				profileDatabase.put(targetSkin.toLowerCase(Locale.ENGLISH), copiedProfile);

				if(callback != null) Bukkit.getScheduler().runTask(iDisguise.this, () -> callback.accept(true));
			} catch(IllegalAccessException|InvocationTargetException|InstantiationException e) {
				if(callback != null) Bukkit.getScheduler().runTask(iDisguise.this, () -> callback.accept(false));
			}
		});
	}
	
	public EntityType undisguise(Player player, boolean fireEvent) throws EventCancelledException {
		if(!isDisguised(player)) return null;

		if(fireEvent) {
			PlayerUndisguiseEvent event = new PlayerUndisguiseEvent(player);
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled()) {
				throw new EventCancelledException();
			}
		}

		return undisguise0(player);
	}

	private synchronized EntityType undisguise0(Player player) {
		if(!isDisguised(player)) return null;
		
		if(getDisguise(player) == EntityType.PLAYER) {
			try {
				Object targetProfile = CraftPlayer_getProfile.invoke(player);
				Multimap sourceMap = (Multimap)GameProfile_getProperties.invoke(profileDatabase.get(player.getName().toLowerCase(Locale.ENGLISH)));
				Multimap targetMap = (Multimap)GameProfile_getProperties.invoke(targetProfile);
				if(sourceMap.containsKey("textures")) {
					if(targetMap.containsKey("textures")) {
						targetMap.removeAll("textures");
					}
					targetMap.putAll("textures", sourceMap.get("textures"));
				}

				playerDisguiseMap.remove(player.getUniqueId());
				for(Player observer : Bukkit.getOnlinePlayers()) {
					if(observer != player) {
						observer.hidePlayer(player);
					}
				}
				for(Player observer : Bukkit.getOnlinePlayers()) {
					if(observer != player) {
						observer.showPlayer(player);
					}
				}

				if(PLAYER_DISGUISE_VIEWSELF) {
					Object entityPlayer = CraftPlayer_getHandle.invoke(player);
					if(!LEGACY_PLAYER_DISGUISE_VIEWSELF) {
						Object PacketRemovePlayerInfo = PacketRemovePlayerInfo_new.newInstance(Arrays.asList(player.getUniqueId()));
						Object PacketUpdatePlayerInfo = PacketUpdatePlayerInfo_new.newInstance(UpdatePlayerInfo_ADD_PLAYER, entityPlayer);
						Object playerConnection = EntityPlayer_playerConnection.get(entityPlayer);
						PlayerConnection_sendPacket.invoke(playerConnection, PacketRemovePlayerInfo);
						PlayerConnection_sendPacket.invoke(playerConnection, PacketUpdatePlayerInfo);
					} else {
						Object entityPlayerArray = Array.newInstance(EntityPlayer, 1);
						Array.set(entityPlayerArray, 0, entityPlayer);
						Object PacketRemovePlayerInfo = PacketUpdatePlayerInfo_new.newInstance(UpdatePlayerInfo_REMOVE_PLAYER, entityPlayerArray);
						Object PacketUpdatePlayerInfo = PacketUpdatePlayerInfo_new.newInstance(UpdatePlayerInfo_ADD_PLAYER, entityPlayerArray);
						Object playerConnection = EntityPlayer_playerConnection.get(entityPlayer);
						PlayerConnection_sendPacket.invoke(playerConnection, PacketRemovePlayerInfo);
						PlayerConnection_sendPacket.invoke(playerConnection, PacketUpdatePlayerInfo);
					}

					Location originalLocation = player.getLocation();
					player.teleport(dummyWorld.getSpawnLocation());
					player.teleport(originalLocation);
				}
				
				return EntityType.PLAYER;
			} catch(IllegalAccessException|InvocationTargetException|IllegalStateException|InstantiationException e) {
				if(debugMode) getLogger().log(Level.SEVERE, "Unexpected failure!", e);
				return null;
			}
		} else {
			Entity entity = disguiseMap.remove(player.getUniqueId());
			entity.remove();
			for(Player observer : Bukkit.getOnlinePlayers()) {
				if(observer != player) {
					observer.showPlayer(player);
				}
			}
			return entity.getType();
		}
	}
	
	@EventHandler
	public void handlePlayerJoin(PlayerJoinEvent event) {
		for(Entry<UUID, Entity> entry : disguiseMap.entrySet()) {
			event.getPlayer().hidePlayer(Bukkit.getPlayer(entry.getKey()));
		}

		String targetSkin = event.getPlayer().getName().toLowerCase(Locale.ENGLISH);
		if(!profileDatabase.containsKey(targetSkin)) retrieveProfile(targetSkin, null);
	}
	
	@EventHandler
	public void handlePlayerQuit(PlayerQuitEvent event) {
		if(isDisguised(event.getPlayer())) {
			undisguise0(event.getPlayer());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void handlePlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(!event.isCancelled() && disguiseMap.containsKey(player.getUniqueId())) {
			Entity entity = disguiseMap.get(player.getUniqueId());
			if(entity.getType().name().equals("SHULKER")) {
				Location to = event.getTo();
				entity.teleport(new Location(to.getWorld(), to.getBlockX()+0.5, to.getBlockY()+0.0, to.getBlockZ()+0.5));
			} else {
				entity.teleport(event.getTo());
			}
			entity.setVelocity(player.getVelocity());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void handleEntityDamage(EntityDamageEvent event) {
		if(event.getEntity().hasMetadata("iDisguise")) {
			if(StringUtil.equals(event.getCause().name(), "FLY_INTO_WALL", "SUFFOCATION")) {
				event.setCancelled(true);
			} else {
				Bukkit.getPlayer((UUID)event.getEntity().getMetadata("iDisguise").get(0).value()).damage(event.getDamage());
				event.setDamage(Double.MIN_VALUE);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void handlePlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked().hasMetadata("iDisguise")) {
			event.setCancelled(true);
			Bukkit.getScheduler().runTaskLater(this, () -> {
				Bukkit.getPluginManager().callEvent(new PlayerInteractDisguisedPlayerEvent(
					event.getPlayer(),
					Bukkit.getPlayer((UUID)event.getRightClicked().getMetadata("iDisguise").get(0).value())
				));
			}, 1L);
		} else if(event.getRightClicked() instanceof Player && playerDisguiseMap.containsKey(((Player)event.getRightClicked()).getUniqueId())) {
			Bukkit.getScheduler().runTaskLater(this, () -> {
				Bukkit.getPluginManager().callEvent(new PlayerInteractDisguisedPlayerEvent(
					event.getPlayer(),
					(Player)event.getRightClicked()
				));
			}, 1L);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void handleEntityCombustLowest(EntityCombustEvent event) {
		if(event.getEntity().hasMetadata("iDisguise")) {
			if(!(event instanceof EntityCombustByBlockEvent || event instanceof EntityCombustByEntityEvent)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void handleEntityCombustMonitor(EntityCombustEvent event) {
		if(event.getEntity().hasMetadata("iDisguise")) {
			if(!event.isCancelled()) {
				Bukkit.getPlayer((UUID)event.getEntity().getMetadata("iDisguise").get(0).value()).setFireTicks(event.getDuration());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void handleEntityTarget(EntityTargetEvent event) {
		if(event.getTarget().hasMetadata("iDisguise")) {
			event.setCancelled(true);
		}
	}

	public Language getLanguage() {
		return language;
	}
	
	public String getVersion() {
		return getDescription().getVersion();
	}
	
	public String getNameAndVersion() {
		getFile();
		return getName() + " " + getVersion();
	}

	public File getFile() {
		return super.getFile();
	}
	
	public boolean debugMode() {
		return debugMode;
	}
	
	public static iDisguise getInstance() {
		return INSTANCE;
	}
	
}
