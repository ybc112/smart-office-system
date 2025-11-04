@echo off
chcp 65001 >nul
echo 正在配置消防疏散图...

if not exist "frontend\public\images" (
    echo 创建目录: frontend\public\images
    mkdir "frontend\public\images"
)

if exist "images\image.png" (
    echo 复制图片文件...
    copy "images\image.png" "frontend\public\images\image.png" >nul
    if %errorlevel% equ 0 (
        echo ✓ 图片文件已成功复制到 frontend\public\images\image.png
        echo.
        echo 配置完成！请重启前端开发服务器以使更改生效。
    ) else (
        echo ✗ 复制文件失败，请检查文件权限
    )
) else (
    echo ✗ 未找到源文件: images\image.png
    echo 请确保图片文件存在于项目根目录的 images 文件夹中
)

pause

