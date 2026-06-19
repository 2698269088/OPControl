package top.mcocet.oPControl;

import org.bukkit.plugin.java.JavaPlugin;

public final class OPControl extends JavaPlugin {

    private ConfigManager configManager;
    private CommandInterceptor commandInterceptor;
    private OpCommandHandler opCommandHandler;
    private AdminProtectionListener adminProtectionListener;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 初始化OP命令处理器（只创建一个实例）
        opCommandHandler = new OpCommandHandler(this);
        
        // 注册命令拦截器（共享同一个 opCommandHandler 实例）
        commandInterceptor = new CommandInterceptor(this, opCommandHandler);
        getServer().getPluginManager().registerEvents(commandInterceptor, this);
        
        // 注册管理员保护监听器
        adminProtectionListener = new AdminProtectionListener(this);
        getServer().getPluginManager().registerEvents(adminProtectionListener, this);
        
        // 注册 /opcontrol 命令（共享同一个 opCommandHandler 实例）
        OpControlCommand opControlCommand = new OpControlCommand(this, opCommandHandler);
        getCommand("opcontrol").setExecutor(opControlCommand);
        
        getLogger().info("OPControl 插件已启用！");
        
        // 检查管理员保护功能状态
        if (configManager.isAdminProtectionEnabled()) {
            getLogger().info("管理员保护功能已启用，受保护的管理员: " + configManager.getProtectedAdmins());
        } else {
            getLogger().info("管理员保护功能未启用");
        }
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
