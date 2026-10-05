# Pocket Campsite

A portable Minecraft tent with 27 storage slots, a crafting table, and a bedroll. Pack it into one item while keeping its contents.

## Install

Requires Minecraft **26.3**, Fabric Loader **0.19.5 or newer**, Java **25 or newer**, and Fabric API for Minecraft 26.3. Development and checks use Fabric API **0.161.0+26.3**.

Download `pocketcampsite-1.0.0.jar` from the [v1.0.0 release](https://github.com/ahmadzamir1403/pocket-campsite/releases/tag/v1.0.0) and put it alongside Fabric API in the instance's `mods` folder. Multiplayer requires both mods on the client and server. Compatibility with other Minecraft versions is not declared.

## Craft a kit

Use this shaped recipe in a crafting table:

```text
W W W
C B C
H T C
```

`W` = white wool, `C` = copper ingot, `B` = white bed, `H` = chest, `T` = crafting table. It produces one Pocket Campsite Kit, also available in Creative's Tools & Utilities tab.

## Controls

| Action | Control |
| --- | --- |
| Place a campsite | Use the Campsite Kit on a block face. |
| Open storage | Interact with your placed tent. |
| Open a 3 x 3 crafting grid | Select **Craft** above the storage slots. |
| Attempt to sleep | Select **Rest** above the storage slots. |
| Pack the tent and its contents | Select **Pack**, or sneak and interact with the tent. |

Placement requires a solid 3 x 3 foundation, no fluid at the base, and room for the tent. Only its owner can use or pack it, within eight blocks.

Packing creates one Campsite Kit containing the stored items. It goes into a free inventory slot; when no slot is available, it drops at the campsite. Placing restores the contents and consumes the kit, including in Creative. Placed campsites save storage and owner with the world.

Kits and items that cannot fit inside container items are excluded from campsite storage. Kits do not stack. Attacks and explosions cannot damage the tent; use **Pack** to remove it.

## Rest

Rest works only in the Overworld when bed rules allow sleep, normally at night or during a thunderstorm. Nearby monsters, an unsafe foundation, blocked player space, or blocked bedroll positions can prevent rest.

A temporary invisible bed exists while you sleep and is removed after waking or packing. **Rest does not set your respawn point.** Packing is blocked while someone sleeps at the campsite. Multiplayer night skipping follows the server's sleeping rules.

## Build from a fresh clone

On Windows, install a Java 25 JDK with `java`, `javac`, and `jar` on `PATH`. Install Minecraft 26.3 and Fabric API 0.161.0+26.3 in PrismLauncher; launch that instance once to download its libraries. No third-party game or Fabric JARs are stored in this repository.

```powershell
git clone https://github.com/ahmadzamir1403/pocket-campsite.git
cd pocket-campsite
.\build.ps1
.\test.ps1
```

The default PrismLauncher folder is `$env:APPDATA\PrismLauncher`. Pass paths explicitly for another installation:

```powershell
.\build.ps1 -PrismRoot 'D:\PrismLauncher' -FabricApiJar 'D:\mods\fabric-api-0.161.0+26.3.jar'
.\test.ps1 -PrismRoot 'D:\PrismLauncher' -FabricApiJar 'D:\mods\fabric-api-0.161.0+26.3.jar'
```

The build extracts Fabric API's nested modules locally and generates a compile-only Minecraft JAR whose menu accessibility matches Fabric's runtime classtweaker. It compiles source, packages `dist/pocketcampsite-1.0.0.jar`, and runs `tools/VerifyMod.java`. Cached dependencies, build classes, logs, and distribution files are ignored by Git; the downloadable JAR is a release asset.

## Checks and limits

The build and package verifier pass: all 14 classes verify, all three PNGs have the expected dimensions, all seven JSON resources parse, required assets and the recipe are present, and compile-only Minecraft classes are excluded.

`test.ps1` also passes checks against Minecraft's actual container serialization and delivery APIs: sparse 27-slot storage, deployed and packed save round-trips, item count/name/damage, copy isolation, empty inventories, 20 pack/unpack cycles, and Creative/full-inventory delivery behavior. The delivery check uses a world-free player stub and Java's test-only `Unsafe` allocation; it does not launch Minecraft.

**Gameplay has not been tested in-game.** Full-world sleeping, crafting, placement, packing, ownership, and entity lifecycle still need gameplay testing. Checks cover API contracts and packaging rather than a complete running world.

## Art

`art/pocket-campsite-concept.png` and `art/pocket-campsite-materials-generated.png` were created with built-in image generation. Exact prompts are in `art/prompts.txt`. The custom A-frame model's UV layout is in `art/uv-layout.md`.

Prepared textures are included in `src/main/resources`. To regenerate them from the included images:

```powershell
java tools/BuildAssets.java .
```
