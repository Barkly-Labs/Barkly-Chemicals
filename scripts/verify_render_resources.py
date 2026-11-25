#!/usr/bin/env python3
"""Static validation for ChemCraft profile and modifier rendering resources."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/barklychemlab"
TYPES = {"edge_tint", "bands", "motif", "orbit", "particles", "motion_trail", "chromatic", "post_vortex", "post_spectral"}
ACCESSIBILITY = {"visual", "hud", "trails", "chromatic", "motion"}
profiles = sorted((ROOT / "experience_profiles").glob("*.json"))
modifiers = sorted((ROOT / "experience_modifiers").glob("*.json"))
assert profiles, "No experience profiles found"
assert modifiers, "No experience modifier resources found"
for path in profiles:
    profile = json.loads(path.read_text(encoding="utf-8"))
    assert profile["id"] == path.stem, f"Profile ID/path mismatch: {path}"
    layers = profile.get("layers", [])
    assert 1 <= len(layers) <= 16, f"Invalid layer count: {path}"
    ids = set()
    for layer in layers:
        assert layer["id"] not in ids, f"Duplicate layer ID in {path}: {layer['id']}"
        ids.add(layer["id"])
        assert layer["type"] in TYPES, f"Unknown renderer type in {path}: {layer['type']}"
        assert layer.get("accessibility", "visual") in ACCESSIBILITY, f"Unknown accessibility group: {path}"
        assert -10000 <= layer["order"] <= 10000
        for key in ("intensity", "opacity"):
            assert 0 <= layer[key] <= 1, f"Out-of-range {key} in {path}"
        assert 0 <= layer["speed"] <= 4 and 0.25 <= layer["scale"] <= 4
for path in modifiers:
    modifier = json.loads(path.read_text(encoding="utf-8"))
    assert modifier["id"] == path.stem, f"Modifier ID/path mismatch: {path}"
    assert 0.5 <= modifier["intensity_multiplier"] <= 1.5
    assert 20 <= modifier["duration_ticks"] <= 12000
    assert modifier["tint"].startswith("#") and len(modifier["tint"]) == 7
    assert len(modifier.get("channels", {})) <= 64
    for channel, value in modifier.get("channels", {}).items():
        assert -0.5 <= value <= 0.5, f"Out-of-range {channel} on {path}"
print(f"PASS: {len(profiles)} profiles, {sum(len(json.loads(p.read_text())['layers']) for p in profiles)} layers, {len(modifiers)} modifiers validated")
# Shader/PostChain file mapping check for Minecraft 1.20.1.
chain_path = ROOT / "shaders/post/chemcraft_perception.json"
chain = json.loads(chain_path.read_text(encoding="utf-8"))
assert "barklychem_stage_a" in chain.get("targets", [])
assert chain.get("passes") == [], "Java composes the ordered shader passes into this owned chain"
program_root = ROOT.parent / "minecraft/shaders/program"
for suffix in ("vortex", "spectral"):
    program_path = program_root / f"barklychem_post_{suffix}.json"
    program = json.loads(program_path.read_text(encoding="utf-8"))
    assert program["vertex"] == "barklychem_post_vertex"
    assert program["fragment"] == f"barklychem_post_{suffix}"
    assert {sampler["name"] for sampler in program["samplers"]} == {"DiffuseSampler"}
    assert (program_root / f"{program['vertex']}.vsh").is_file()
    assert (program_root / f"{program['fragment']}.fsh").is_file()
    uniforms = {uniform["name"] for uniform in program["uniforms"]}
    assert {"ProjMat", "InSize", "OutSize", "Strength", "Motion", "Phase", "Time", "Speed", "Scale", "Tint"} <= uniforms
    shader_text = (program_root / f"{program['fragment']}.fsh").read_text(encoding="utf-8")
    declared = {line.split()[2].rstrip(";") for line in shader_text.splitlines()
                if line.strip().startswith("uniform ") and len(line.split()) >= 3}
    assert {"DiffuseSampler", "InSize", "Time", "Speed", "Scale", "Strength", "Motion", "Phase", "Tint"} <= declared
assert any(layer["type"] == "post_vortex" for layer in json.loads((ROOT / "experience_profiles/echo_prism.json").read_text())["layers"])
print("PASS: post chain, shader program JSON, GLSL file references, uniforms, and fictional demo profile validated")
