# OPControl - Minecraft 服务器管理员权限控制插件

[![Version](https://img.shields.io/badge/version-1.0-blue)]()
[![Minecraft](https://img.shields.io/badge/minecraft-1.21+-green)]()
[![Platform](https://img.shields.io/badge/platform-Paper%20%7C%20Folia-yellow)]()

## 📖 项目简介

OPControl 是一个专为 Minecraft 服务器设计的权限控制插件，用于防止因管理员账号被盗或其他原因导致的服务器设置被恶意修改。通过分层权限管理和敏感操作二次确认机制，确保服务器安全。

### ✨ 核心特性

- 🔐 **分层权限控制** - 服主、白名单、管理员、普通玩家、黑名单五级权限体系
- ⚠️ **敏感操作确认** - op/deop/stop 等命令需要服主二次确认
- 🚫 **黑名单系统** - 黑名单玩家无法执行任何命令
- ✅ **白名单豁免** - 白名单玩家不受任何限制
- 🖱️ **点击交互界面** - 美观的聊天栏按钮，一键批准/拒绝
- 🌐 **Folia 完全兼容** - 支持 Paper 和 Folia 服务端
- ⏱️ **请求超时机制** - 15秒自动过期，防止请求堆积

---

## 🚀 快速开始

### 安装要求

- **Java**: 21 或更高版本
- **Minecraft**: 1.21+
- **服务端**: Paper 1.21+ 或 Folia 1.20+

### 安装步骤

1. 下载最新的 `OPControl.jar` 文件
2. 将插件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 编辑 `plugins/OPControl/config.yml` 配置文件
5. 重新加载配置或重启服务器

---

## ⚙️ 配置说明

### config.yml

```yaml
# 是否启用命令拦截
settings:
  intercept-commands: true

# 服务器服主名称（完全不受限制）
server:
  owner: "YourServerOwnerName"

# 白名单玩家列表（不受任何命令限制）
# 注意：某些修改版服务端可能将控制台识别为CONSOLE玩家
whitelist:
  players:
    - "CONSOLE"
    - "TrustedAdmin1"
    - "TrustedAdmin2"

# 黑名单玩家列表（不允许执行任何命令）
blacklist:
  players:
    - "BadPlayer1"
    - "BadPlayer2"

# 被阻止的命令列表（不包含斜杠）
# 仅对非服主/非白名单的管理员生效
blocked:
  commands:
    - "stop"
    - "plugman"
    - "op"
    - "deop"
```

---

## 🎮 使用说明

### 权限层级

```
┌─────────────────────────────────────────┐
│         权限层级（从高到低）              │
├─────────────────────────────────────────┤
│ 1. 服主 (owner)                          │
│    └─ 完全不受限制，可执行任何命令        │
├─────────────────────────────────────────┤
│ 2. 白名单玩家 (whitelist)                │
│    └─ 完全不受限制，可执行任何命令        │
├─────────────────────────────────────────┤
│ 3. 管理员 (OP)                           │
│    ├─ 可执行所有不需要OP权限的命令        │
│    ├─ 执行黑名单命令 → ❌ 被阻止         │
│    └─ 执行敏感命令 → ⚠️ 需服主确认       │
├─────────────────────────────────────────┤
│ 4. 普通玩家                              │
│    └─ 根据配置控制（目前留空）            │
├─────────────────────────────────────────┤
│ 5. 黑名单玩家                            │
│    └─ ❌ 无法执行任何命令                │
└─────────────────────────────────────────┘
```

### 敏感命令列表

以下命令在执行时需要服主二次确认（针对非服主/非白名单的管理员）：

| 命令 | 说明 | 风险等级 |
|------|------|---------|
| `/tp <玩家>` | 传送到服主 | ⚠️ 中 |
| `/op <玩家>` | 给予OP权限 | 🔴 高 |
| `/deop <玩家>` | 取消OP权限 | 🔴 高 |
| `/stop` | 停止服务器 | 🔴 极高 |

### 操作流程示例

#### 场景1：管理员尝试传送到服主

```
1. 管理员 EnuFume 执行: /tp MCOCET
2. 系统检测到目标为服主
3. 向服主 MCOCET 发送确认请求：
   ========== [OPControl] 传送请求 ==========
   玩家 EnuFume 请求传送到 MCOCET
   
   [✓ 允许]  [✗ 拒绝]
   
   ========================================
4. 服主点击按钮
   - 点击 [✓ 允许] → 执行传送
   - 点击 [✗ 拒绝] → 拒绝请求
5. 15秒内未响应 → 请求自动过期
```

#### 场景2：管理员尝试给予OP权限

```
1. 管理员执行: /op NewPlayer
2. 向服主发送确认请求
3. 服主批准后，NewPlayer 获得OP权限
4. 相关玩家收到通知消息
```

#### 场景3：黑名单玩家尝试执行命令

```
1. 黑名单玩家 BadPlayer 执行: /spawn
2. 系统立即阻止
3. 显示消息: "你已被列入黑名单，无法执行任何命令！"
4. 控制台记录日志
```

---

## 🛠️ 命令参考

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/tp <玩家>` | OP | 传送到指定玩家（到服主需确认） |
| `/op <玩家>` | OP | 给予OP权限（需服主确认） |
| `/deop <玩家>` | OP | 取消OP权限（需服主确认） |
| `/stop` | OP | 停止服务器（需服主确认） |

### 服主管理命令

| 命令 | 说明 |
|------|------|
| `/opcontrol` | 显示帮助信息 |
| `/opcontrol tp <accept\|deny> <requestId>` | 处理传送请求 |
| `/opcontrol op <accept\|deny> <requestId>` | 处理OP授予请求 |
| `/opcontrol deop <accept\|deny> <requestId>` | 处理OP取消请求 |
| `/opcontrol stop <accept\|deny> <requestId>` | 处理服务器停止请求 |

**别名**: `/opc`

---

## 🔧 技术细节

### Folia 兼容性

OPControl 完全兼容 Folia 服务端的区域化多线程架构：

- ✅ 使用 `ConcurrentHashMap` 确保线程安全
- ✅ 使用 `GlobalRegionScheduler` 进行任务调度
- ✅ 使用 `teleportAsync()` 进行异步传送
- ✅ 自动检测运行环境并适配 API

### 架构设计

```
OPControl (主类)
├── ConfigManager (配置管理)
│   ├── 加载/保存 config.yml
│   ├── 管理服主、白名单、黑名单
│   └── 管理被阻止的命令列表
│
├── CommandInterceptor (命令拦截器)
│   ├── 监听 PlayerCommandPreprocessEvent
│   ├── 黑名单检查
│   ├── 服主/白名单豁免
│   ├── 管理员命令过滤
│   └── 普通玩家控制（预留）
│
├── OpCommandHandler (OP命令处理器)
│   ├── 处理 tp/op/deop/stop 命令
│   ├── 生成和管理请求
│   ├── 发送确认消息给服主
│   └── 处理服主的响应
│
├── OpControlCommand (管理命令)
│   ├── 处理 /opcontrol 命令
│   └── 调用 OpCommandHandler 处理响应
│
└── FoliaScheduler (调度器工具)
    ├── 自动检测 Folia 环境
    ├── 提供统一的调度 API
    └── 线程安全的任务执行
```

---

## 📝 开发指南

### 编译项目

```bash
# 克隆仓库
git clone https://github.com/yourusername/OPControl.git

# 进入目录
cd OPControl

# 使用 Maven 编译
mvn clean package

# 生成的 jar 文件位于 target/OPControl-1.0.jar
```

### 添加新的敏感命令

1. 在 `OpCommandHandler.java` 中添加处理方法：
```java
public boolean handleXXXCommand(Player executor, String params) {
    if (isOwner(executor) || configManager.isInWhitelist(executor.getName())) {
        return true;
    }
    // 发送确认请求逻辑
    return false;
}
```

2. 在 `CommandInterceptor.java` 的 `handleSensitiveCommand()` 中添加判断：
```java
if (command.equalsIgnoreCase("xxx")) {
    handleXXXCommandForAdmin(player, message, event);
    return;
}
```

3. 在 `OpControlCommand.java` 中添加子命令处理

---

## 🐛 常见问题

### Q: 服主点击按钮后提示"请求不存在或已过期"

**A**: 确保：
1. 服主用户名正确配置在 `server.owner` 中
2. 使用的是最新版本（已修复实例共享问题）
3. 完全重启服务器（不要使用 plugman reload）

### Q: 传送时出现 "Must use teleportAsync" 错误

**A**: 这是 Folia 的要求，确保使用最新版本（已修复）

### Q: 如何让控制台也能使用命令？

**A**: 在配置文件的白名单中添加 `CONSOLE`：
```yaml
whitelist:
  players:
    - "CONSOLE"
```

### Q: 如何修改请求超时时间？

**A**: 当前固定为 15 秒，如需修改可在 `OpCommandHandler.java` 的 `setRequestExpiration()` 方法中调整 tick 数（300L = 15秒）

---

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📧 联系方式

- **作者**: MCOCET
- **网站**: https://home.mcocet.top
- **问题反馈**: [GitHub Issues](https://github.com/yourusername/OPControl/issues)

---

## 🙏 致谢

感谢以下项目和社区的支持：

- [PaperMC](https://papermc.io/) - 高性能 Minecraft 服务端
- [Folia](https://papermc.io/software/folia) - 区域化多线程服务端
- [Bukkit/Spigot](https://www.spigotmc.org/) - Minecraft 插件 API

---

**Made with ❤️ by MCOCET**
