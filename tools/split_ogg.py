"""把上下课铃声按秒切成小段（按 Ogg 页边界切割，无需重新编码）。

切完后每个小段都是一个可以独立播放的 .ogg，带上原始的文件头页，
配合 MOD 里"每秒播下一段"的逻辑，就能实现走出范围再回来时从中途继续播放。
"""
import json
import os
import struct

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOUNDS = os.path.join(ROOT, "src", "main", "resources", "assets", "chinese_school", "sounds")
LANG_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "chinese_school", "lang")


def read_pages(data):
    """拆出所有 Ogg 页，返回 (start, end, header_type, granule) 列表。"""
    pages = []
    offset = 0
    while offset + 27 <= len(data):
        if data[offset:offset + 4] != b"OggS":
            # 跳过脏数据，找下一个页头
            nxt = data.find(b"OggS", offset + 1)
            if nxt < 0:
                break
            offset = nxt
            continue
        header_type = data[offset + 5]
        granule = struct.unpack_from("<q", data, offset + 6)[0]
        segments = data[offset + 26]
        body_start = offset + 27 + segments
        body_len = sum(data[offset + 27 + i] for i in range(segments))
        end = body_start + body_len
        pages.append((offset, end, header_type, granule))
        offset = end
    return pages


def sample_rate(data, pages):
    """从 identification header 里读采样率。"""
    start, _end, _ht, _g = pages[0]
    segments = data[start + 26]
    body_start = start + 27 + segments
    body = data[body_start:body_start + 64]
    if body[1:7] != b"vorbis":
        raise ValueError("不是 Vorbis 流")
    return struct.unpack_from("<I", body, 12)[0]


def split(path, name, seconds_per_chunk=1):
    data = open(path, "rb").read()
    pages = read_pages(data)
    rate = sample_rate(data, pages)

    # 头页：BOS 页 + comment/setup 页（它们的 granule 都是 0），
    # 真正的音频页从第一个 granule > 0 的页开始
    header_end = 0
    for idx, (_s, _end, _ht, granule) in enumerate(pages):
        if idx > 0 and granule > 0:
            header_end = idx
            break
    headers = pages[:header_end]

    chunks = []
    current = []
    current_bucket = None
    for page in pages[header_end:]:
        start, end, header_type, granule = page
        if header_type & 0x01:
            # 续包的起始页跳过，避免切开一半的数据包
            continue
        bucket = 0 if granule < 0 else granule // (rate * seconds_per_chunk)
        if current_bucket is None:
            current_bucket = bucket
        if bucket != current_bucket:
            chunks.append(current)
            current = []
            current_bucket = bucket
        current.append(page)
    if current:
        chunks.append(current)

    out_dir = os.path.join(SOUNDS, "chunks")
    os.makedirs(out_dir, exist_ok=True)
    for old in os.listdir(out_dir):
        if old.startswith(name + "_"):
            os.remove(os.path.join(out_dir, old))

    names = []
    for idx, chunk in enumerate(chunks):
        buf = bytearray()
        for start, end, _ht, _g in headers + chunk:
            buf += data[start:end]
        chunk_name = f"{name}_{idx:02d}"
        with open(os.path.join(out_dir, f"{chunk_name}.ogg"), "wb") as fh:
            fh.write(buf)
        # 每段都必须带齐三个 Vorbis 头（identification / comment / setup）
        for mark in (b"\x01vorbis", b"\x03vorbis", b"\x05vorbis"):
            if mark not in buf[:4096]:
                raise ValueError(f"{chunk_name} 缺少头部标记 {mark!r}")
        names.append(chunk_name)

    print(f"{name}: {len(pages)} 页, 采样率 {rate}, 头页 {len(headers)} 个, 切出 {len(names)} 段")
    return names


def update_sounds_json(all_names):
    entries = {}
    for chunk_name in all_names:
        entries[chunk_name] = {"sounds": [{"name": "chinese_school:chunks/" + chunk_name, "stream": True}]}
    path = os.path.join(SOUNDS, "..", "sounds.json")
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(entries, fh, ensure_ascii=False, indent=2)
        fh.write("\n")
    print("updated sounds.json:", len(entries), "entries")


def update_lang(all_names):
    titles = {"shangke": "上课铃响起", "xiake": "下课铃响起"}
    titles_en = {"shangke": "Class bell rings", "xiake": "Break bell rings"}
    for lang, table in (("zh_cn.json", titles), ("en_us.json", titles_en)):
        path = os.path.join(LANG_DIR, lang)
        data = json.load(open(path, encoding="utf-8"))
        for chunk_name in all_names:
            base = "shangke" if chunk_name.startswith("shangke") else "xiake"
            key = "subtitles.chinese_school." + chunk_name.replace("/", ".")
            data[key] = table[base]
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(data, fh, ensure_ascii=False, indent=2)
            fh.write("\n")
    print("updated lang files")


def main():
    all_names = []
    all_names += split(os.path.join(SOUNDS, "shangke.ogg"), "shangke")
    all_names += split(os.path.join(SOUNDS, "xiake.ogg"), "xiake")
    update_sounds_json(all_names)
    update_lang(all_names)
    print("TOTAL:", len(all_names))


if __name__ == "__main__":
    main()
