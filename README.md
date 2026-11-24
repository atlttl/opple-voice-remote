# 小欧遥控 · OPPLE 离线语音灯

把手机变成欧普吸顶灯的语音遥控器：界面照抄 OPPLE 实体遥控器的布局，每按一个键，手机扬声器就按官方离线语音模块的口令原样念出「小欧小欧 + 命令」，由灯具自身的离线模块完成识别。

- 不联网、不做语音识别、不采集数据
- 只调用系统 TTS（TextToSpeech）与音频输出
- 已签名 release APK，可直接侧载安装

## 安装

```
app/build/outputs/apk/release/app-release.apk   （约 1.0 MB）
```

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

最低 Android 7.0（API 24），arm64 / armeabi-v7a 通用（纯 Kotlin，无 native 依赖）。

## 使用

1. 手机外放口朝向灯具，距离 1–3 米。默认开启「自动提升媒体音量」，播放前把媒体音量提升到设定值（默认 100%，可在设置里按 10% 步进调整），播完还原原音量。
2. 界面默认只有一个灯键，唤醒词为「小欧小欧」。长按灯键即可改成这盏灯实际应答的唤醒词（未配对过是「小欧小欧」，配对过主卧/次卧则选对应项），按钮名称随之自动变更。
3. 家里有多盏灯时，点灯键行尾的「＋」增加一个灯键，再长按设置各自的唤醒词；最多 6 个。长按编辑器里可删除多余灯键。
4. 点按任意键即可。首次会播「唤醒词 → 停顿 → 命令」；随后的 15 秒内再点按只播命令，与灯具的 15 秒唤醒窗口一致。
5. 主题默认跟随手机的深色开关；需要固定深色或浅色时，到设置 → 外观里选。
6. 同一处「应用图标」可在两套图标间切换：Android（蓝调，默认）与 Apple（暖光），均为上传原图直接缩放，未做重绘。切换即时生效。

## 遥控器布局与功能映射

| 遥控器元素 | 应用内行为 |
| --- | --- |
| 圆形灯键行 | 默认 1 个，尾随「＋」按键可增加到 6 个；点按切换当前灯具，长按改该键唤醒词 |
| 状态行「清空」 | 立即结束 15 秒连续控制窗口，下一条命令重新播唤醒词 |
| 灯键名称 | 由唤醒词自动推导：小欧客厅灯→客厅，小欧主卧灯→主卧，小欧小欧→默认；重名自动编号 |
| 中央圆盘 亮度+ / 亮度- | 播「亮一点」/「暗一点」 |
| 中央圆盘 色温+ / 色温- | 播「白一点」/「黄一点」（色温升高＝更白） |
| 会客 / 娱乐 / 影院 / 夜灯 | 播「会客模式」等四组场景口令 |
| 开灯 / 关灯 长条键 | 播「开灯」/「关灯」 |
| 更多口令 | 最亮 / 最暗 / 白光 / 黄光 / 打开氛围光 / 关闭氛围光 / 延时关灯 / 安静模式 |
| 发送记录 | 最近 5 条口令，点按可重播 |

圆盘外圈细环是 15 秒连续控制窗口的倒计时；状态条显示语音引擎、中文语音是否就绪、是否正在外放。

## 口令表（逐字，勿改）

```
唤醒      小欧小欧
开关      开灯 / 关灯
亮度      亮一点 / 暗一点 / 最亮 / 最暗
色温      白一点 / 黄一点 / 白光 / 黄光
场景      会客模式 / 娱乐模式 / 影院模式 / 夜灯模式
氛围光    打开氛围光 / 关闭氛围光
延时      延时关灯（唤醒后说，5 分钟后熄灭）
静音      安静模式（上电 30 秒内说，之后不再有蜂鸣应答）
房间配对  设为客厅灯 / 设为餐厅灯 / 设为主卧灯 / 设为次卧灯 /
          设为书房灯 / 设为阳台灯 / 设为入户灯 / 设为厨房灯 / 设为卫生间灯
配对后唤醒词  小欧客厅灯 / 小欧餐厅灯 / 小欧主卧灯 / 小欧次卧灯 /
              小欧书房灯 / 小欧阳台灯 / 小欧入户灯 / 小欧厨房灯 / 小欧卫生间灯
复位      墙壁开关快速开关十次
```

