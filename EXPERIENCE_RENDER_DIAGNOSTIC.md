# ChemCraft screen-space rendering investigation

## Result in brief

The existing effect path did not contain a shader or a post-processing framebuffer pass. The visible layer was only a few low-alpha `GuiGraphics.fill` calls around the viewport and small marks near the crosshair. It also silently discarded an activation if the profile catalog had not loaded or did not resolve the profile ID. Those are concrete implementation faults consistent with “no visible effect.”

I changed profile lookup to fall back to the exact packaged JSON resource and log missing profiles, and moved the in-world overlay to the Forge `HOTBAR` post stage. The overlay now draws nested, profile-colored edge bands, distinct profile marks, movement cues, trails and a readable active-profile label. It remains a GUI overlay; there is still no GLSL shader or custom framebuffer pipeline.

**I cannot claim the rendering is fixed in-game.** The client could not be launched in this environment, so visual confirmation is still outstanding.

## Complete activation-to-render trace

1. `ExperienceTokenItem.use` runs on the logical server. When the common experience switch is enabled, it applies the existing Minecraft effect and sends `StartExperiencePacket(profileId, durationTicks)` to that same `ServerPlayer`.
2. `ChemLabNetwork` registers packet discriminator 0. `StartExperiencePacket.handle` queues work and dispatches to the client side using `DistExecutor`.
3. `ClientExperienceState.start` normalizes the registry path and looks up the profile. The former implementation returned silently when that lookup failed. It now attempts a direct `ResourceManager` read from `assets/barklychemlab/experience_profiles/<id>.json` and logs a clear error if that exact resource is absent.
4. Profile reload is registered through `RegisterClientReloadListenersEvent`, a Forge mod-bus event in Forge 47.4.28. `prepare` lists JSON resources under `experience_profiles`; `apply` publishes an immutable map and logs the loaded count. Packaged resources use `assets/barklychemlab/experience_profiles/<registry_path>.json`.
5. The profile's base strength and the local `CLIENT_INTENSITY` setting combine with its lifecycle phase to produce each active client layer. The start log records registry ID, duration, profile strength, local intensity, and catalog size.
6. `ExperiencePresentationEvents.overlay` listens to Forge's `RenderGuiOverlayEvent.Post` at `VanillaGuiOverlay.HOTBAR`. It renders after the hotbar stage, avoiding dependency on crosshair visibility. The menu path remains `ScreenEvent.Render.Post`; camera feedback uses `ViewportEvent.ComputeCameraAngles`.
7. The renderer uses `GuiGraphics.fill`/`drawString` and ARGB colors. It writes into Minecraft's current GUI render target via the normal GUI batch. There is no separate render target, GLSL program, `RegisterShadersEvent` handler, shader JSON, shader texture, or framebuffer blit in this project. Therefore there are no shader paths to resolve or shader compilation errors to inspect.

## Root-cause findings

- **No shader implementation existed.** Several prior descriptions referred to chromatic aberration, world warping, blur, and layered perception, but the source only had GUI primitives. There was no shader registration or framebuffer composition implementing those effects.
- **The existing drawing was easy to miss.** The only world hook was the crosshair post event; its border was two pixels wide, while the profile marks were drawn at low alpha. This did not match the expected visible screen-space response.
- **Profile lookup failed closed without diagnostics.** An empty catalog or an ID mismatch made `ClientExperienceState.start` return without rendering, with no actionable client log.
- **Packet routing is client-local by design.** The item sends the profile ID and duration to the item-using player. Profile intensity itself is computed client-side from JSON strength, lifecycle phase and that player's client accessibility setting; it is not broadcast to other players.

## Changes made

- Added profile-resource direct lookup as a fallback and error/info logs around catalog loading and profile activation.
- Added one-time logs confirming the GUI overlay hook is receiving `RenderGuiOverlayEvent.Post` and when a profile reaches its drawing code.
- Switched the screen-space hook from crosshair post to hotbar post so the frame does not depend on crosshair rendering.
- Replaced the nearly invisible 2-pixel border with nested, profile-colored edge bands. Kept the central view and hotbar controls clear; profile-specific marks, trails, HUD feedback and optional movement/camera/audio cues remain separate modules.
- Did not add a shader or change user configuration values. The current overlay is a genuine screen-space GUI effect, not a post-processing effect over the rendered 3D scene.

## API/resource verification

- Inspected the mapped Forge 47.4.28 API: `RenderGuiOverlayEvent.Post` carries a `NamedGuiOverlay`; `VanillaGuiOverlay.HOTBAR.type()` is available; `RegisterClientReloadListenersEvent` implements `IModBusEvent`.
- Confirmed the JAR contains profile JSON resources at `assets/barklychemlab/experience_profiles/`.
- No shader registration, shader resources, custom framebuffer, or shader compilation path exists to validate in this version.

## Build and runtime verification

- `gradle build --no-daemon --offline`: passed after the patch, including Java compilation and Forge reobfuscation.
- `gradle runClient --no-daemon --offline`: blocked before launch at ForgeGradle's `downloadMCMeta` task with `java.net.SocketException: Network is unreachable`. The offline workspace lacks the required Minecraft metadata. No client window or Minecraft log was produced.
- The available desktop app inventory did not include Minecraft. Therefore the following are **not runtime-verified**: packet delivery in a live session, profile catalog reload, the `HOTBAR` event firing, screen visibility/alpha, rendering exceptions, shader compilation (none is implemented), or multiplayer behavior.

The new client logs are intended to establish each runtime stage once the mod is launched: profile reload count, activation and intensity, receipt of the render hook, and profile draw. A live launch is required before calling the effect visually confirmed.

## Follow-up: full-frame renderer revision

This diagnostic records the pre-upgrade GUI-only investigation. The project now includes a separate `PostChain`-based full-frame world-target pass, described in [EXPERIENCE_RENDERING_UPGRADE.md](EXPERIENCE_RENDERING_UPGRADE.md). That newer code has not been compiled or launched in the current environment; treat shader loading and visual output as pending runtime verification.
