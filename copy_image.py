import os
import shutil

# 创建目标目录
target_dir = 'frontend/public/images'
os.makedirs(target_dir, exist_ok=True)

# 复制文件
source_file = 'images/image.png'
target_file = os.path.join(target_dir, 'image.png')

if os.path.exists(source_file):
    shutil.copy2(source_file, target_file)
    print(f'✓ 图片已成功复制到 {target_file}')
    print(f'  源文件: {os.path.abspath(source_file)}')
    print(f'  目标文件: {os.path.abspath(target_file)}')
else:
    print(f'✗ 源文件不存在: {os.path.abspath(source_file)}')

