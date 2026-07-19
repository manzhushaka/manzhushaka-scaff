"""生成两张演示 banner 图（品牌风格：赤陶渐变 / 墨金），供首页轮播演示数据使用。"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

W, H = 1404, 720
OUT_DIR = Path("/Users/manzhushaka/kimi-work/iip/uploadPath/upload/2026/07/19")
OUT_DIR.mkdir(parents=True, exist_ok=True)
FONT = "/System/Library/Fonts/Hiragino Sans GB.ttc"


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


def chip(draw, x, y, text, fg, bg):
    f = font(30)
    box = draw.textbbox((0, 0), text, font=f)
    tw, th = box[2] - box[0], box[3] - box[1]
    pad_x, pad_y = 26, 12
    draw.rounded_rectangle([x, y, x + tw + pad_x * 2, y + th + pad_y * 2], radius=(th + pad_y * 2) // 2, fill=bg)
    draw.text((x + pad_x, y + pad_y - box[1]), text, font=f, fill=fg)


# ---------- banner 1：赤陶红渐变（活动主视觉） ----------
img = gradient((W, H), (185, 44, 18), (217, 74, 30), (224, 123, 42)).convert("RGBA")
ov = Image.new("RGBA", (W, H), (0, 0, 0, 0))
d = ImageDraw.Draw(ov)
circle(d, W - 40, -40, 300, (255, 255, 255, 20))
circle(d, W - 220, H + 60, 220, (255, 255, 255, 18))
img.alpha_composite(ov)
d = ImageDraw.Draw(img)

chip(d, 72, 78, "以票促消 · 以游惠民", (255, 240, 230, 255), (255, 255, 255, 46))
d.text((72, 180), "乐享安阳——发票核验积分", font=font(88), fill=(255, 255, 255, 255))
d.text((72, 300), "兑换活动", font=font(88), fill=(255, 255, 255, 255))
d.text((74, 452), "上传发票攒积分，兑换景区门票与商户优惠券", font=font(38), fill=(255, 235, 224, 255))
d.line([(74, 560), (760, 560)], fill=(255, 255, 255, 90), width=2)
d.text((74, 592), "活动期：2026.07.01 ~ 2027.05.31", font=font(34), fill=(255, 220, 205, 255))
img.convert("RGB").save(OUT_DIR / "banner-demo-1.jpg", quality=88)

# ---------- banner 2：墨金（积分商城引流） ----------
img = Image.new("RGBA", (W, H), (34, 30, 24, 255))
ov = Image.new("RGBA", (W, H), (0, 0, 0, 0))
d = ImageDraw.Draw(ov)
circle(d, W - 60, 60, 260, (200, 137, 26, 28))
circle(d, W - 260, H + 40, 200, (200, 137, 26, 22))
img.alpha_composite(ov)
d = ImageDraw.Draw(img)

chip(d, 72, 78, "积分商城", (255, 217, 138, 255), (200, 137, 26, 60))
d.text((72, 190), "积分兑好礼 · 惠游安阳", font=font(88), fill=(255, 248, 238, 255))
d.text((74, 340), "景区门票 / 酒店券 / 餐饮满减，积分即可兑换", font=font(38), fill=(214, 202, 188, 255))
d.line([(74, 470), (760, 470)], fill=(200, 137, 26, 120), width=2)
d.text((74, 502), "热门门票 2,000 分起兑", font=font(34), fill=(255, 217, 138, 255))
img.convert("RGB").save(OUT_DIR / "banner-demo-2.jpg", quality=88)

print("saved:", list(p.name for p in OUT_DIR.iterdir()))
