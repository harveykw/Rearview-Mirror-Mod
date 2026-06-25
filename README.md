
# Rearview Mirror Mod

A client-only Fabric mod for minecraft java 1.21.11 that adds a configurable HUD rearview mirror box.

The mirror works by doing an extra world render pass into a small offscreen framebuffer, then drawing that rear view into a HUD box. 

![Example demo](readmeImages/example%202.gif)

## Mod Platforms:
https://modrinth.com/project/gTRtEVEQ


## Controls

Default keys:

- `R` - toggle mirror HUD
- `O` - open the config screen

Inside the config screen:

- Use the Mirror FOV slider to change the rear camera angle
- Drag and resize the mirror as needed
- Edit the framerate of the mirror for performance.


The config is saved to:

```text
.minecraft/config/rearviewmirror.json
```

### Performance

Because this mod works by calling an extra render before the main render for every frame,
it adds a constant multiplier on the complexity. I've tried lowering the resolution and animations
for the rendered image, however it appears to be capped by the overhead of rendering a frame.
As such the only meaningful performance improvement is by adjusting the framerate of the mirror, which
you can do in the in-game config screen.