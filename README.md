
# Rearview Mirror Mod — For Fabric 1.21.11

A client-only Fabric mod that adds a configurable HUD rearview mirror box.

The mirror works by doing an extra world render pass into a small offscreen framebuffer, then drawing that rear view into a HUD box. 

## Controls

Default keys:

- `R` — toggle mirror HUD
- `O` — open the config screen

Inside the config screen:

- Use the Mirror FOV slider to change the rear camera angle
- Drag and resize the mirror as needed


The config is saved to:

```text
.minecraft/config/rearviewmirror.json
```

