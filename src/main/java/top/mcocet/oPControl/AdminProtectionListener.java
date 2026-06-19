package top.mcocet.oPControl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

/**
 * 管理员保护事件监听器
 * 保护指定管理员免受伤害和死亡，但保留伤害动画
 * 可选：保护装备耐久不消耗
 */
public class AdminProtectionListener implements Listener {
    
    private final OPControl plugin;
    private final ConfigManager configManager;

    public AdminProtectionListener(OPControl plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    /**
     * 处理玩家受伤事件
     * 如果玩家是受保护的管理员，则取消伤害但保留动画
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        // 检查是否启用管理员保护
        if (!configManager.isAdminProtectionEnabled()) {
            return;
        }
        
        // 只处理玩家
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // 检查是否为受保护的管理员
        if (isProtectedAdmin(player)) {
            // 设置伤害为0，保留动画效果（参考 SpawnPointProtection 的做法）
            event.setDamage(0.0);
            
            // 播放受伤声音（根据伤害类型）
            playDamageSound(player, event.getCause());
        }
    }

    /**
     * 处理玩家死亡事件
     * 如果玩家是受保护的管理员，则阻止死亡
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // 检查是否启用管理员保护
        if (!configManager.isAdminProtectionEnabled()) {
            return;
        }
        
        Player player = event.getEntity();
        
        // 检查是否为受保护的管理员
        if (isProtectedAdmin(player)) {
            // 取消死亡事件
            event.setCancelled(true);
            
            // 设置生命值为满血
            player.setHealth(20.0);
            
            // 清除负面效果
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            
            // 发送提示消息
            player.sendMessage("§a[OPControl] §e你受到管理员保护，已免疫此次死亡！");
            
            // 记录日志
            plugin.getLogger().info("受保护的管理员 " + player.getName() + " 免疫了死亡");
        }
    }

    /**
     * 播放受伤声音（不触发动画，因为 setDamage(0.0) 会自动显示动画）
     */
    private void playDamageSound(Player player, EntityDamageEvent.DamageCause cause) {
        // 根据伤害类型播放不同的声音
        switch (cause) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                player.playSound(player.getLocation(), "entity.player.hurt", 1.0f, 1.0f);
                break;
            case FALL:
                player.playSound(player.getLocation(), "entity.player.hurt.fall", 1.0f, 1.0f);
                break;
            case FIRE:
            case FIRE_TICK:
                player.playSound(player.getLocation(), "entity.player.hurt.on_fire", 1.0f, 1.0f);
                break;
            case LAVA:
                player.playSound(player.getLocation(), "block.lava.pop", 1.0f, 1.0f);
                break;
            case DROWNING:
                player.playSound(player.getLocation(), "entity.player.hurt.drown", 1.0f, 1.0f);
                break;
            default:
                player.playSound(player.getLocation(), "entity.player.hurt", 1.0f, 1.0f);
                break;
        }
    }

    /**
     * 检查玩家是否为受保护的管理员
     * 条件：1. 是OP 2. 在保护名单中
     */
    private boolean isProtectedAdmin(Player player) {
        return player.isOp() && configManager.isProtectedAdmin(player.getName());
    }

    /**
     * 处理物品耐久损耗事件
     * 如果玩家是受保护的管理员且启用了耐久保护，则取消耐久消耗
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent event) {
        // 检查是否启用管理员保护
        if (!configManager.isAdminProtectionEnabled()) {
            return;
        }
        
        // 检查是否启用耐久保护
        if (!configManager.isDurabilityProtectionEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // 检查是否为受保护的管理员
        if (isProtectedAdmin(player)) {
            // 取消耐久消耗
            event.setCancelled(true);
        }
    }
}
