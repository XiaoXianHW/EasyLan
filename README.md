# EasyLAN
**A Minecraft Forge Mod for Custom LAN Servers Related settings for customizing the LAN server (built-in server)**<br>
| [中文文档](https://github.com/XiaoXianHW/EasyLAN/blob/1.20.1/README_CN.md) |

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easylan)
- [Modrinth](https://modrinth.com/mod/easylan)
- [MC百科](https://www.mcmod.cn/class/11373.html)
- [Wiki](https://docs.axtn.net/docs/EasyLAN/)（writing..）

<br>

**-----------**<br>
**注意:**<br>
Mod并没有停止维护，只是最近比较忙，和一些精神上的问题暂时停更一段时间，我会收集关键性问题并在下面列出作为未来修复任务，如果有熟悉mod开发的可以合作，后期有打算制作Addon等功能，作者目前在治疗心理和精神疾病，对于那些提出问题但迟迟没有修复的感到很抱歉，会在回来后进行统一修复.<br>
<br>
###目前已知的问题:
- 局域网screen里的取消按钮无法点击
- UI和其他自定义UI mod冲突
- Public IPv4为Unknown（需要更换API接口）
- LanOutput功能的开放端口输出错误
- HttpAPI功能有时会造成客户端崩溃
- 需要Fabric版本
- 更新Forge版本至1.20.4
<br>
**如有其他问题欢迎在issue提出**<br>
**-----------**<br>

## Configurable List

### *Custom*

- Custom Port（**100-65535**）
- Custom Max Player（**2-500000**）
- Custom Motd（**100 Word Count**）

### *Server Basic Setting*

- Allow PVP（**True/False**）
- Online Mode（**True/False**）
- Spawn Animals（**True/False**）
- Spawn NPCs（**True/False**）
- Allow Flight（**True/False**）

### *Server Command Support*

- WhiteList（**/whitelist [on/off/add/remove/...]**）
- Banned（**/ban|/ban-ip | /pardon|/pardon-ip**）
- Operator（**/op | /deop**）
- SaveAll（**/save-all | /save-off | /save-on**）

### *Other*

- HttpAPI Info（HTTPApi Support | [Docs](https://docs.axtn.net/docs/EasyLan/HttpAPI)）<br>
  ***(There are compatibility issues with HttpAPI, which may cause a crash when exiting the game. For details, please refer to https://github.com/XiaoXianHW/EasyLAN/issues/2)***
- LAN output（Game Chat Output LAN Server Info）

<br>

**You can also configure this plugin through `.minecraft\config\easylan.cfg` (similar to server.properties)**

## Support Version

- 1.7.2 - 1.20.1 [Forge]<br>
  **Unsupport 1.13.2**
- Please delete the `.minecraft\config\easylan.cfg` file when updating from an old version of EasyLAN to a new version, otherwise the game may crash (see the update log for details)

<br>

## Translation Contribution

This MOD supports multiple languages;<br>
To contribute translations, please refer to and upload your language files to `src/main/resources/assets/easylan/lang`<br>
Contributed translations will be added in the **next Minecraft version** (or full version if it's a major refactoring update)

- 1.7.2 - 1.12.2 (xx_XX.lang; **eg: zh_CN.lang**)
- 1.14.4 - 1.20.1 (xx_xx.json; **eg: zh_cn.json**)

<br>

## Developers
Build
```
git clone https://github.com/XiaoXianHW/EasyLAN.git
./gradlew build
```

For IntelliJ IDEA
```
./gradlew genIntellijRuns
```

For Eclipse
```
./gradlew genEclipseRuns
```

**If you are using `runClient` in the IDEA or Eclipse compilation environment, you need to set the `DevMode` boolean value in `EasyLAN.java` to `true`, otherwise it cannot be executed normally**
