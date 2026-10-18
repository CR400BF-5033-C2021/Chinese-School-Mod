"""由 desk.json 派生"桌上有翻开的书"的模型变体 desk_book.json。

书立着、朝向课桌正面（模型 -Z 方向），通过模型元素旋转做出翻开的效果。
改动 desk.json 之后重新跑一次即可：

    python tools/add_book_variant.py
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODELS = os.path.join(ROOT, "src", "main", "resources", "assets", "chinese_school", "models", "block")
MODID = "chinese_school"

# 书本整体向后倾斜的角度；原点放在桌面靠近玩家的一侧
TILT = -40
ORIGIN = [8, 16.0, 6.0]

# 名字, from, to, z 轴额外旋转（做出左右两页的 V 形）, 各面 UV
BOOK_ELEMENTS = [
    (
        "book_cover",
        [1.8, 16.0, 3.2],
        [14.2, 16.3, 11.6],
        0,
        {
            "up": [0, 10, 16, 16],
            "down": [0, 10, 16, 16],
            "north": [0, 10, 16, 12],
            "south": [0, 10, 16, 12],
            "east": [0, 10, 16, 12],
            "west": [0, 10, 16, 12],
        },
    ),
    (
        "book_page_left",
        [2.2, 16.3, 3.6],
        [8.0, 16.6, 11.2],
        -6,
        {
            "up": [1, 1, 7, 9],
            "down": [1, 1, 7, 9],
            "north": [1, 1, 7, 2],
            "south": [1, 1, 7, 2],
            "east": [1, 1, 7, 2],
            "west": [1, 1, 7, 2],
        },
    ),
    (
        "book_page_right",
        [8.0, 16.3, 3.6],
        [13.8, 16.6, 11.2],
        6,
        {
            "up": [9, 1, 15, 9],
            "down": [9, 1, 15, 9],
            "north": [9, 1, 15, 2],
            "south": [9, 1, 15, 2],
            "east": [9, 1, 15, 2],
            "west": [9, 1, 15, 2],
        },
    ),
]


def main():
    desk_path = os.path.join(MODELS, "desk.json")
    out_path = os.path.join(MODELS, "desk_book.json")

    with open(desk_path, encoding="utf-8") as fh:
        model = json.load(fh)

    model["textures"]["book"] = f"{MODID}:block/desk_book"

    for name, start, end, z_angle, uvs in BOOK_ELEMENTS:
        rotation = {"origin": ORIGIN, "x": TILT, "z": z_angle, "rescale": False}
        model["elements"].append(
            {
                "name": name,
                "from": start,
                "to": end,
                "rotation": rotation,
                "faces": {face: {"texture": "#book", "uv": uv} for face, uv in uvs.items()},
            }
        )

    with open(out_path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(model, fh, ensure_ascii=False, indent=2)
        fh.write("\n")

    print(f"{desk_path} -> {out_path}（{len(model['elements'])} 个元素）")


if __name__ == "__main__":
    main()
