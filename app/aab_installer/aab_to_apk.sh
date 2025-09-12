#!/bin/bash

# AAB 转 APK 工具脚本
# 支持多种模式：通用APK、分屏APK、直接安装等

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 默认参数
AAB_FILE=""
DEVICE_ID=""
OUTPUT_DIR="./output"
MODE="universal"  # universal, split, install
KEYSTORE_FILE="./keystore.jks"
KEYSTORE_PASS_FILE="./keystore.pwd"
KEY_ALIAS="key0"
KEY_PASS_FILE="./key.pwd"
AUTO_CLEANUP=false

# 显示帮助信息
show_help() {
    echo -e "${BLUE}AAB 转 APK 工具${NC}"
    echo ""
    echo "使用方法:"
    echo "  $0 [选项] <aab文件>"
    echo ""
    echo "选项:"
    echo "  -m, --mode MODE        模式: universal(通用APK), split(分屏APK), install(直接安装) [默认: universal]"
    echo "  -d, --device DEVICE    指定设备ID"
    echo "  -o, --output DIR       输出目录 [默认: ./output]"
    echo "  -k, --keystore FILE    密钥库文件 [默认: ./keystore.jks]"
    echo "  -a, --alias ALIAS      密钥别名 [默认: key0]"
    echo "  -c, --cleanup          安装成功后自动删除临时文件"
    echo "  -h, --help             显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 app-release.aab                           # 生成通用APK"
    echo "  $0 -m split app-release.aab                  # 生成分屏APK"
    echo "  $0 -m install -d emulator-5554 app-release.aab  # 直接安装到指定设备"
    echo "  $0 -o ./apk_output app-release.aab           # 指定输出目录"
}

# 解析命令行参数
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -m|--mode)
                MODE="$2"
                shift 2
                ;;
            -d|--device)
                DEVICE_ID="$2"
                shift 2
                ;;
            -o|--output)
                OUTPUT_DIR="$2"
                shift 2
                ;;
            -k|--keystore)
                KEYSTORE_FILE="$2"
                shift 2
                ;;
            -a|--alias)
                KEY_ALIAS="$2"
                shift 2
                ;;
            -c|--cleanup)
                AUTO_CLEANUP=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            -*)
                echo -e "${RED}错误: 未知选项 $1${NC}"
                show_help
                exit 1
                ;;
            *)
                if [ -z "$AAB_FILE" ]; then
                    AAB_FILE="$1"
                else
                    echo -e "${RED}错误: 只能指定一个AAB文件${NC}"
                    exit 1
                fi
                shift
                ;;
        esac
    done
}

# 检查依赖
check_dependencies() {
    echo -e "${BLUE}=== 检查依赖 ===${NC}"
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        echo -e "${RED}错误: 未找到 Java${NC}"
        exit 1
    fi
    
    # 检查 bundletool
    if [ ! -f "bundletool.jar" ]; then
        echo -e "${RED}错误: 未找到 bundletool.jar${NC}"
        exit 1
    fi
    
    # 检查密钥库文件
    if [ ! -f "$KEYSTORE_FILE" ]; then
        echo -e "${RED}错误: 未找到密钥库文件: $KEYSTORE_FILE${NC}"
        exit 1
    fi
    
    # 检查密码文件
    if [ ! -f "$KEYSTORE_PASS_FILE" ]; then
        echo -e "${RED}错误: 未找到密钥库密码文件: $KEYSTORE_PASS_FILE${NC}"
        exit 1
    fi
    
    if [ ! -f "$KEY_PASS_FILE" ]; then
        echo -e "${RED}错误: 未找到密钥密码文件: $KEY_PASS_FILE${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}依赖检查通过${NC}"
}

# 检查 AAB 文件
check_aab_file() {
    if [ -z "$AAB_FILE" ]; then
        echo -e "${RED}错误: 请指定AAB文件${NC}"
        show_help
        exit 1
    fi
    
    if [ ! -f "$AAB_FILE" ]; then
        echo -e "${RED}错误: AAB文件不存在: $AAB_FILE${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}AAB文件检查通过: $AAB_FILE${NC}"
}

# 创建输出目录
create_output_dir() {
    rm -rf "$OUTPUT_DIR"
    mkdir -p "$OUTPUT_DIR"
    echo -e "${GREEN}输出目录: $OUTPUT_DIR${NC}"
}

