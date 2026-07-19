"""生成四张景区商户演示 logo 图（800x800 方形，低饱和暖色底 + 景区名称大字）。

风格与 docs/prototype/gen_demo_banners.py 的首页 banner 演示图保持一致：
对角三段渐变底、低透明度圆形装饰、胶囊 chip、主标题 + 分隔线 + 副标题。
输出到后端 uploadPath 对应目录，供 iip_merchant.logo 演示数据引用。
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
    """水平居中绘制胶囊 chip，返回 chip 底边 y 坐标。"""
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


# 四张图的配色与文案：均为低饱和暖色，与 banner 赤陶/墨金风格协调
LOGOS = [
    {
        "file": "merchant-scenic-1.jpg",
        "name": "殷墟博物馆",
        "sub": "发票积分合作景区",
        "colors": ((150, 96, 60), (178, 120, 74), (198, 148, 98)),  # 陶土暖棕
        "fg": (255, 245, 236, 255),
        "fg_dim": (240, 216, 196, 255),
        "accent": (255, 255, 255, 110),
    },
    {
        "file": "merchant-scenic-2.jpg",
        "name": "红旗渠风景区",
        "sub": "发票积分合作景区",
        "colors": ((156, 62, 46), (186, 86, 56), (204, 116, 72)),  # 低饱和暖红
        "fg": (255, 245, 238, 255),
        "fg_dim": (244, 214, 200, 255),
        "accent": (255, 255, 255, 110),
    },
    {
        "file": "merchant-scenic-3.jpg",
        "name": "太行大峡谷景区",
        "sub": "发票积分合作景区",
        "colors": ((104, 86, 64), (138, 112, 82), (168, 140, 104)),  # 暖岩褐
        "fg": (255, 248, 240, 255),
        "fg_dim": (238, 222, 204, 255),
        "accent": (255, 255, 255, 100),
    },
    {
        "file": "merchant-scenic-4.jpg",
        "name": "羑里城遗址",
        "sub": "发票积分合作景区",
        "colors": ((44, 40, 32), (58, 50, 38), (74, 62, 44)),  # 墨金
        "fg": (255, 232, 190, 255),
        "fg_dim": (214, 190, 152, 255),
        "accent": (200, 137, 26, 150),
    },
]

for cfg in LOGOS:
    c1, c2, c3 = cfg["colors"]
    img = gradient((W, H), c1, c2, c3).convert("RGBA")

    # 低透明度圆形装饰与顶部 chip 画在叠加层上，经 alpha_composite 正确做半透明混合
    ov = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(ov)
    circle(d, W - 30, -30, 240, (255, 255, 255, 18))
    circle(d, -60, H + 40, 200, (255, 255, 255, 14))
    circle(d, W - 180, H - 100, 90, (255, 255, 255, 12))
    chip_center(d, 120, "河南 · 安阳", cfg["fg"], (255, 255, 255, 40))
    img.alpha_composite(ov)
    d = ImageDraw.Draw(img)

    # 中部景区名 → 分隔线 → 副标题
    name_font = fit_name_font(cfg["name"], NAME_MAX_WIDTH, NAME_START_SIZE)
    box = d.textbbox((0, 0), cfg["name"], font=name_font)
    name_h = box[3] - box[1]
    text_center(d, (H - name_h) / 2 - box[1] - 20, cfg["name"], name_font, cfg["fg"])

    line_y = 560
    d.line([(W / 2 - 110, line_y), (W / 2 + 110, line_y)], fill=cfg["accent"], width=2)
    text_center(d, line_y + 28, cfg["sub"], font(34), cfg["fg_dim"])

    img.convert("RGB").save(OUT_DIR / cfg["file"], quality=88)

print("saved:", sorted(p.name for p in OUT_DIR.iterdir()))
