package top.mcocet.oPControl;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;
    
    // 配置项常量
    private static final String INTERCEPT_COMMANDS_KEY = "settings.intercept-commands";
    private static final String OWNER_KEY = "server.owner";
    private static final String WHITELIST_KEY = "whitelist.players";
    private static final String BLACKLIST_KEY = "blacklist.players";
    private static final String BLOCKED_COMMANDS_KEY = "blocked.commands";

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // 设置默认值
        setDefaults();
    }

    /**
     * 设置默认配置值
     */
    private void setDefaults() {
        config.addDefault(INTERCEPT_COMMANDS_KEY, true);
        config.addDefault(OWNER_KEY, "");
        config.addDefault(WHITELIST_KEY, new ArrayList<>());
        config.addDefault(BLACKLIST_KEY, new ArrayList<>());
        config.addDefault(BLOCKED_COMMANDS_KEY, new ArrayList<>());
        config.options().copyDefaults(true);
        saveConfig();
    }

    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存配置文件: " + e.getMessage());
        }
    }

    /**
     * 重新加载配置文件
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
    }

    /**
     * 获取服主名称
     * @return 服主名称
     */
    public String getOwner() {
        return config.getString(OWNER_KEY, "");
    }

    /**
     * 设置服主名称
     * @param owner 服主名称
     */
    public void setOwner(String owner) {
        config.set(OWNER_KEY, owner);
        saveConfig();
    }

    /**
     * 获取白名单玩家列表
     * @return 白名单玩家列表
     */
    public List<String> getWhitelist() {
        return config.getStringList(WHITELIST_KEY);
    }

    /**
     * 添加玩家到白名单
     * @param player 玩家名称
     */
    public void addToWhitelist(String player) {
        List<String> whitelist = getWhitelist();
        if (!whitelist.contains(player)) {
            whitelist.add(player);
            config.set(WHITELIST_KEY, whitelist);
            saveConfig();
        }
    }

    /**
     * 从白名单中移除玩家
     * @param player 玩家名称
     */
    public void removeFromWhitelist(String player) {
        List<String> whitelist = getWhitelist();
        if (whitelist.remove(player)) {
            config.set(WHITELIST_KEY, whitelist);
            saveConfig();
        }
    }

    /**
     * 检查玩家是否在白名单中
     * @param player 玩家名称
     * @return 是否在白名单中
     */
    public boolean isInWhitelist(String player) {
        return getWhitelist().contains(player);
    }

    /**
     * 获取黑名单玩家列表
     * @return 黑名单玩家列表
     */
    public List<String> getBlacklist() {
        return config.getStringList(BLACKLIST_KEY);
    }

    /**
     * 添加玩家到黑名单
     * @param player 玩家名称
     */
    public void addToBlacklist(String player) {
        List<String> blacklist = getBlacklist();
        if (!blacklist.contains(player)) {
            blacklist.add(player);
            config.set(BLACKLIST_KEY, blacklist);
            saveConfig();
        }
    }

    /**
     * 从黑名单中移除玩家
     * @param player 玩家名称
     */
    public void removeFromBlacklist(String player) {
        List<String> blacklist = getBlacklist();
        if (blacklist.remove(player)) {
            config.set(BLACKLIST_KEY, blacklist);
            saveConfig();
        }
    }

    /**
     * 检查玩家是否在黑名单中
     * @param player 玩家名称
     * @return 是否在黑名单中
     */
    public boolean isInBlacklist(String player) {
        return getBlacklist().contains(player);
    }

    /**
     * 获取被阻止的命令列表
     * @return 被阻止的命令列表
     */
    public List<String> getBlockedCommands() {
        return config.getStringList(BLOCKED_COMMANDS_KEY);
    }

    /**
     * 添加命令到阻止列表
     * @param command 命令（不包含斜杠）
     */
    public void addBlockedCommand(String command) {
        List<String> blockedCommands = getBlockedCommands();
        if (!blockedCommands.contains(command)) {
            blockedCommands.add(command);
            config.set(BLOCKED_COMMANDS_KEY, blockedCommands);
            saveConfig();
        }
    }

    /**
     * 从阻止列表中移除命令
     * @param command 命令（不包含斜杠）
     */
    public void removeBlockedCommand(String command) {
        List<String> blockedCommands = getBlockedCommands();
        if (blockedCommands.remove(command)) {
            config.set(BLOCKED_COMMANDS_KEY, blockedCommands);
            saveConfig();
        }
    }

    /**
     * 检查命令是否被阻止
     * @param command 命令（不包含斜杠）
     * @return 是否被阻止
     */
    public boolean isCommandBlocked(String command) {
        return getBlockedCommands().contains(command);
    }

    /**
     * 获取原始配置文件对象
     * @return 配置文件对象
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * 检查是否启用命令拦截
     * @return 是否启用命令拦截
     */
    public boolean isInterceptCommandsEnabled() {
        return config.getBoolean(INTERCEPT_COMMANDS_KEY, true);
    }

    /**
     * 设置是否启用命令拦截
     * @param enabled 是否启用
     */
    public void setInterceptCommandsEnabled(boolean enabled) {
        config.set(INTERCEPT_COMMANDS_KEY, enabled);
        saveConfig();
    }
}