# 生成通用 APK
generate_universal_apk() {
    echo -e "${BLUE}=== 生成通用 APK ===${NC}"
    
    local apks_file="$OUTPUT_DIR/app-universal.apks"
    local universal_apk="$OUTPUT_DIR/app-universal.apk"
    
    echo "生成 APKS 文件..."
    java -jar bundletool.jar build-apks \
        --bundle="$AAB_FILE" \
        --output="$apks_file" \
        --ks="$KEYSTORE_FILE" \
        --ks-pass=file:"$KEYSTORE_PASS_FILE" \
        --ks-key-alias="$KEY_ALIAS" \
        --key-pass=file:"$KEY_PASS_FILE" \
        --mode=universal
    
    echo "解压 APKS 文件..."
    unzip -o "$apks_file" -d "$OUTPUT_DIR"
    mv "$OUTPUT_DIR/universal.apk" "$universal_apk"
    
    echo -e "${GREEN}通用 APK 生成完成: $universal_apk${NC}"
    ls -lh "$universal_apk"
    
    # 询问是否清理临时文件
    if [ "$AUTO_CLEANUP" = true ]; then
        echo "自动删除临时文件..."
        rm -f "$apks_file"
        rm -f "$OUTPUT_DIR"/*.pb
        echo -e "${GREEN}临时文件已删除${NC}"
    else
        echo -e "${YELLOW}=== 清理临时文件 ===${NC}"
        read -p "是否删除临时文件? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "删除临时文件..."
            rm -f "$apks_file"
            rm -f "$OUTPUT_DIR"/*.pb
            echo -e "${GREEN}临时文件已删除${NC}"
        else
            echo "保留临时文件: $apks_file"
        fi
    fi
}

# 生成分屏 APK
generate_split_apks() {
    echo -e "${BLUE}=== 生成分屏 APK ===${NC}"
    
    local apks_file="$OUTPUT_DIR/app-split.apks"
    
    echo "生成分屏 APKS 文件..."
    java -jar bundletool.jar build-apks \
        --bundle="$AAB_FILE" \
        --output="$apks_file" \
        --ks="$KEYSTORE_FILE" \
        --ks-pass=file:"$KEYSTORE_PASS_FILE" \
        --ks-key-alias="$KEY_ALIAS" \
        --key-pass=file:"$KEY_PASS_FILE"
    
    echo -e "${GREEN}分屏 APKS 生成完成: $apks_file${NC}"
    ls -lh "$apks_file"
}

# 直接安装
install_directly() {
    echo -e "${BLUE}=== 直接安装 ===${NC}"
    
    # 检查 ADB
    if ! command -v adb &> /dev/null; then
        echo -e "${RED}错误: 未找到 ADB${NC}"
        exit 1
    fi
    
    # 检查设备连接
    if [ -n "$DEVICE_ID" ]; then
        if ! adb -s "$DEVICE_ID" get-state &> /dev/null; then
            echo -e "${RED}错误: 设备 $DEVICE_ID 未连接${NC}"
            exit 1
        fi
        echo "目标设备: $DEVICE_ID"
    else
        local devices=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)
        if [ "$devices" -eq 0 ]; then
            echo -e "${RED}错误: 未找到连接的设备${NC}"
            exit 1
        elif [ "$devices" -gt 1 ]; then
            echo -e "${YELLOW}警告: 发现多个设备，将使用默认设备${NC}"
        fi
    fi
    
    local apks_file="$OUTPUT_DIR/app-install.apks"
    
    echo "生成 APKS 文件..."
    java -jar bundletool.jar build-apks \
        --bundle="$AAB_FILE" \
        --output="$apks_file" \
        --ks="$KEYSTORE_FILE" \
        --ks-pass=file:"$KEYSTORE_PASS_FILE" \
        --ks-key-alias="$KEY_ALIAS" \
        --key-pass=file:"$KEY_PASS_FILE"
    
    echo "安装到设备..."
    local install_success=false
    if [ -n "$DEVICE_ID" ]; then
        if java -jar bundletool.jar install-apks --apks="$apks_file" --device-id="$DEVICE_ID"; then
            install_success=true
        fi
    else
        if java -jar bundletool.jar install-apks --apks="$apks_file"; then
            install_success=true
        fi
    fi
    
    if [ "$install_success" = true ]; then
        echo -e "${GREEN}安装完成${NC}"
        
        # 清理临时文件
        if [ "$AUTO_CLEANUP" = true ]; then
            echo "自动删除临时文件..."
            rm -f "$apks_file"
            # 删除 output 目录下的其他临时文件
            rm -f "$OUTPUT_DIR"/*.pb
            echo -e "${GREEN}临时文件已删除${NC}"
        else
            echo -e "${YELLOW}=== 清理临时文件 ===${NC}"
            read -p "是否删除临时文件? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo "删除临时文件..."
                rm -f "$apks_file"
                # 删除 output 目录下的其他临时文件
                rm -f "$OUTPUT_DIR"/*.pb
                echo -e "${GREEN}临时文件已删除${NC}"
            else
                echo "保留临时文件: $apks_file"
            fi
        fi
    else
        echo -e "${RED}安装失败${NC}"
        echo "APKS 文件位置: $apks_file"
    fi
}

# 主函数
main() {
    echo -e "${BLUE}=== AAB 转 APK 工具 ===${NC}"
    echo "AAB 文件: $AAB_FILE"
    echo "模式: $MODE"
    echo "设备: ${DEVICE_ID:-"自动检测"}"
    echo "输出目录: $OUTPUT_DIR"
    echo ""
    
    check_dependencies
    check_aab_file
    create_output_dir
    
    case $MODE in
        universal)
            generate_universal_apk
            ;;
        split)
            generate_split_apks
            ;;
        install)
            install_directly
            ;;
        *)
            echo -e "${RED}错误: 无效的模式: $MODE${NC}"
            echo "支持的模式: universal, split, install"
            exit 1
            ;;
    esac
    
    echo -e "${GREEN}=== 完成 ===${NC}"
}

# 解析参数并运行
parse_args "$@"
main
