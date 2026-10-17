"""把 Blockbench 的 .bbmodel 工程转换成 Minecraft 方块模型 JSON。

用法：
    python tools/bbmodel_to_model.py <输入.bbmodel> <输出模型.json> [--modid chinese_school]

约定：
    - bbmodel 中的纹理文件名（去掉 .png）就是模型里的材质名，
      例如 desk.png -> "#desk"，纹理路径为 "<modid>:block/desk"；
    - 第一个纹理同时作为 particle 纹理；
    - 仅支持不带元素旋转的 cube（Minecraft 方块模型本身也不支持元素旋转）。
"""
import argparse
import json
import os
import sys


def convert(bbmodel_path, modid):
    with open(bbmodel_path, encoding="utf-8") as fh:
        bb = json.load(fh)

    textures = bb.get("textures", [])
    if not textures:
        raise SystemExit("bbmodel 里没有纹理")

    # 纹理 id -> 材质名（文件名去扩展名）
    id_to_material = {}
    material_to_path = {}
    for texture in textures:
        name = texture.get("name") or ""
        material = os.path.splitext(name)[0] or f"texture{texture.get('id', 0)}"
        id_to_material[str(texture.get("id"))] = material
        material_to_path[material] = f"{modid}:block/{material}"

    elements = []
    for element in bb.get("elements", []):
        if element.get("type", "cube") != "cube":
            raise SystemExit(f"不支持的元素类型：{element.get('type')}")
        if "rotation" in element:
            raise SystemExit(f"元素 {element.get('name')} 带旋转，Minecraft 方块模型不支持元素旋转")

        faces = {}
        for face_name, face in element.get("faces", {}).items():
            if "texture" not in face:
                continue
            material = id_to_material[str(face["texture"])]
            entry = {"texture": "#" + material}
            if "uv" in face:
                entry["uv"] = face["uv"]
            if "rotation" in face:
                entry["rotation"] = face["rotation"]
            faces[face_name] = entry

        elements.append({"from": element["from"], "to": element["to"], "faces": faces})

    model_textures = {"particle": next(iter(material_to_path.values()))}
    for material, path in material_to_path.items():
        model_textures[material] = path

    return {
        "parent": "minecraft:block/block",
        "textures": model_textures,
        "elements": elements,
    }


def main():
    parser = argparse.ArgumentParser(description="bbmodel -> Minecraft 方块模型 JSON")
    parser.add_argument("bbmodel")
    parser.add_argument("output")
    parser.add_argument("--modid", default="chinese_school")
    args = parser.parse_args()

    model = convert(args.bbmodel, args.modid)

    output_dir = os.path.dirname(os.path.abspath(args.output))
    os.makedirs(output_dir, exist_ok=True)
    with open(args.output, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(model, fh, ensure_ascii=False, indent=2)
        fh.write("\n")

    print(f"{args.bbmodel} -> {args.output}（{len(model['elements'])} 个元素，"
          f"{len(model['textures']) - 1} 种材质）")


if __name__ == "__main__":
    sys.exit(main())
