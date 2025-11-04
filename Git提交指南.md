# Git 提交操作指南

## 自动提交（推荐）

直接运行 `提交代码.bat` 文件即可自动完成：
1. 检查 Git 状态
2. 添加所有更改的文件
3. 提交更改（包含详细的提交信息）
4. 推送到 GitHub

## 手动提交步骤

如果自动脚本无法运行，请手动执行以下命令：

```bash
# 1. 检查当前状态
git status

# 2. 添加所有更改的文件
git add .

# 3. 提交更改
git commit -m "fix: 修复办公区数据显示问题和OfficeManagement保存错误

- 修复设备管理页面办公区数据无法显示的问题
- 修复OfficeManagement.vue中saveWorkArea函数的selectedOfficeId未定义错误
- 优化消防疏散图显示功能，支持多路径回退"

# 4. 推送到 GitHub
git push origin master
```

## 注意事项

1. 确保您已经配置了 Git 用户信息：
   ```bash
   git config --global user.name "ybc112"
   git config --global user.email "your-email@example.com"
   ```

2. 如果推送时提示需要认证，请确保：
   - 已配置 SSH 密钥，或
   - 使用 GitHub Personal Access Token

3. 如果远程仓库分支不是 master，请将 `master` 替换为您的分支名（如 `main`）

