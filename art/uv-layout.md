# Pocket Campsite atlas

Texture: `assets/pocketcampsite/textures/entity/pocket_campsite.png`, 256 x 256 pixels.

Four 128 x 128 material quadrants: canvas upper-left; wood upper-right; copper lower-left; teal bedding lower-right. Tile texture patterns need no face-specific art.

| Model part | UV origin | Cuboid dimensions | UV envelope |
| --- | --- | --- | --- |
| Canvas slopes | 0, 0 | 1 x 36 x 36 | 74 x 72 |
| Floor halves | 128, 32 | 20 x 1 x 36 | 112 x 37 |
| Chest | 128, 0 | 12 x 9 x 9 | 42 x 18 |
| Workbench | 128, 80 | 12 x 7 x 10 | 44 x 17 |
| Poles | 0, 128 | 2 x 30 x 2 | 8 x 32 |
| Ridge | 0, 168 | 2 x 2 x 38 | 80 x 40 |
| Latch | 20, 128 | 2 x 3 x 1 | 6 x 4 |
| Bedroll | 128, 128 | 12 x 3 x 28 | 80 x 31 |
| Pillow | 128, 164 | 12 x 2 x 5 | 34 x 7 |

Floor at y=0. Ridge at y=-30 in model space; renderer flips model Y to world up. Roof panels rotated +/-0.58 radians form an open-ended A-frame; camp floor, bedroll, chest, and workbench visible inside. Width2.5 blocks, length2.25 blocks, height1.94 blocks.
