<div align="center">
  <img src="./Logo.png" alt="logo" width="100" />
  <h2>CircuitJS1 Desktop</h2>
  <h3>Offline electronic circuit simulator based on NW.js</h3>
</div>

<p align="center">🌐 <a href="./README.en.md">English</a> | <a href="./README.md">中文</a></p>

### 1. Features
- Offline desktop app: run circuit simulations without a browser or network
- Cross-platform: Windows (x32/x64), Linux (x32/x64), macOS (x64/arm64)
- Based on a [modified NW.js](https://github.com/SEVA77/nw.js_mod)
- Multi-language UI: 13 languages supported
- Distributed for educational purposes: all components are idealized models; not recommended for modeling real circuits

### 2. Quick Start
Download the binaries for your platform:
- [Latest release](https://github.com/SEVA77/circuitjs1/releases/latest)
- [All releases](https://github.com/SEVA77/circuitjs1/releases)

> If you run into issues with this app, you can try the [offline version](http://www.falstad.com/circuit/offline/) by the main developer, based on Electron.

### 3. Building
| Tool | Version |
|------|---------|
| JDK | 8+ |
| Maven | 3+ |
| Node.js | with npm |

```bash
# Install dependencies
npm install

# Full build for all platforms without rebuilding the GWT app (outputs to ./out/)
npm run build

# Full build including rebuilding the GWT app
npm run full

# Build only the GWT app (outputs to ./target/site/)
npm run buildgwt

# Run in the NW.js SDK version
npm start

# Development mode (build options, checkers, devmod)
npm run dev
```

### 4. Development Commands
| Command | Description |
|---------|-------------|
| `npm run check` | Check build steps |
| `npm run devmode` | Run devmode (works directly in the `war` directory, separate from `target/site`) |

### 5. Supported Languages
English, Russian, Danish, German, Polish, Spanish, French, Italian, Portuguese, Czech, Norwegian, Chinese, Japanese.

### 6. Acknowledgements
This project is localized and improved on top of the upstream repository [tyza66/Emulator-Desktop](https://github.com/tyza66/Emulator-Desktop).

### 7. License
This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version. © Usevalad Khatkevich 2025