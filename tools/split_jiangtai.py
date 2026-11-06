"""把 blockbench/jiangtai.bbmodel 拆成多方块结构的两个半块模型。

模型原始范围 X: -8..24（宽 2 格）。整体右移 8 得到 0..32：
- 0..16  -> 放在"副块"（玩家右侧）
- 16..32 -> 放在"主块"（玩家点击的那一格，即左半），坐标整体减 16
跨越 X=16 接缝的方块会被切成两块，UV 按新坐标重新投影。
"""
import json
import os

ROOT = r"D:\zxm20\Documents\jvavproject\chinese_school"
BB = os.path.join(ROOT, "blockbench", "jiangtai.bbmodel")
MODELS = os.path.join(ROOT, "src", "main", "resources", "assets", "chinese_school", "models", "block")
SEAM = 16.0
SHIFT = 8.0

FACES = ("north", "south", "east", "west", "up", "down")


def projection_uv(face, x1, y1, z1, x2, y2, z2):
    if face in ("north", "south"):
        return [x1, 16 - y2, x2, 16 - y1]
    if face in ("east", "west"):
        return [z1, 16 - y2, z2, 16 - y1]
    return [x1, z1, x2, z2]


def clipped_pieces(box):
    """把 (x1,y1,z1,x2,y2,z2) 按 X=16 切成不跨界的小块。"""
    x1, y1, z1, x2, y2, z2 = box
    if x2 <= SEAM:
        return [(x1, y1, z1, x2, y2, z2)]
    if x1 >= SEAM:
        return [(x1, y1, z1, x2, y2, z2)]
    return [(x1, y1, z1, SEAM, y2, z2), (SEAM, y1, z1, x2, y2, z2)]


def build_half(name, boxes, offset_x):
    elements = []
    for idx, (x1, y1, z1, x2, y2, z2) in enumerate(boxes):
        fx1, fx2 = x1 - offset_x, x2 - offset_x
        faces = {}
        for face in FACES:
            faces[face] = {
                "texture": "#stripped_oak_log",
                "uv": projection_uv(face, fx1, y1, z1, fx2, y2, z2),
            }
        elements.append({
            "name": f"{name}_{idx}",
            "from": [fx1, y1, z1],
            "to": [fx2, y2, z2],
            "faces": faces,
        })
    return {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": "minecraft:block/stripped_oak_log",
            "stripped_oak_log": "minecraft:block/stripped_oak_log",
        },
        "elements": elements,
    }


def main():
    bb = json.load(open(BB, encoding="utf-8"))
    extra_boxes, main_boxes = [], []
    for el in bb["elements"]:
        x1, y1, z1 = el["from"]
        x2, y2, z2 = el["to"]
        for piece in clipped_pieces((x1 + SHIFT, y1, z1, x2 + SHIFT, y2, z2)):
            px1, py1, pz1, px2, py2, pz2 = piece
            if px2 <= SEAM:
                extra_boxes.append(piece)
            else:
                main_boxes.append(piece)

    for name, boxes, offset in (("jiangtai_extra", extra_boxes, 0.0),
                                ("jiangtai_main", main_boxes, SEAM)):
        model = build_half(name, boxes, offset)
        out = os.path.join(MODELS, name + ".json")
        with open(out, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(model, fh, ensure_ascii=False, indent=2)
            fh.write("\n")
        xs = [v for e in model["elements"] for v in (e["from"][0], e["to"][0])]
        zs = [v for e in model["elements"] for v in (e["from"][2], e["to"][2])]
        ys = [v for e in model["elements"] for v in (e["from"][1], e["to"][1])]
        print(f"{name}: {len(model['elements'])} 个元素, X {min(xs):g}..{max(xs):g}, "
              f"Y {min(ys):g}..{max(ys):g}, Z {min(zs):g}..{max(zs):g}")

    # 旧的单块模型不再使用
    old = os.path.join(MODELS, "jiangtai.json")
    if os.path.exists(old):
        os.remove(old)
        print("removed obsolete:", old)


if __name__ == "__main__":
    main()


def build_inventory_model():
    """物品栏用的缩小版整体模型：整张讲台等比缩到 16 格内，居中。"""
    bb = json.load(open(BB, encoding="utf-8"))
    elements = []
    cx, cz = 8.0, 8.0
    for idx, el in enumerate(bb["elements"]):
        x1, y1, z1 = el["from"]
        x2, y2, z2 = el["to"]
        nx1, nx2 = cx + (x1 - cx) * 0.5, cx + (x2 - cx) * 0.5
        ny1, ny2 = y1 * 0.5, y2 * 0.5
        nz1, nz2 = cz + (z1 - cz) * 0.5, cz + (z2 - cz) * 0.5
        faces = {}
        for face in FACES:
            faces[face] = {"texture": "#stripped_oak_log",
                           "uv": projection_uv(face, nx1, ny1, nz1, nx2, ny2, nz2)}
        elements.append({"name": f"inv_{idx}", "from": [nx1, ny1, nz1], "to": [nx2, ny2, nz2], "faces": faces})
    model = {
        "parent": "minecraft:block/block",
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
            "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
        },
        "textures": {"particle": "minecraft:block/stripped_oak_log", "stripped_oak_log": "minecraft:block/stripped_oak_log"},
        "elements": elements,
    }
    out = os.path.join(MODELS, "jiangtai_inventory.json")
    with open(out, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(model, fh, ensure_ascii=False, indent=2)
        fh.write("\n")
    print("inventory model:", out, f"({len(elements)} elements)")


build_inventory_model()
