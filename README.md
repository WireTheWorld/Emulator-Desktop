<div align="center">
  <img src="./Logo.png" alt="logo" width="100" />
  <h2>CircuitJS1 桌面版</h2>
  <h3>基于 NW.js 的离线电子电路模拟器</h3>
</div>

<p align="center">🌐 <a href="./README.en.md">English</a> | <a href="./README.md">中文</a></p>

### 一、功能简介
- 离线桌面应用：无需浏览器与网络即可运行电路仿真
- 跨平台：支持 Windows (x32/x64)、Linux (x32/x64)、macOS (x64/arm64)
- 基于修改版 [NW.js](https://github.com/SEVA77/nw.js_mod)
- 多语言界面：支持 13 种语言
- 教育用途分发：元件均为理想化模型，不建议用于真实电路建模

### 二、快速开始
下载对应平台的二进制程序：
- [最新版本](https://github.com/SEVA77/circuitjs1/releases/latest)
- [所有版本](https://github.com/SEVA77/circuitjs1/releases)

> 如使用中遇到问题，可尝试主开发者基于 Electron 的[离线版本](http://www.falstad.com/circuit/offline/)。

### 三、构建
| 工具 | 版本 |
|------|------|
| JDK | 8+ |
| Maven | 3+ |
| Node.js | 带 npm |

```bash
# 安装依赖
npm install

# 不重建 GWT 应用，为所有平台完整构建（输出 ./out/）
npm run build

# 重建 GWT 应用的完整构建
npm run full

# 仅构建 GWT 应用（输出 ./target/site/）
npm run buildgwt

# 在 NW.js SDK 版本中运行
npm start

# 开发模式（含构建选项、检查器、devmod）
npm run dev
```

### 四、开发命令
| 命令 | 说明 |
|------|------|
| `npm run check` | 检查构建步骤 |
| `npm run devmode` | 运行 devmode（直接在 `war` 目录中工作，与 `target/site` 分开） |

### 五、支持语言
英语、俄语、丹麦语、德语、波兰语、西班牙语、法语、意大利语、葡萄牙语、捷克语、挪威语、中文、日语。

### 六、致谢
本项目基于上游仓库 [tyza66/Emulator-Desktop](https://github.com/tyza66/Emulator-Desktop) 进行本地化与改进，特此致谢。

### 七、许可证
本项目为自由软件，依据自由软件基金会发布的 GNU 通用公共许可证（GPL）第 2 版或（由你选择）任何更高版本分发。© Usevalad Khatkevich 2025