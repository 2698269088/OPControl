package top.mcocet.oPControl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia兼容的调度器工具类
 * 自动检测是否在Folia环境下运行，并使用相应的调度API
 */
public class FoliaScheduler {
    
    private static final boolean IS_FOLIA;
    
    static {
        // 检测是否为Folia环境
        boolean isFolia = false;
        
        try {
            // 尝试加载Folia特有的类
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            // 不是Folia环境
            isFolia = false;
        }
        
        IS_FOLIA = isFolia;
    }
    
    /**
     * 判断当前是否在Folia环境中运行
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }
    
    /**
     * 延迟执行任务
     * @param plugin 插件实例
     * @param runnable 要执行的任务
     * @param delayTicks 延迟 tick数
     */
    public static void runDelayed(Plugin plugin, Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            // Folia: 使用全局区域调度器
            try {
                Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (task) -> runnable.run(), delayTicks);
            } catch (Exception e) {
                // 如果失败，回退到标准API
                Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
            }
        } else {
            // 标准Bukkit/Paper
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }
    
    /**
     * 立即执行任务
     * @param plugin 插件实例
     * @param runnable 要执行的任务
     */
    public static void run(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            // Folia: 使用全局区域调度器
            try {
                Bukkit.getGlobalRegionScheduler().run(plugin, (task) -> runnable.run());
            } catch (Exception e) {
                // 如果失败，回退到标准API
                Bukkit.getScheduler().runTask(plugin, runnable);
            }
        } else {
            // 标准Bukkit/Paper
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }
    
    /**
     * 在实体所在的区域执行任务
     * @param plugin 插件实例
     * @param entity 实体
     * @param runnable 要执行的任务
     * @param delayTicks 延迟 tick数
     */
    public static void runDelayedForEntity(Plugin plugin, Entity entity, Runnable runnable, long delayTicks) {
        if (IS_FOLIA && entity != null) {
            try {
                // Folia: 使用实体调度器
                entity.getScheduler().runDelayed(plugin, (task) -> runnable.run(), null, delayTicks);
            } catch (Exception e) {
                // 如果失败，回退到全局调度器
                runDelayed(plugin, runnable, delayTicks);
            }
        } else {
            // 标准Bukkit/Paper
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }
    
    /**
     * 广播消息（线程安全）
     * @param plugin 插件实例
     * @param message 要广播的消息
     */
    public static void broadcastMessage(Plugin plugin, String message) {
        if (IS_FOLIA) {
            // Folia: 必须在全局区域执行
            try {
                Bukkit.getGlobalRegionScheduler().run(plugin, (task) -> Bukkit.getServer().broadcastMessage(message));
            } catch (Exception e) {
                // 如果失败，直接广播
                Bukkit.getServer().broadcastMessage(message);
            }
        } else {
            // 标准Bukkit/Paper
            Bukkit.getServer().broadcastMessage(message);
        }
    }
}
