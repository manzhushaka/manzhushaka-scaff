"""生成两张存量推荐商户的演示 logo 图（800x800，风格与 gen_scenic_logos.py 一致）。

老字号烩面馆（merchant_id=1，餐饮）与殷都宾馆（merchant_id=2，住宿）原 logo
指向 /profile/avatar/merchant/ 下不存在或近乎空白的旧文件，首页推荐商户卡片
展示为空白。本脚本生成与景区 logo 同风格的演示图，供 iip_merchant.logo 引用。
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

W, H = 800, 800
OUT_DIR = Path("/Users/manzhushaka/kimi-work/iip/uploadPath/upload/2026/07/19")
OUT_DIR.mkdir(parents=True, exist_ok=True)
FONT = "/System/Library/Fonts/Hiragino Sans GB.ttc"

NAME_MAX_WIDTH = 660
NAME_START_SIZE = 128


def font(size):
    return ImageFont.truetype(FONT, size, index=1)


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def gradient(size, c1, c2, c3):
    """对角三段渐变：左上 c1 → 中 c2 → 右下 c3。"""
    img = Image.new("RGB", size)
    px = img.load()
    w, h = size
    for y in range(h):
        for x in range(0, w, 4):  # 4px 步进加速，肉眼无差
            t = (x / w + y / h) / 2
            color = lerp(c1, c2, t * 2) if t < 0.5 else lerp(c2, c3, (t - 0.5) * 2)
            for dx in range(4):
                if x + dx < w:
                    px[x + dx, y] = color
    return img


def circle(draw, cx, cy, r, rgba):
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=rgba)


def chip_center(draw, y, text, fg, bg):
    """水平居中绘制胶囊 chip（画在叠加层上），返回 chip 底边 y 坐标。"""
    f = font(30)
    box = draw.textbbox((0, 0), text, font=f)
    tw, th = box[2] - box[0], box[3] - box[1]
    pad_x, pad_y = 26, 12
    x = (W - tw - pad_x * 2) // 2
    draw.rounded_rectangle([x, y, x + tw + pad_x * 2, y + th + pad_y * 2], radius=(th + pad_y * 2) // 2, fill=bg)
    draw.text((x + pad_x, y + pad_y - box[1]), text, font=f, fill=fg)
    return y + th + pad_y * 2


def text_center(draw, y, text, f, fill):
    """水平居中绘制单行文字。"""
    box = draw.textbbox((0, 0), text, font=f)
    tw = box[2] - box[0]
    draw.text(((W - tw) / 2 - box[0], y), text, font=f, fill=fill)


def fit_name_font(text, max_width, start_size):
    """名称字号自适应：从 start_size 递减，直到宽度不超过 max_width。"""
    probe = ImageDraw.Draw(Image.new("RGB", (1, 1)))
    size = start_size
    while size > 40:
        f = font(size)
        box = probe.textbbox((0, 0), text, font=f)
        if box[2] - box[0] <= max_width:
            return f
        size -= 4
    return font(40)


# 两张图的配色与文案：低饱和暖色，与景区 logo / banner 风格协调
LOGOS = [
    {
        "file": "merchant-demo-1.jpg",
        "name": "老字号烩面馆",
        "chip": "餐饮 · 安阳老字号",
        "sub": "发票积分合作商户",
        "colors": ((168, 88, 32), (196, 116, 44), (214, 150, 78)),  # 暖橘棕
        "fg": (255, 246, 236, 255),
        "fg_dim": (242, 218, 194, 255),
        "accent": (255, 255, 255, 110),
    },
    {
        "file": "merchant-demo-2.jpg",
        "name": "殷都宾馆",
        "chip": "住宿 · 毗邻殷墟",
        "sub": "发票积分合作商户",
        "colors": ((84, 74, 60), (112, 98, 76), (142, 124, 96)),  # 暖咖褐
        "fg": (255, 248, 238, 255),
        "fg_dim": (236, 220, 198, 255),
        "accent": (255, 255, 255, 100),
    },
    {
        "file": "merchant-demo-5.jpg",
        "name": "东坡文化旅游区文创店",
        "chip": "景区 · 文创零售",
        "sub": "发票积分合作商户",
        "colors": ((66, 84, 66), (90, 110, 84), (118, 138, 106)),  # 暖橄榄绿
        "fg": (246, 248, 238, 255),
        "fg_dim": (216, 226, 202, 255),
        "accent": (255, 255, 255, 100),
    },
]

for cfg in LOGOS:
    c1, c2, c3 = cfg["colors"]
    img = gradient((W, H), c1, c2, c3).convert("RGBA")

    # 圆形装饰与半透明 chip 画在叠加层上，经 alpha_composite 正确混合
    ov = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(ov)
    circle(d, W - 30, -30, 240, (255, 255, 255, 18))
    circle(d, -60, H + 40, 200, (255, 255, 255, 14))
    circle(d, W - 180, H - 100, 90, (255, 255, 255, 12))
    chip_center(d, 120, cfg["chip"], cfg["fg"], (255, 255, 255, 40))
    img.alpha_composite(ov)
    d = ImageDraw.Draw(img)

    # 中部商户名 → 分隔线 → 副标题
    name_font = fit_name_font(cfg["name"], NAME_MAX_WIDTH, NAME_START_SIZE)
    box = d.textbbox((0, 0), cfg["name"], font=name_font)
    name_h = box[3] - box[1]
    text_center(d, (H - name_h) / 2 - box[1] - 20, cfg["name"], name_font, cfg["fg"])

    line_y = 560
    d.line([(W / 2 - 110, line_y), (W / 2 + 110, line_y)], fill=cfg["accent"], width=2)
    text_center(d, line_y + 28, cfg["sub"], font(34), cfg["fg_dim"])

    img.convert("RGB").save(OUT_DIR / cfg["file"], quality=88)

print("saved:", [cfg["file"] for cfg in LOGOS])
