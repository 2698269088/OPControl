package top.mcocet.oPControl;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OpCommandHandler implements Listener {
    
    private final OPControl plugin;
    private final ConfigManager configManager;
    
    // 存储待处理的请求 <请求ID, 请求信息>
    private final ConcurrentHashMap<String, Object> pendingRequests = new ConcurrentHashMap<>();

    public OpCommandHandler(OPControl plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    /**
     * 监听玩家传送事件
     * 注：此方法目前未使用，实际逻辑在命令拦截器中处理
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // 当前主要通过命令拦截器处理，此方法保留用于未来扩展
    }

    /**
     * 处理tp命令
     * @param executor 执行命令的玩家
     * @param targetName 目标玩家名称
     * @return 是否允许执行
     */
    public boolean handleTpCommand(Player executor, String targetName) {
        // 检查是否为服主或白名单玩家，如果是则不受限制
        if (isOwner(executor) || configManager.isInWhitelist(executor.getName())) {
            return true; // 允许执行
        }
        
        // 获取目标玩家
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            executor.sendMessage("§c[OPControl] 目标玩家不在线！");
            return false;
        }
        
        // 检查目标是否为服主
        if (isOwner(target)) {
            // 向服主发送确认请求
            sendTpRequestToOwner(executor, target);
            executor.sendMessage("§e[OPControl] 已向服主发送传送请求，请等待批准...");
            return false; // 暂时阻止，等待服主决定
        }
        
        // 其他情况允许执行（包括有权限的普通玩家）
        return true;
    }

    /**
     * 处理op命令（给予OP权限）
     * @param executor 执行命令的玩家
     * @param targetName 目标玩家名称
     * @return 是否允许执行
     */
    public boolean handleOpCommand(Player executor, String targetName) {
        // 检查是否为服主或白名单玩家
        if (isOwner(executor) || configManager.isInWhitelist(executor.getName())) {
            return true;
        }
        
        // 向服主发送确认请求
        sendOpRequestToOwner(executor, targetName);
        executor.sendMessage("§e[OPControl] 已向服主发送OP权限授予请求，请等待批准...");
        return false;
    }

    /**
     * 处理deop命令（取消OP权限）
     * @param executor 执行命令的玩家
     * @param targetName 目标玩家名称
     * @return 是否允许执行
     */
    public boolean handleDeopCommand(Player executor, String targetName) {
        // 检查是否为服主或白名单玩家
        if (isOwner(executor) || configManager.isInWhitelist(executor.getName())) {
            return true;
        }
        
        // 向服主发送确认请求
        sendDeopRequestToOwner(executor, targetName);
        executor.sendMessage("§e[OPControl] 已向服主发送OP权限取消请求，请等待批准...");
        return false;
    }

    /**
     * 处理stop命令（停止服务器）
     * @param executor 执行命令的玩家
     * @return 是否允许执行
     */
    public boolean handleStopCommand(Player executor) {
        // 检查是否为服主或白名单玩家
        if (isOwner(executor) || configManager.isInWhitelist(executor.getName())) {
            return true;
        }
        
        // 向服主发送确认请求
        sendStopRequestToOwner(executor);
        executor.sendMessage("§e[OPControl] 已向服主发送服务器停止请求，请等待批准...");
        return false;
    }

    /**
     * 向服主发送传送请求
     */
    private void sendTpRequestToOwner(Player executor, Player target) {
        String ownerName = configManager.getOwner();
        Player owner = Bukkit.getPlayer(ownerName);
        
        if (owner == null || !owner.isOnline()) {
            executor.sendMessage("§c[OPControl] 服主不在线，无法发送请求！");
            return;
        }
        
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // 保存请求信息
        TeleportRequest request = new TeleportRequest(requestId, executor.getName(), target.getName());
        Object previous = pendingRequests.put(requestId, request);
        
        if (previous != null) {
            plugin.getLogger().warning("[DEBUG] 警告：请求ID冲突！旧请求被覆盖 - ID: " + requestId);
        }
        
        // 调试日志
        plugin.getLogger().info("[DEBUG] 创建传送请求 - ID: " + requestId + ", 执行者: " + executor.getName() + ", 目标: " + target.getName());
        plugin.getLogger().info("[DEBUG] 当前待处理请求数: " + pendingRequests.size());
        plugin.getLogger().info("[DEBUG] 所有请求ID: " + pendingRequests.keySet());
        
        // 构建提示消息
        owner.sendMessage("§6§l========== [OPControl] 传送请求 ==========");
        owner.sendMessage("§e玩家 §b" + executor.getName() + " §e请求传送到 §b" + target.getName());
        owner.sendMessage("");
        
        // 创建接受按钮
        TextComponent acceptButton = new TextComponent("§a[✓ 允许]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/opcontrol tp accept " + requestId));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("点击允许 " + executor.getName() + " 传送到 " + target.getName()).create()));
        
        // 创建拒绝按钮
        TextComponent denyButton = new TextComponent("§c[✗ 拒绝]");
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/opcontrol tp deny " + requestId));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("点击拒绝 " + executor.getName() + " 的传送请求").create()));
        
        // 发送按钮
        owner.spigot().sendMessage(acceptButton, new TextComponent("  "), denyButton);
        owner.sendMessage("");
        owner.sendMessage("§6========================================");
        
        // 设置请求过期时间（60秒后自动过期）
        setRequestExpiration(requestId, executor, owner, "传送请求");
    }

    /**
     * 向服主发送OP权限授予请求
     */
    private void sendOpRequestToOwner(Player executor, String targetName) {
        Player owner = getOnlineOwner(executor);
        if (owner == null) return;
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        pendingRequests.put(requestId, new OpRequest(requestId, executor.getName(), targetName, RequestType.OP));
        
        owner.sendMessage("§6§l========== [OPControl] OP权限授予请求 ==========");
        owner.sendMessage("§e玩家 §b" + executor.getName() + " §e请求给予 §b" + targetName + " §eOP权限");
        owner.sendMessage("");
        
        TextComponent acceptButton = new TextComponent("§a[✓ 允许]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/opcontrol op accept " + requestId));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("点击允许给予 " + targetName + " OP权限").create()));
        
        TextComponent denyButton = new TextComponent("§c[✗ 拒绝]");
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/opcontrol op deny " + requestId));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("点击拒绝授予 " + targetName + " OP权限").create()));
        
        owner.spigot().sendMessage(acceptButton, new TextComponent("  "), denyButton);
        owner.sendMessage("");
        owner.sendMessage("§6================================================");
        
        setRequestExpiration(requestId, executor, owner, "OP权限授予请求");
    }

    /**
     * 向服主发送OP权限取消请求
     */
    private void sendDeopRequestToOwner(Player executor, String targetName) {
        Player owner = getOnlineOwner(executor);
        if (owner == null) return;
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        pendingRequests.put(requestId, new OpRequest(requestId, executor.getName(), targetName, RequestType.DEOP));
        
        owner.sendMessage("§6§l========== [OPControl] OP权限取消请求 ==========");
        owner.sendMessage("§e玩家 §b" + executor.getName() + " §e请求取消 §b" + targetName + " §e的OP权限");
        owner.sendMessage("");
        
        TextComponent acceptButton = new TextComponent("§a[✓ 允许]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/opcontrol deop accept " + requestId));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("点击允许取消 " + targetName + " 的OP权限").create()));
        
        TextComponent denyButton = new TextComponent("§c[✗ 拒绝]");
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/opcontrol deop deny " + requestId));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("点击拒绝取消 " + targetName + " 的OP权限").create()));
        
        owner.spigot().sendMessage(acceptButton, new TextComponent("  "), denyButton);
        owner.sendMessage("");
        owner.sendMessage("§6================================================");
        
        setRequestExpiration(requestId, executor, owner, "OP权限取消请求");
    }

    /**
     * 向服主发送服务器停止请求
     */
    private void sendStopRequestToOwner(Player executor) {
        Player owner = getOnlineOwner(executor);
        if (owner == null) return;
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        pendingRequests.put(requestId, new StopRequest(requestId, executor.getName()));
        
        owner.sendMessage("§6§l========== [OPControl] 服务器停止请求 ==========");
        owner.sendMessage("§e玩家 §b" + executor.getName() + " §e请求§c§l停止服务器");
        owner.sendMessage("§c⚠ 警告：此操作将关闭整个服务器！");
        owner.sendMessage("");
        
        TextComponent acceptButton = new TextComponent("§a[✓ 允许]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/opcontrol stop accept " + requestId));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("§c点击允许停止服务器").create()));
        
        TextComponent denyButton = new TextComponent("§c[✗ 拒绝]");
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/opcontrol stop deny " + requestId));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("点击拒绝停止服务器").create()));
        
        owner.spigot().sendMessage(acceptButton, new TextComponent("  "), denyButton);
        owner.sendMessage("");
        owner.sendMessage("§6=================================================");
        
        setRequestExpiration(requestId, executor, owner, "服务器停止请求");
    }

    /**
     * 处理服主的响应
     * @param requestId 请求ID
     * @param accepted 是否接受
     * @param responder 响应的玩家（应该是服主）
     */
    public void handleTpResponse(String requestId, boolean accepted, Player responder) {
        // 调试日志
        plugin.getLogger().info("[DEBUG] 处理传送响应 - ID: " + requestId + ", 接受: " + accepted);
        plugin.getLogger().info("[DEBUG] 当前待处理请求数: " + pendingRequests.size());
        plugin.getLogger().info("[DEBUG] 所有请求ID: " + pendingRequests.keySet());
        
        Object requestObj = pendingRequests.remove(requestId);
        
        if (requestObj == null) {
            plugin.getLogger().warning("[DEBUG] 请求不存在 - ID: " + requestId);
            responder.sendMessage("§c[OPControl] 请求不存在或已过期！");
            return;
        }
        
        if (!(requestObj instanceof TeleportRequest)) {
            plugin.getLogger().warning("[DEBUG] 请求类型不匹配 - 实际类型: " + requestObj.getClass().getName());
            responder.sendMessage("§c[OPControl] 请求不存在或已过期！");
            return;
        }
        
        TeleportRequest request = (TeleportRequest) requestObj;
        
        Player executor = Bukkit.getPlayer(request.getExecutorName());
        Player target = Bukkit.getPlayer(request.getTargetName());
        
        if (executor == null || !executor.isOnline()) {
            responder.sendMessage("§c[OPControl] 请求玩家已离线！");
            return;
        }
        
        if (accepted) {
            // 接受请求
            responder.sendMessage("§a[OPControl] 已允许 " + executor.getName() + " 传送到 " + 
                (target != null ? target.getName() : request.getTargetName()));
            executor.sendMessage("§a[OPControl] 服主已批准你的传送请求！");
            
            // 执行传送（Folia兼容）
            if (target != null && target.isOnline()) {
                // 使用Folia兼容的传送方式
                teleportPlayer(executor, target.getLocation(), target.getName());
            } else {
                executor.sendMessage("§c[OPControl] 目标玩家已离线，无法传送！");
            }
        } else {
            // 拒绝请求
            responder.sendMessage("§c[OPControl] 已拒绝 " + executor.getName() + " 的传送请求");
            executor.sendMessage("§c[OPControl] 服主拒绝了你的传送请求！");
        }
    }

    /**
     * 处理OP/DEOP请求的响应
     */
    public void handleOpResponse(String requestId, boolean accepted, Player responder, String command) {
        Object requestObj = pendingRequests.remove(requestId);
        
        if (requestObj == null || !(requestObj instanceof OpRequest)) {
            responder.sendMessage("§c[OPControl] 请求不存在或已过期！");
            return;
        }
        
        OpRequest request = (OpRequest) requestObj;
        
        Player executor = Bukkit.getPlayer(request.getExecutorName());
        Player target = Bukkit.getPlayer(request.getTargetName());
        
        if (target == null || !target.isOnline()) {
            responder.sendMessage("§c[OPControl] 目标玩家已离线！");
            if (executor != null && executor.isOnline()) {
                executor.sendMessage("§c[OPControl] 目标玩家已离线，操作取消！");
            }
            return;
        }
        
        if (accepted) {
            if (command.equalsIgnoreCase("op")) {
                target.setOp(true);
                responder.sendMessage("§a[OPControl] 已给予 " + target.getName() + " OP权限");
                if (executor != null && executor.isOnline()) {
                    executor.sendMessage("§a[OPControl] 服主已批准，" + target.getName() + " 已获得OP权限");
                }
                target.sendMessage("§a[OPControl] 你已被授予OP权限！");
            } else {
                target.setOp(false);
                responder.sendMessage("§a[OPControl] 已取消 " + target.getName() + " 的OP权限");
                if (executor != null && executor.isOnline()) {
                    executor.sendMessage("§a[OPControl] 服主已批准，" + target.getName() + " 的OP权限已被取消");
                }
                target.sendMessage("§c[OPControl] 你的OP权限已被取消");
            }
        } else {
            String action = command.equalsIgnoreCase("op") ? "授予" : "取消";
            responder.sendMessage("§c[OPControl] 已拒绝" + action + target.getName() + " 的OP权限");
            if (executor != null && executor.isOnline()) {
                executor.sendMessage("§c[OPControl] 服主拒绝了你的请求");
            }
        }
    }

    /**
     * 处理STOP请求的响应
     */
    public void handleStopResponse(String requestId, boolean accepted, Player responder) {
        Object requestObj = pendingRequests.remove(requestId);
        
        if (requestObj == null || !(requestObj instanceof StopRequest)) {
            responder.sendMessage("§c[OPControl] 请求不存在或已过期！");
            return;
        }
        
        StopRequest request = (StopRequest) requestObj;
        
        Player executor = Bukkit.getPlayer(request.getExecutorName());
        
        if (accepted) {
            responder.sendMessage("§a[OPControl] 服主已批准停止服务器，服务器将在3秒后关闭...");
            if (executor != null && executor.isOnline()) {
                executor.sendMessage("§a[OPControl] 服主已批准，服务器即将关闭");
            }
            
            // 广播消息（Folia兼容）
            FoliaScheduler.broadcastMessage(plugin, "§c§l================================");
            FoliaScheduler.broadcastMessage(plugin, "§c§l    服务器即将关闭");
            FoliaScheduler.broadcastMessage(plugin, "§c§l================================");
            
            // 3秒后停止服务器（Folia兼容）
            FoliaScheduler.runDelayed(plugin, () -> {
                Bukkit.shutdown();
            }, 60L);
        } else {
            responder.sendMessage("§c[OPControl] 已拒绝停止服务器");
            if (executor != null && executor.isOnline()) {
                executor.sendMessage("§c[OPControl] 服主拒绝了停止服务器的请求");
            }
        }
    }

    /**
     * Folia兼容的玩家传送方法
     * 在Folia环境下使用teleportAsync，在普通环境下使用同步teleport
     * @param player 要传送的玩家
     * @param location 目标位置
     * @param targetName 目标玩家名称（用于提示消息）
     */
    private void teleportPlayer(Player player, org.bukkit.Location location, String targetName) {
        if (FoliaScheduler.isFolia()) {
            // Folia: 使用 teleportAsync（通过反射调用，避免编译时依赖）
            try {
                java.lang.reflect.Method teleportAsyncMethod = Player.class.getMethod("teleportAsync", org.bukkit.Location.class);
                @SuppressWarnings("unchecked")
                java.util.concurrent.CompletableFuture<Boolean> future = 
                    (java.util.concurrent.CompletableFuture<Boolean>) teleportAsyncMethod.invoke(player, location);
                future.thenAccept(success -> {
                    FoliaScheduler.run(plugin, () -> {
                        if (success) {
                            player.sendMessage("§a已传送到 " + targetName);
                        } else {
                            player.sendMessage("§c[OPControl] 传送失败！");
                        }
                    });
                });
            } catch (Exception e) {
                // 反射失败，回退到同步传送
                boolean success = player.teleport(location);
                if (success) {
                    player.sendMessage("§a已传送到 " + targetName);
                } else {
                    player.sendMessage("§c[OPControl] 传送失败！");
                }
            }
        } else {
            // 普通Bukkit/Paper: 使用同步teleport
            boolean success = player.teleport(location);
            if (success) {
                player.sendMessage("§a已传送到 " + targetName);
            } else {
                player.sendMessage("§c[OPControl] 传送失败！");
            }
        }
    }

    /**
     * 检查玩家是否为服主
     */
    private boolean isOwner(Player player) {
        String owner = configManager.getOwner();
        return owner != null && !owner.isEmpty() && owner.equalsIgnoreCase(player.getName());
    }

    /**
     * 获取在线的服主
     */
    private Player getOnlineOwner(Player executor) {
        String ownerName = configManager.getOwner();
        Player owner = Bukkit.getPlayer(ownerName);
        
        if (owner == null || !owner.isOnline()) {
            executor.sendMessage("§c[OPControl] 服主不在线，无法发送请求！");
            return null;
        }
        
        return owner;
    }

    /**
     * 设置请求过期时间
     */
    private void setRequestExpiration(String requestId, Player executor, Player owner, String requestType) {
        FoliaScheduler.runDelayed(plugin, () -> {
            Object removed = pendingRequests.remove(requestId);
            if (removed != null) {
                plugin.getLogger().info("[DEBUG] 请求已过期 - ID: " + requestId + ", 类型: " + requestType);
                owner.sendMessage("§c[OPControl] " + requestType + "已过期！");
                if (executor.isOnline()) {
                    executor.sendMessage("§c[OPControl] " + requestType + "已过期，请稍后再试！");
                }
            } else {
                plugin.getLogger().info("[DEBUG] 请求已被处理或不存在 - ID: " + requestId);
            }
        }, 300L); // 15秒 = 300 ticks
    }

    /**
     * 传送请求数据类
     */
    private static class TeleportRequest {
        private final String requestId;
        private final String executorName;
        private final String targetName;

        public TeleportRequest(String requestId, String executorName, String targetName) {
            this.requestId = requestId;
            this.executorName = executorName;
            this.targetName = targetName;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getExecutorName() {
            return executorName;
        }

        public String getTargetName() {
            return targetName;
        }
    }

    /**
     * OP/DEOP请求数据类
     */
    private static class OpRequest {
        private final String requestId;
        private final String executorName;
        private final String targetName;
        private final RequestType type;

        public OpRequest(String requestId, String executorName, String targetName, RequestType type) {
            this.requestId = requestId;
            this.executorName = executorName;
            this.targetName = targetName;
            this.type = type;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getExecutorName() {
            return executorName;
        }

        public String getTargetName() {
            return targetName;
        }

        public RequestType getType() {
            return type;
        }
    }

    /**
     * STOP请求数据类
     */
    private static class StopRequest {
        private final String requestId;
        private final String executorName;

        public StopRequest(String requestId, String executorName) {
            this.requestId = requestId;
            this.executorName = executorName;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getExecutorName() {
            return executorName;
        }
    }

    /**
     * 请求类型枚举
     */
    private enum RequestType {
        OP, DEOP
    }
}
