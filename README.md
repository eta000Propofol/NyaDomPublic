# NyaDomPublic

> Paper 26.1.2 下的 Dominion 领地「宣传栏」扩展插件

NyaDomPublic 是 [Dominion](https://github.com/LunaDeerMC/Dominion) 领地的宣传栏扩展。玩家通过 `/board` 打开 6×9 箱子 GUI，将自己在 Dominion 中的领地上架到全服宣传栏，其他玩家点击条目即可传送到对应领地；领主/管理员可管理自己条目的简介与下架。

## 功能特性

- **箱子 GUI**：6×9 双排箱子界面，上方 45 格展示全服宣传条目，底部 9 格固定操作按钮。
- **随机告示牌**：上架时随机选择一种告示牌材质（橡木、云杉、白桦、丛林、金合欢、深色橡木、红树、樱花、竹、绯红、诡异、苍白橡木）。
- **Vault 经济**：上架消耗龙门币，默认 `1000`，可在 `config.yml` 修改。
- **领地简介**：支持在管理界面通过聊天输入修改简介，显示在条目 lore 上。
- **权限联动**：点击条目传送前会检查 Dominion 的 `TELEPORT` 传送权限，领地未开启传送权限时无法通过宣传栏传送。
- **全服上限**：默认最多上架 `45` 条，同一领地不可重复上架。
- **自动清理**：领地被删除或宣传过期时自动移除对应条目；支持按天数设置上架时长，默认永久。

## 环境依赖

| 依赖 | 说明 |
| --- | --- |
| Paper 26.1.2+ | 服务端（Java 25） |
| Dominion | 领地插件，提供 DominionAPI |
| Vault | 经济接口，需要已注册的经济实现（如 CMI、EssentialsX 等） |

缺少 Dominion 或 Vault 时插件会拒绝加载。

## 安装

1. 将 `NyaDomPublic-1.0.0.jar` 放入服务端 `plugins/` 目录。
2. 确认 `plugins/` 下同时存在 `Dominion` 与 `Vault` 及其经济实现插件。
3. 重启服务端，插件会在 `plugins/NyaDomPublic/` 下生成 `config.yml`、`messages.yml` 与 `data.yml`。

## 命令

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/board` | 打开宣传栏 GUI（别名 `/nyadompublic`、`/dominionboard`） | `nyadompublic.use` |
| `/board reload` | 重载配置、文案与宣传数据 | `nyadompublic.admin` |

## 权限

| 权限节点 | 默认值 | 说明 |
| --- | --- | --- |
| `nyadompublic.use` | `true` | 打开宣传栏、上架/管理有权限的领地 |
| `nyadompublic.admin` | `op` | 执行 `/board reload` |

## 配置

`plugins/NyaDomPublic/config.yml`：

```yaml
economy:
  cost: 1000.0          # 上架费用
  currency-name: '龙门币'

listing:
  duration-days: -1     # 上架时长（天），-1 表示永久
  max: 45               # 全服宣传上限（GUI 固定 45 格，超过自动钳制）
  description-max-length: 100
  sign-materials:       # 随机告示牌材质池
    - OAK_SIGN
    - SPRUCE_SIGN
    # ...
```

`messages.yml` 提供全部玩家提示文案，支持 `&` 颜色代码；`data.yml` 为宣传数据，无需手动编辑。

## 构建

```bash
# Windows
./gradlew.bat build

# Linux/macOS
./gradlew build
```

构建产物位于 `build/libs/NyaDomPublic-1.0.0.jar`。

## 目录结构

```
src/main/java/com/nyadom/nyadompublic
├── command/      # 指令
├── config/       # 配置读取
├── data/         # 宣传条目数据
├── dominion/     # Dominion API 封装
├── economy/      # Vault 经济封装
├── gui/          # 箱子 GUI
└── listener/     # 事件监听
```

## 说明

- 宣传栏仅通过 `/board` 指令打开，不放置任何世界内实体/方块。
- 只有 Dominion 领地所有者与管理员可以上架/管理对应领地。
- 下架不退款；传送本身不额外收费。
