"""由 desk.json 派生出"桌上有书"的模型变体 desk_book.json。

课桌把书摆上桌面时通过方块状态 has_book 切换到这个模型，因此它是 desk.json 的
一份副本加上一个书本元素。改动 desk.json 之后重新跑一次即可：

    python tools/add_book_variant.py
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODELS = os.path.join(ROOT, "src", "main", "resources", "assets", "chinese_school", "models", "block")
MODID = "chinese_school"

# 书本摆放在桌面（y=16）之上
BOOK_FROM = [3.5, 16.0, 4.0]
BOOK_TO = [12.5, 16.9, 12.0]

# 各面在 desk_book.png 中取样的区域：桌面用封面，侧面用书页，靠外一侧用书脊
BOOK_FACES = {
    "up": [1, 1, 11, 10],
    "down": [1, 1, 11, 10],
    "north": [1, 12, 11, 14],
    "south": [1, 12, 11, 14],
    "east": [1, 12, 11, 14],
    "west": [13, 1, 15, 10],
}


def main():
    desk_path = os.path.join(MODELS, "desk.json")
    out_path = os.path.join(MODELS, "desk_book.json")

    with open(desk_path, encoding="utf-8") as fh:
        model = json.load(fh)

    model["textures"]["book"] = f"{MODID}:block/desk_book"
    model["elements"].append(
        {
            "from": BOOK_FROM,
            "to": BOOK_TO,
            "faces": {face: {"texture": "#book", "uv": uv} for face, uv in BOOK_FACES.items()},
        }
    )

    with open(out_path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(model, fh, ensure_ascii=False, indent=2)
        fh.write("\n")

    print(f"{desk_path} -> {out_path}（{len(model['elements'])} 个元素）")


if __name__ == "__main__":
    main()
