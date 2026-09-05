# CircuitJS1 桌面版 Mod

**Circuit Simulator 更名为 CircuitJS1 Desktop Mod**

![](screenshot.png)

这是 **Circuit Simulator** 的离线版源码，基于[修改版 NW.js](https://github.com/SEVA77/nw.js_mod)做了一些小改动。它最初由 Paul Falstad 以 Java Applet 的形式编写，由 Iain Sharp 移植为基于 GWT 的浏览器版本。该程序由 Usevalad Khatkevich 修改并编译为适用于 Windows (x32, x64)、Linux (x32, x64) 和 MacOS (x64, arm64) 的离线版本。

本程序由我作为教育用途的程序分发。不建议使用该程序对真实电路进行建模，因为程序中的许多元件都是理想化的。

该程序支持以下语言：英语、俄语、丹麦语、德语、波兰语、西班牙语、法语、意大利语、葡萄牙语、捷克语、挪威语、中文、日语。

Web 版应用程序参见：

Paul 的页面：https://www.falstad.com/circuit/ \
源码：https://github.com/pfalstad/circuitjs1

Iain 的页面：https://lushprojects.com/circuitjs/ \
源码：https://github.com/sharpie7/circuitjs1

## 下载：

你可以为 Windows (x32, x64)、Linux (x32, x64) 和 Mac OS X (x64, arm64) 下载此程序：
- [最新版本](https://github.com/SEVA77/circuitjs1/releases/latest)
- [所有版本](https://github.com/SEVA77/circuitjs1/releases)

> 如果你在使用此应用程序时遇到问题，可以尝试[主开发者的此离线应用程序](http://www.falstad.com/circuit/offline/)，它基于 Electron。

## 构建程序

构建该项目需要的工具：

* JDK 8+
* Maven 3+
* 带 npm 的 Node.js

在本地 `node_modules` 文件夹中安装依赖：
```
npm install
```

在不需要重建 GWT 应用的情况下，为所有平台进行完整构建：
```
npm run build
```

重建 GWT 应用的完整构建：
```
npm run full
```

*输出文件夹：* `./out/`

你也可以只构建 GWT 应用程序：
```
npm run buildgwt
```

并在 NW.js SDK 版本中运行它：
```
npm start
```

*输出文件夹：* `./target/site/`

## 开发

在开发菜单中有各种构建选项、检查器和 devmod：

```
npm run dev
```

***单独的命令：***

检查构建步骤：
```
npm run check
```

运行 devmode：
```
npm run devmode
```

Devmode 直接在 `war` 目录中工作，与 `target/site` 目录分开。

## 许可证

本程序是自由软件；你可以根据自由软件基金会发布的 GNU 通用公共许可证的条款（许可证第 2 版，或（由你选择）任何更高版本）重新分发和/或修改它。

分发本程序是希望它有用，但不提供任何保证；甚至没有对适销性或特定用途适用性的默示保证。参见 GNU 通用公共许可证了解更多详情。

你应该已经收到一份 GNU 通用公共许可证的副本，随本程序一同分发；如果没有，请写信给自由软件基金会，地址：51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA。

© Usevalad Khatkevich 2025

## 致谢

* [Paul Falstad](https://github.com/pfalstad) - 创作者
* [Iain Sharp](https://github.com/sharpie7) - JavaScript 转换，因此该应用程序有了更多发展机会。
* [Brian Gordon](https://github.com/briangordon) - circuitjs1 的 Maven 化版本