## 技术规格

参照 FLClash 最新版的工程规范，逐项对齐如下：

| 项目 | FLClash 最新版 | 本应用 |
| --- | --- | --- |
| UI 框架 | Flutter + Material 3 | Kotlin + Jetpack Compose + Material 3 |
| JVM | Java 17 | Java 17 |
| minSdk | 24 | 24 |
| compileSdk / targetSdk | 37 / 37 | 35 / 35（本机可用的稳定 AGP 8.7.3 + SDK 35 组合） |
| 发布构建 | R8 混淆 + 资源压缩 | `isMinifyEnabled` + `isShrinkResources` |
| 签名 | `keystore.jks` + `local.properties` 存口令 | 同左（`app/keystore.jks`，口令见 `local.properties`） |
| debug 包名 | 追加 `.dev` | 追加 `.dev` |
| 可切换图标 | — | 两个 `activity-alias`，用 `setComponentEnabledSetting` 切换 |
| 构建工具 | Gradle Wrapper | Gradle 8.11.1 |

未采用 Flutter 的原因：本应用只需要 TTS 与一层静态界面，Flutter 引擎会额外引入 ~9 MB 原生库并增加一层平台通道，收益为零。其余可对齐的规范全部对齐。

## 工程结构

```
app/src/main/java/com/opple/voiceremote/
├── MainActivity.kt          入口、edge-to-edge、主题状态
├── data/Prefs.kt            设置持久化
├── ui/Theme.kt              深/浅两套调色板、全局常规字重
├── ui/Components.kt         灯具图标、圆盘、场景键、音量点、开关键
├── ui/RemoteScreen.kt       主界面与口令下发
├── ui/SettingsSheet.kt      语速、间隔、重复、唤醒词、房间配对
└── voice/
    ├── VoiceCatalog.kt      口令词表
    └── VoiceEngine.kt       TTS 排队、唤醒/停顿/命令、音量接管
```

## 交付物

```
dist/OppleVoiceRemote-1.0.8.apk   已签名安装包
dist/icon-warm.png                Apple 图标（暖光，第一张上传原图）
dist/icon-blue.png                Android 图标（蓝调，第二张上传原图，默认）
dist/preview-window.png           连续控制窗口与「清空」按钮
dist/preview-dark.png             深色界面（单灯 + 加号）
dist/preview-light.png            浅色界面（单灯 + 加号）
dist/preview-three-lamps.png      三个灯键的排布
dist/preview-editor.png           长按灯键后的唤醒词编辑器
```

`app/src/test` 下有一个 Robolectric 渲染测试：无需真机即可把深浅两套界面渲染成 PNG，并校验 MainActivity 能正常启动。改版式后跑 `./gradlew testDebugUnitTest`，看 `app/build/screenshots/` 即可。

## 构建

```bash
# 需要 JDK 17 与 Android SDK 35
./gradlew assembleRelease     # 产物 app/build/outputs/apk/release/
./gradlew installRelease      # 直接装到已连接设备
```

`local.properties` 中的 `sdk.dir` 指向本机 SDK；换机器时改这一行即可。签名口令写在 `local.properties` 的 `storePassword / keyAlias / keyPassword` 三项中，`app/keystore.jks` 与 `local.properties` 都未入库，请自行备份。缺少签名配置时，release 构建会退回 debug 签名并追加 `.dev` 包名后缀。

`settings.gradle.kts` 里优先使用阿里云镜像，海外网络可删除镜像两行。

## 识别率调优

- 无响应：把「命令重复」设为 2 次，语速调到 0.9 左右，间隔保持 700 ms。
- 唤醒成功但命令不执行：确认口令逐字一致（例如是「亮一点」不是「亮一点儿」）。
- 音色生硬：在系统语音设置里换一个中文音色；安卓原生 TTS 与讯飞、小爱、小艺的合成音色识别率差异明显。
- 手机接着耳机 / 蓝牙时不会外放，应用会在状态区提示。
- 「自动提升媒体音量」只往上提升，不压低你当前的音量；目标值设得比当前低时不会有任何动作。
- 灯键数量只影响界面，不改变语音协议；同一个唤醒词的两个灯键其实指向同一盏灯，只用于同一盏灯的两种命名习惯。
- 灯具已被设为「安静模式」时没有蜂鸣应答，属正常现象。
