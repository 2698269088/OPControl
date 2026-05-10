package top.mcocet.oPControl;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandInterceptor implements Listener {
    
    private final OPControl plugin;
    private final ConfigManager configManager;
    private final OpCommandHandler opCommandHandler;

    public CommandInterceptor(OPControl plugin, OpCommandHandler opCommandHandler) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.opCommandHandler = opCommandHandler; // 使用传入的实例，而不是创建新的
    }

    /**
     * 拦截玩家命令执行事件
     * 优先级设置为最高，确保在其他插件之前处理
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // 检查是否启用命令拦截
        if (!configManager.isInterceptCommandsEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // 提取命令名称（去除斜杠和参数）
        String command = extractCommandName(message);
        
        // 排除 /opcontrol 命令，避免干扰插件自身的命令处理
        if (command.equalsIgnoreCase("opcontrol") || command.equalsIgnoreCase("opc")) {
            return; // 直接返回，允许执行
        }
        
        // 检查是否为黑名单玩家，如果是则不允许执行任何命令
        if (configManager.isInBlacklist(player.getName())) {
            event.setCancelled(true);
            player.sendMessage("§c[OPControl] 你已被列入黑名单，无法执行任何命令！");
            plugin.getLogger().info("黑名单玩家 " + player.getName() + " 尝试执行命令: " + message);
            return;
        }
        
        // 检查是否为服主或白名单玩家，如果是则不受任何限制
        if (isOwner(player) || configManager.isInWhitelist(player.getName())) {
            return; // 直接返回，允许执行任何命令
        }
        
        // 判断玩家类型并分别处理
        if (player.isOp()) {
            // 管理员（OP）执行命令 - 只处理需要OP权限的命令
            handleAdminCommand(player, command, event);
        } else {
            // 普通玩家执行命令 - 根据配置文件控制（目前留空）
            handlePlayerCommand(player, command, event);
        }
    }

    /**
     * 处理管理员执行的命令
     * 只拦截需要OP权限的命令
     */
    private void handleAdminCommand(Player player, String command, PlayerCommandPreprocessEvent event) {
        // 检查该命令是否需要OP权限
        if (requiresOpPermission(command)) {
            // 是需要OP权限的命令，检查是否在黑名单中
            if (configManager.isCommandBlocked(command)) {
                // 命令在黑名单中，拒绝执行
                event.setCancelled(true);
                player.sendMessage("§c[OPControl] 该命令已被禁止使用！");
                plugin.getLogger().info("管理员 " + player.getName() + " 尝试执行被禁止的命令: /" + command);
                return;
            }
            
            // 对于特定的敏感命令，需要进行额外检查
            handleSensitiveCommand(player, command, event.getMessage(), event);
        }
        // 如果命令不需要OP权限或不在黑名单中，允许正常执行
    }

    /**
     * 处理敏感命令（需要服主确认的命令）
     */
    private void handleSensitiveCommand(Player player, String command, String message, PlayerCommandPreprocessEvent event) {
        // 处理 tp 命令
        if (command.equalsIgnoreCase("tp") || command.equalsIgnoreCase("teleport")) {
            handleTpCommandForAdmin(player, message, event);
            return;
        }
        
        // 处理 op 命令
        if (command.equalsIgnoreCase("op")) {
            handleOpCommandForAdmin(player, message, event);
            return;
        }
        
        // 处理 deop 命令
        if (command.equalsIgnoreCase("deop")) {
            handleDeopCommandForAdmin(player, message, event);
            return;
        }
        
        // 处理 stop 命令
        if (command.equalsIgnoreCase("stop")) {
            handleStopCommandForAdmin(player, event);
            return;
        }
        
        // 其他需要OP权限的命令，直接允许执行
    }

    /**
     * 处理管理员的 tp 命令
     */
    private void handleTpCommandForAdmin(Player player, String message, PlayerCommandPreprocessEvent event) {
        String[] parts = message.split(" ");
        if (parts.length < 2) {
            return;
        }
        
        String targetName = parts[1];
        boolean allowed = opCommandHandler.handleTpCommand(player, targetName);
        
        if (!allowed) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理管理员的 op 命令
     */
    private void handleOpCommandForAdmin(Player player, String message, PlayerCommandPreprocessEvent event) {
        String[] parts = message.split(" ");
        if (parts.length < 2) {
            return;
        }
        
        String targetName = parts[1];
        boolean allowed = opCommandHandler.handleOpCommand(player, targetName);
        
        if (!allowed) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理管理员的 deop 命令
     */
    private void handleDeopCommandForAdmin(Player player, String message, PlayerCommandPreprocessEvent event) {
        String[] parts = message.split(" ");
        if (parts.length < 2) {
            return;
        }
        
        String targetName = parts[1];
        boolean allowed = opCommandHandler.handleDeopCommand(player, targetName);
        
        if (!allowed) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理管理员的 stop 命令
     */
    private void handleStopCommandForAdmin(Player player, PlayerCommandPreprocessEvent event) {
        boolean allowed = opCommandHandler.handleStopCommand(player);
        
        if (!allowed) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理普通玩家执行的命令
     * 根据配置文件控制（目前留空，后续扩展）
     */
    private void handlePlayerCommand(Player player, String command, PlayerCommandPreprocessEvent event) {
        // TODO: 后续根据配置文件实现对普通玩家命令的控制
        // 例如：
        // - 检查是否允许执行该命令
        // - 记录命令使用日志
        // - 限制特定命令的使用频率
        // - 等等...
        
        // 目前暂时允许普通玩家执行所有命令
        // 可以在这里添加额外的检查逻辑
    }

    /**
     * 从完整的命令消息中提取命令名称
     * 例如："/gamemode creative" -> "gamemode"
     * 
     * @param message 完整的命令消息
     * @return 命令名称（不包含斜杠和参数）
     */
    private String extractCommandName(String message) {
        // 去除开头的斜杠
        if (message.startsWith("/")) {
            message = message.substring(1);
        }
        
        // 提取第一个空格前的部分作为命令名
        int spaceIndex = message.indexOf(' ');
        if (spaceIndex != -1) {
            return message.substring(0, spaceIndex).toLowerCase();
        }
        
        return message.toLowerCase();
    }

    /**
     * 检查玩家是否为服主
     * @param player 玩家对象
     * @return 是否为服主
     */
    private boolean isOwner(Player player) {
        String owner = configManager.getOwner();
        return owner != null && !owner.isEmpty() && owner.equalsIgnoreCase(player.getName());
    }

    /**
     * 检查命令是否需要OP权限
     * @param command 命令名称
     * @return 是否需要OP权限
     */
    private boolean requiresOpPermission(String command) {
        // 获取命令对象
        PluginCommand pluginCommand = plugin.getServer().getPluginCommand(command);
        
        if (pluginCommand == null) {
            // 如果是 Bukkit 内置命令或未知命令，使用默认判断逻辑
            return isDefaultOpCommand(command);
        }
        
        // 检查命令的权限（getPermission返回的是String类型的权限名称）
        String permissionName = pluginCommand.getPermission();
        
        if (permissionName == null || permissionName.isEmpty()) {
            // 没有设置权限，通常意味着需要OP
            return true;
        }
        
        // 检查权限名称是否是 OP 相关的
        String permName = permissionName.toLowerCase();
        return permName.contains("op") || permName.contains("admin") || permName.contains("*");
    }

    /**
     * 判断是否为默认的OP命令
     * @param command 命令名称
     * @return 是否为OP命令
     */
    private boolean isDefaultOpCommand(String command) {
        // Bukkit/Paper 内置的需要OP权限的命令列表
        String[] opCommands = {
            "stop", "restart", "reload", "save-all", "save-off", "save-on",
            "op", "deop", "ban", "ban-ip", "pardon", "pardon-ip",
            "kick", "whitelist", "gamemode", "difficulty", "effect",
            "enchant", "experience", "xp", "give", "summon", "setblock",
            "fill", "clone", "tp", "teleport", "title", "bossbar",
            "scoreboard", "team", "advancement", "recipe", "datapack",
            "function", "publish", "debug", "spreadplayers",
            "plugman", "bukkit:plugman", "paper:plugman"
        };
        
        for (String opCmd : opCommands) {
            if (opCmd.equalsIgnoreCase(command)) {
                return true;
            }
        }
        
        return false;
    }
}
