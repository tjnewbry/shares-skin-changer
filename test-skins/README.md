# Test skins

Skins for manually checking arm width detection. Pick them with **Choose File...** in the mod.

| File | Arm width | Expected result |
|---|---|---|
| `slim-3px.png` | 3px (slim) | "3px / Slim arms detected." |
| `classic-4px.png` | 4px (classic) | "4px / Classic arms detected." |

Detection checks four pixel pairs that only 4px arms use (right arm, right hand, left arm, left hand). If at least two pairs are fully transparent, the skin is treated as slim.
