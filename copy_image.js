const fs = require('fs');
const path = require('path');

// 创建目标目录
const targetDir = path.join(__dirname, 'frontend', 'public', 'images');
if (!fs.existsSync(targetDir)) {
  fs.mkdirSync(targetDir, { recursive: true });
  console.log(`✓ 已创建目录: ${targetDir}`);
}

// 复制文件
const sourceFile = path.join(__dirname, 'images', 'image.png');
const targetFile = path.join(targetDir, 'image.png');

if (fs.existsSync(sourceFile)) {
  fs.copyFileSync(sourceFile, targetFile);
  console.log(`✓ 图片已成功复制到: ${targetFile}`);
  console.log(`  源文件: ${path.resolve(sourceFile)}`);
  console.log(`  目标文件: ${path.resolve(targetFile)}`);
} else {
  console.log(`✗ 源文件不存在: ${path.resolve(sourceFile)}`);
}

