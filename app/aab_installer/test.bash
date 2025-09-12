#!/bin/bash

# AAB 转 APK 安装脚本
# 使用方法: ./test.bash [aab文件路径] [设备ID]

set -e

# 默认参数
AAB_FILE=${1:-"./app-release.aab"}
DEVICE_ID=${2:-""}
OUTPUT_DIR="./output"
APKS_FILE="$OUTPUT_DIR/app.apks"
UNIVERSAL_APK="$OUTPUT_DIR/app-universal.apk"

echo "=== AAB 转 APK 安装脚本 ==="
echo "AAB 文件: $AAB_FILE"
echo "设备 ID: ${DEVICE_ID:-"自动检测"}"

# 检查 AAB 文件是否存在
if [ ! -f "$AAB_FILE" ]; then
    echo "错误: AAB 文件不存在: $AAB_FILE"
    echo "使用方法: $0 [aab文件路径] [设备ID]"
    exit 1
fi

# 清理并创建输出目录
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

echo "=== 步骤 1: 生成 APKS 文件 ==="
java -jar bundletool.jar build-apks \
    --bundle="$AAB_FILE" \
    --output="$APKS_FILE" \
    --ks=./keystore.jks \
    --ks-pass=file:./keystore.pwd \
    --ks-key-alias=key0 \
    --key-pass=file:key.pwd

echo "=== 步骤 2: 生成通用 APK ==="
UNIVERSAL_APKS_FILE="$OUTPUT_DIR/app-universal.apks"
java -jar bundletool.jar build-apks \
    --bundle="$AAB_FILE" \
    --output="$UNIVERSAL_APKS_FILE" \
    --ks=./keystore.jks \
    --ks-pass=file:./keystore.pwd \
    --ks-key-alias=key0 \
    --key-pass=file:key.pwd \
    --mode=universal

# 解压 APKS 文件获取通用 APK
echo "=== 步骤 3: 解压 APKS 获取通用 APK ==="
unzip -o "$UNIVERSAL_APKS_FILE" -d "$OUTPUT_DIR"
mv "$OUTPUT_DIR/universal.apk" "$UNIVERSAL_APK"

echo "=== 步骤 4: 安装 APK ==="
INSTALL_SUCCESS=false
if [ -n "$DEVICE_ID" ]; then
    echo "安装到指定设备: $DEVICE_ID"
    if adb -s "$DEVICE_ID" install -r "$UNIVERSAL_APK"; then
        INSTALL_SUCCESS=true
    fi
else
    echo "安装到默认设备"
    if adb install -r "$UNIVERSAL_APK"; then
        INSTALL_SUCCESS=true
    fi
fi

if [ "$INSTALL_SUCCESS" = true ]; then
    echo "=== 安装成功 ==="
    echo "通用 APK 位置: $UNIVERSAL_APK"
    echo "APKS 文件位置: $APKS_FILE"
    
    # 显示文件信息
    echo ""
    echo "=== 文件信息 ==="
    ls -lh "$UNIVERSAL_APK"
    ls -lh "$APKS_FILE"
    
    # 询问是否删除临时文件
    echo ""
    echo "=== 清理临时文件 ==="
    echo "1) 只删除临时文件 (保留 APK)"
    echo "2) 删除所有文件 (包括 APK)"
    echo "3) 保留所有文件"
    read -p "请选择 (1/2/3): " -n 1 -r
    echo
    case $REPLY in
        1)
            echo "删除临时文件..."
            rm -f "$APKS_FILE"
            rm -f "$UNIVERSAL_APKS_FILE"
            # 删除 output 目录下的其他临时文件
            rm -f "$OUTPUT_DIR"/*.pb
            # 删除解压后的临时 APK 文件（如果存在）
            if [ -f "$OUTPUT_DIR/universal.apk" ]; then
                rm -f "$OUTPUT_DIR/universal.apk"
            fi
            echo "临时文件已删除"
            echo "保留的 APK 文件: $UNIVERSAL_APK"
            ;;
        2)
            echo "删除所有文件..."
            rm -f "$APKS_FILE"
            rm -f "$UNIVERSAL_APKS_FILE"
            rm -f "$OUTPUT_DIR"/*.pb
            rm -f "$UNIVERSAL_APK"
            if [ -f "$OUTPUT_DIR/universal.apk" ]; then
                rm -f "$OUTPUT_DIR/universal.apk"
            fi
            echo "所有文件已删除"
            ;;
        *)
            echo "保留所有文件"
            ;;
    esac
else
    echo "=== 安装失败 ==="
    echo "请检查设备连接和 APK 文件"
    echo "APK 文件位置: $UNIVERSAL_APK"
    echo "APKS 文件位置: $APKS_FILE"
fi