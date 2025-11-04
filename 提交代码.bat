@echo off
chcp 65001 >nul
echo ========================================
echo 准备提交代码到 GitHub
echo ========================================
echo.

echo [1/4] 检查 Git 状态...
git status
echo.

echo [2/4] 添加所有更改的文件...
git add .
echo.

echo [3/4] 提交更改...
git commit -m "fix: 修复办公区数据显示问题和OfficeManagement保存错误

- 修复设备管理页面办公区数据无法显示的问题
- 修复OfficeManagement.vue中saveWorkArea函数的selectedOfficeId未定义错误
- 优化消防疏散图显示功能，支持多路径回退"
echo.

echo [4/4] 推送到 GitHub...
git push origin master
echo.

echo ========================================
echo 提交完成！
echo ========================================
pause

