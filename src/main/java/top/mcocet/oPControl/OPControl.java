package top.mcocet.oPControl;

import org.bukkit.plugin.java.JavaPlugin;

public final class OPControl extends JavaPlugin {

    private ConfigManager configManager;
    private CommandInterceptor commandInterceptor;
    private OpCommandHandler opCommandHandler;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 初始化OP命令处理器（只创建一个实例）
        opCommandHandler = new OpCommandHandler(this);
        
        // 注册命令拦截器（共享同一个 opCommandHandler 实例）
        commandInterceptor = new CommandInterceptor(this, opCommandHandler);
        getServer().getPluginManager().registerEvents(commandInterceptor, this);
        
        // 注册 /opcontrol 命令（共享同一个 opCommandHandler 实例）
        OpControlCommand opControlCommand = new OpControlCommand(this, opCommandHandler);
        getCommand("opcontrol").setExecutor(opControlCommand);
        
        getLogger().info("OPControl 插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("OPControl 插件已禁用！");
    }

    /**
     * 获取配置管理器实例
     * @return 配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
