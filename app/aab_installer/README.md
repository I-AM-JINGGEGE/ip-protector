# AAB 转 APK 工具

这个工具可以将 Android App Bundle (AAB) 文件转换为 APK 文件并安装到设备上。

## 文件说明

- `bundletool.jar` - Google 官方的 AAB 处理工具
- `keystore.jks` - 签名密钥库文件 (与项目根目录一致)
- `keystore.pwd` - 密钥库密码文件 (密码: 0099oopp,.mnjkl;)
- `key.pwd` - 密钥密码文件 (密码: 0099oopp,.mnjkl;)
- `test.bash` - 简化的安装脚本
- `aab_to_apk.sh` - 功能完整的转换工具

## 使用方法

### 方法1: 使用简化脚本 (test.bash)

```bash
# 基本用法
./test.bash app-release.aab

# 指定设备
./test.bash app-release.aab emulator-5554
```

### 方法2: 使用完整工具 (aab_to_apk.sh)

#### 生成通用 APK
```bash
# 生成通用 APK (包含所有架构)
./aab_to_apk.sh app-release.aab

# 指定输出目录
./aab_to_apk.sh -o ./apk_output app-release.aab
```

#### 生成分屏 APK
```bash
# 生成分屏 APK (按架构分离)
./aab_to_apk.sh -m split app-release.aab
```

#### 直接安装到设备
```bash
# 安装到默认设备
./aab_to_apk.sh -m install app-release.aab

# 安装到指定设备
./aab_to_apk.sh -m install -d emulator-5554 app-release.aab

# 安装成功后自动删除临时文件
./aab_to_apk.sh -m install -c app-release.aab
```

## 参数说明

### aab_to_apk.sh 参数

- `-m, --mode MODE` - 模式: universal(通用APK), split(分屏APK), install(直接安装)
- `-d, --device DEVICE` - 指定设备ID
- `-o, --output DIR` - 输出目录
- `-k, --keystore FILE` - 密钥库文件
- `-a, --alias ALIAS` - 密钥别名
- `-c, --cleanup` - 安装成功后自动删除临时文件
- `-h, --help` - 显示帮助信息

## 输出文件

### 通用 APK 模式
- `output/app-universal.apk` - 通用 APK 文件

### 分屏 APK 模式
- `output/app-split.apks` - 分屏 APKS 文件

### 直接安装模式
- 直接安装到设备，不生成文件

## 注意事项

1. 确保已安装 Java 环境
2. 确保已安装 ADB (用于设备安装)
3. 确保设备已连接并开启 USB 调试
4. 密钥库文件和密码文件必须存在且正确

## 故障排除

### 常见错误

1. **Java 未找到**
   ```bash
   # 安装 Java
   sudo apt install openjdk-11-jdk
   ```

2. **ADB 未找到**
   ```bash
   # 安装 ADB
   sudo apt install android-tools-adb
   ```

3. **设备未连接**
   ```bash
   # 检查设备连接
   adb devices
   ```

4. **密钥库错误**
   - 检查 `keystore.jks` 文件是否存在
   - 检查密码文件内容是否正确 (密码: 0099oopp,.mnjkl;)
   - 检查密钥别名是否正确 (别名: key0)

## 示例

```bash
# 1. 生成通用 APK
./aab_to_apk.sh app-release.aab

# 2. 查看生成的 APK
ls -lh output/app-universal.apk

# 3. 手动安装 APK
adb install output/app-universal.apk
```

## 清理功能

### 清理的文件类型：
- **APKS 文件**：`app.apks`、`app-universal.apks`、`app-install.apks`
- **临时文件**：`*.pb` 文件（protobuf 临时文件）
- **解压文件**：`universal.apk`（解压后的临时文件）
- **保留 APK 文件**：`app-universal.apk`（最终安装文件）

### 清理方式：
1. **交互式清理**：安装/生成成功后询问是否删除临时文件
2. **自动清理**：使用 `-c` 参数自动删除临时文件
3. **手动清理**：手动删除 `output` 目录下的临时文件
