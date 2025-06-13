from PIL import Image, ImageDraw, ImageFont
import json
from io import BytesIO
import logging
import sys


def draw_ocr_results_return_bytes(json_path: str) -> bytes:
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    image_width = 800

    image_height = max(
        item['location']['top'] + item['location']['height'] + 40
        for item in data['words_result']
    ) + 20

    img = Image.new('RGB', (image_width, image_height), color='white')
    draw = ImageDraw.Draw(img)

    try:
        font = ImageFont.truetype("msyh.ttc", size=18)
    except:
        raise Exception("字体加载失败，请确认字体文件路径或名称正确")

    for item in data['words_result']:
        loc = item['location']
        x = loc['left']
        y = loc['top']
        text = item.get('targetWords', '')
        draw.text((x, y), text, fill='black', font=font)

    # 将图片保存到内存字节流
    img_bytes_io = BytesIO()
    img.save(img_bytes_io, format='PNG')
    img_bytes = img_bytes_io.getvalue()  # 这就是bytes

    return img_bytes


if __name__ == "__main__":
    import sys

    if len(sys.argv) != 2:
        sys.exit(1)

    json_path = sys.argv[1]
    img_bytes = draw_ocr_results_return_bytes(json_path)

    # 如果你想测试保存成文件看看
    with open('output_test.png', 'wb') as f:
          f.write(img_bytes)
    sys.stdout.buffer.write(img_bytes)
