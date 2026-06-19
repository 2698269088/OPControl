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
    private static final String ADMIN_PROTECTION_ENABLED_KEY = "admin-protection.enabled";
    private static final String ADMIN_PROTECTION_LIST_KEY = "admin-protection.protected-admins";
    private static final String ADMIN_PROTECTION_DURABILITY_KEY = "admin-protection.protect-durability";
    private static final String OWNER_KEY = "server.owner";
    private static final String WHITELIST_KEY = "whitelist.players";
    private static final String BLACKLIST_KEY = "blacklist.players";
    private static final String BLOCKED_COMMANDS_KEY = "blocked.commands";
    // 非OP玩家命令控制配置
    private static final String NON_OP_CONTROL_ENABLED_KEY = "non-op-command-control.enabled";
    private static final String NON_OP_CONTROL_MODE_KEY = "non-op-command-control.mode";
    private static final String NON_OP_WHITELIST_COMMANDS_KEY = "non-op-command-control.whitelist-commands";
    private static final String NON_OP_BLACKLIST_COMMANDS_KEY = "non-op-command-control.blacklist-commands";

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
        config.addDefault(ADMIN_PROTECTION_ENABLED_KEY, false);
        config.addDefault(ADMIN_PROTECTION_LIST_KEY, new ArrayList<>());
        config.addDefault(ADMIN_PROTECTION_DURABILITY_KEY, true);
        config.addDefault(OWNER_KEY, "");
        config.addDefault(WHITELIST_KEY, new ArrayList<>());
        config.addDefault(BLACKLIST_KEY, new ArrayList<>());
        config.addDefault(BLOCKED_COMMANDS_KEY, new ArrayList<>());
        // 非OP玩家命令控制默认值
        config.addDefault(NON_OP_CONTROL_ENABLED_KEY, false);
        config.addDefault(NON_OP_CONTROL_MODE_KEY, "whitelist");
        config.addDefault(NON_OP_WHITELIST_COMMANDS_KEY, new ArrayList<>());
        config.addDefault(NON_OP_BLACKLIST_COMMANDS_KEY, new ArrayList<>());
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

    /**
     * 检查是否启用管理员保护
     * @return 是否启用
     */
    public boolean isAdminProtectionEnabled() {
        return config.getBoolean(ADMIN_PROTECTION_ENABLED_KEY, false);
    }

    /**
     * 设置是否启用管理员保护
     * @param enabled 是否启用
     */
    public void setAdminProtectionEnabled(boolean enabled) {
        config.set(ADMIN_PROTECTION_ENABLED_KEY, enabled);
        saveConfig();
    }

    /**
     * 获取受保护的管理员名单
     * @return 管理员名单
     */
    public List<String> getProtectedAdmins() {
        return config.getStringList(ADMIN_PROTECTION_LIST_KEY);
    }

    /**
     * 检查玩家是否在保护名单中
     * @param playerName 玩家名称
     * @return 是否在保护名单中
     */
    public boolean isProtectedAdmin(String playerName) {
        return getProtectedAdmins().contains(playerName);
    }

    /**
     * 添加管理员到保护名单
     * @param playerName 玩家名称
     */
    public void addProtectedAdmin(String playerName) {
        List<String> admins = getProtectedAdmins();
        if (!admins.contains(playerName)) {
            admins.add(playerName);
            config.set(ADMIN_PROTECTION_LIST_KEY, admins);
            saveConfig();
        }
    }

    /**
     * 从保护名单中移除管理员
     * @param playerName 玩家名称
     */
    public void removeProtectedAdmin(String playerName) {
        List<String> admins = getProtectedAdmins();
        if (admins.remove(playerName)) {
            config.set(ADMIN_PROTECTION_LIST_KEY, admins);
            saveConfig();
        }
    }

    /**
     * 检查是否启用装备耐久保护
     * @return 是否启用
     */
    public boolean isDurabilityProtectionEnabled() {
        return config.getBoolean(ADMIN_PROTECTION_DURABILITY_KEY, true);
    }

    /**
     * 设置是否启用装备耐久保护
     * @param enabled 是否启用
     */
    public void setDurabilityProtectionEnabled(boolean enabled) {
        config.set(ADMIN_PROTECTION_DURABILITY_KEY, enabled);
        saveConfig();
    }

    // ==================== 非OP玩家命令控制配置 ====================

    /**
     * 检查是否启用非OP玩家命令控制
     * @return 是否启用
     */
    public boolean isNonOpCommandControlEnabled() {
        return config.getBoolean(NON_OP_CONTROL_ENABLED_KEY, false);
    }

    /**
     * 设置是否启用非OP玩家命令控制
     * @param enabled 是否启用
     */
    public void setNonOpCommandControlEnabled(boolean enabled) {
        config.set(NON_OP_CONTROL_ENABLED_KEY, enabled);
        saveConfig();
    }

    /**
     * 获取非OP玩家命令控制模式
     * @return "whitelist" 或 "blacklist"
     */
    public String getNonOpCommandControlMode() {
        return config.getString(NON_OP_CONTROL_MODE_KEY, "whitelist");
    }

    /**
     * 设置非OP玩家命令控制模式
     * @param mode "whitelist" 或 "blacklist"
     */
    public void setNonOpCommandControlMode(String mode) {
        if ("whitelist".equalsIgnoreCase(mode) || "blacklist".equalsIgnoreCase(mode)) {
            config.set(NON_OP_CONTROL_MODE_KEY, mode.toLowerCase());
            saveConfig();
        }
    }

    /**
     * 获取非OP玩家命令白名单列表
     * @return 白名单命令列表
     */
    public List<String> getNonOpWhitelistCommands() {
        return config.getStringList(NON_OP_WHITELIST_COMMANDS_KEY);
    }

    /**
     * 添加命令到非OP玩家白名单
     * @param command 命令（不包含斜杠）
     */
    public void addNonOpWhitelistCommand(String command) {
        List<String> whitelist = getNonOpWhitelistCommands();
        if (!whitelist.contains(command)) {
            whitelist.add(command);
            config.set(NON_OP_WHITELIST_COMMANDS_KEY, whitelist);
            saveConfig();
        }
    }

    /**
     * 从非OP玩家白名单中移除命令
     * @param command 命令（不包含斜杠）
     */
    public void removeNonOpWhitelistCommand(String command) {
        List<String> whitelist = getNonOpWhitelistCommands();
        if (whitelist.remove(command)) {
            config.set(NON_OP_WHITELIST_COMMANDS_KEY, whitelist);
            saveConfig();
        }
    }

    /**
     * 检查命令是否在非OP玩家白名单中
     * @param command 命令（不包含斜杠）
     * @return 是否在白名单中
     */
    public boolean isCommandInNonOpWhitelist(String command) {
        return getNonOpWhitelistCommands().contains(command.toLowerCase());
    }

    /**
     * 获取非OP玩家命令黑名单列表
     * @return 黑名单命令列表
     */
    public List<String> getNonOpBlacklistCommands() {
        return config.getStringList(NON_OP_BLACKLIST_COMMANDS_KEY);
    }

    /**
     * 添加命令到非OP玩家黑名单
     * @param command 命令（不包含斜杠）
     */
    public void addNonOpBlacklistCommand(String command) {
        List<String> blacklist = getNonOpBlacklistCommands();
        if (!blacklist.contains(command)) {
            blacklist.add(command);
            config.set(NON_OP_BLACKLIST_COMMANDS_KEY, blacklist);
            saveConfig();
        }
    }

    /**
     * 从非OP玩家黑名单中移除命令
     * @param command 命令（不包含斜杠）
     */
    public void removeNonOpBlacklistCommand(String command) {
        List<String> blacklist = getNonOpBlacklistCommands();
        if (blacklist.remove(command)) {
            config.set(NON_OP_BLACKLIST_COMMANDS_KEY, blacklist);
            saveConfig();
        }
    }

    /**
     * 检查命令是否在非OP玩家黑名单中
     * @param command 命令（不包含斜杠）
     * @return 是否在黑名单中
     */
    public boolean isCommandInNonOpBlacklist(String command) {
        return getNonOpBlacklistCommands().contains(command.toLowerCase());
    }
}