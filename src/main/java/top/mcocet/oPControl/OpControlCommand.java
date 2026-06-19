package top.mcocet.oPControl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpControlCommand implements CommandExecutor {
    
    private final OPControl plugin;
    private final OpCommandHandler opCommandHandler;

    public OpControlCommand(OPControl plugin, OpCommandHandler opCommandHandler) {
        this.plugin = plugin;
        this.opCommandHandler = opCommandHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c[OPControl] 该命令只能由玩家执行！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // 显示帮助信息
            sendHelpMessage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "tp":
                handleTpSubCommand(player, args);
                break;
            case "op":
                handleOpSubCommand(player, args);
                break;
            case "deop":
                handleDeopSubCommand(player, args);
                break;
            case "stop":
                handleStopSubCommand(player, args);
                break;
                
            default:
                player.sendMessage("§c[OPControl] 未知的子命令！使用 /opcontrol 查看帮助");
                break;
        }
        
        return true;
    }

    /**
     * 处理 tp 子命令
     */
    private void handleTpSubCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c[OPControl] 用法: /opcontrol tp <accept|deny> <requestId>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (!action.equals("accept") && !action.equals("deny")) {
            player.sendMessage("§c[OPControl] 无效的操作！请使用 accept 或 deny");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage("§c[OPControl] 请提供请求ID！");
            return;
        }
        
        String requestId = args[2];
        boolean accepted = action.equals("accept");
        
        // 处理响应
        opCommandHandler.handleTpResponse(requestId, accepted, player);
    }

    /**
     * 处理 op 子命令
     */
    private void handleOpSubCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c[OPControl] 用法: /opcontrol op <accept|deny> <requestId>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (!action.equals("accept") && !action.equals("deny")) {
            player.sendMessage("§c[OPControl] 无效的操作！请使用 accept 或 deny");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage("§c[OPControl] 请提供请求ID！");
            return;
        }
        
        String requestId = args[2];
        boolean accepted = action.equals("accept");
        
        // 处理响应
        opCommandHandler.handleOpResponse(requestId, accepted, player, "op");
    }

    /**
     * 处理 deop 子命令
     */
    private void handleDeopSubCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c[OPControl] 用法: /opcontrol deop <accept|deny> <requestId>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (!action.equals("accept") && !action.equals("deny")) {
            player.sendMessage("§c[OPControl] 无效的操作！请使用 accept 或 deny");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage("§c[OPControl] 请提供请求ID！");
            return;
        }
        
        String requestId = args[2];
        boolean accepted = action.equals("accept");
        
        // 处理响应
        opCommandHandler.handleOpResponse(requestId, accepted, player, "deop");
    }

    /**
     * 处理 stop 子命令
     */
    private void handleStopSubCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c[OPControl] 用法: /opcontrol stop <accept|deny> <requestId>");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (!action.equals("accept") && !action.equals("deny")) {
            player.sendMessage("§c[OPControl] 无效的操作！请使用 accept 或 deny");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage("§c[OPControl] 请提供请求ID！");
            return;
        }
        
        String requestId = args[2];
        boolean accepted = action.equals("accept");
        
        // 处理响应
        opCommandHandler.handleStopResponse(requestId, accepted, player);
    }

    /**
     * 发送帮助信息
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage("§6§l========== [OPControl] 帮助 ==========");
        player.sendMessage("§e/opcontrol tp <accept|deny> <requestId> §7- 处理传送请求");
        player.sendMessage("§e/opcontrol op <accept|deny> <requestId> §7- 处理OP授予请求");
        player.sendMessage("§e/opcontrol deop <accept|deny> <requestId> §7- 处理OP取消请求");
        player.sendMessage("§e/opcontrol stop <accept|deny> <requestId> §7- 处理服务器停止请求");
        player.sendMessage("§6=======================================");
    }
}
