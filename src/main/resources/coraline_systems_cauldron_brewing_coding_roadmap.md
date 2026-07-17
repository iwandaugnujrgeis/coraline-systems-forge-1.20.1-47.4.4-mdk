# Cauldron Brewing System — Roadmap & Technical Handoff
*Living document. Supersedes/extends `cauldron_brewing_handoff.md` (the original design brief — read that first for *what* Wine/Kombucha are and why they're asymmetric). This document is the *how*: current implementation state, binding conventions, and what's next.*

---

## How to use this document

1. Read `cauldron_brewing_handoff.md` first for design intent (light thresholds, spoil/stall asymmetry, drink effects).
2. Read Section 1 below for current status before writing any code.
3. Section 2 (codebase conventions) and Section 3 (reference files) are binding — don't invent parallel patterns, and don't guess at Vanilla/Forge signatures not yet reviewed (see Section 3.2).

## 1. Current State

### 1.1 What works (playtested)
- Vanilla `WATER_CAULDRON` (full, `LEVEL == 3`) converts to `BrewingCauldronBlock` on first Mulberries/Tea Leaves addition, via `CauldronInteraction.WATER` entries (not `BREWING` — see 1.2).
- Solid-ingredient level (1–5, `BrewingCauldronBlock.LEVEL`) tracks Mulberries/Tea Leaves added, capped correctly.
- Branch locking works: a cauldron committed to Mulberries (Wine) rejects Tea Leaves and vice versa, and rejects the wrong culture item (Yeast on a Tea-committed cauldron, Dregs on a Mulberries-committed one) — via `BrewingCauldronBlockEntity.impliedCulture`, set once at first solid addition, cleared only by `reset()`.
- Water Bucket/Lava Bucket/Powder Snow Bucket no longer revert a converted `BrewingCauldronBlock` back to Vanilla cauldron — explicitly rejected via a shared `REJECT` constant in `BrewingCauldronInteractions`, registered after removing the inherited `addDefaultInteractions(BREWING)` call.
- Universal collection (drain): Glass Bottle/Bucket pull Tea, Mulberry Juice, Kombucha, Wine, or Dregs from a `BrewingCauldronBlock`, draining `waterLevel` (NOT the solid `LEVEL`) and stamping strength from the solid `LEVEL` without modifying it.
- Universal fill (pour back in): filled Bottle/Bucket of any of the 5 substances poured onto an *empty* Vanilla `CAULDRON` converts it into a seeded `BrewingCauldronBlock` (see `CauldronInteraction.EMPTY` registrations in `registerFillInteractions()`).
- All 10 drink items (5 substances × Bottle/Bucket) share `AbstractCoralineDrinkItem`; strength tooltip is opt-in via constructor (Wine/Tea/Mulberry Juice have it, Kombucha/Dregs don't).
- BlockEntity client sync.

### 1.2 Standing architectural rule — don't relearn this
A plain, empty Vanilla `Blocks.CAULDRON` dispatches `use()` through `CauldronInteraction.EMPTY`. A full Vanilla `WATER_CAULDRON` dispatches through `CauldronInteraction.WATER`. Only an *already-converted* `BrewingCauldronBlock` dispatches through `BrewingCauldronInteractions.BREWING`. Every "why isn't my interaction firing" bug so far (solid-adding conversion, the fill path) traced back to registering on the wrong map for the block's *current* type. Register solid-conversion entries on `WATER`, fill-from-empty entries on `EMPTY`, and everything else (subsequent solids, cultures, collection) on `BREWING`.

### 1.3 Not implemented yet
- **Session 2** (we initially planned the work out as "Sessions" — keeping it this way): Random-tick brew progress, light-level checks, Wine spoil / Kombucha stall/complete transitions (Section 2/design-doc scope — no code exists).
- **Session 3**: Sound/particle polish beyond reused Vanilla Bottle/Bucket sounds.
- **Session 4**: All drink *effects* (`applyDrinkEffect` bodies are TODO stubs in all 10 item classes).

---

## 2. Codebase Conventions (binding — don't deviate without stated reason)

Derived from `CentrifugeBlockEntity.java`, `CentrifugeBlock.java`, `CoralineItems.java`.

### 2.1 Item registration (`CoralineItems.java`)
```java
public static final RegistryObject<Item> ORB = REGISTRY.register(
        "orb", () -> new OrbItem(new Item.Properties().stacksTo(16)));

private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> supplier) {
    return REGISTRY.register(name, supplier);
}
```
Plain items go through `CoralineItems` directly. Block-linked `BlockItem`s go through `CoralineBlocks`' own `register(...)` overloads instead.

### 2.2 Block registration (`CoralineBlocks.java`)
```java
public static final RegistryObject<Block> CENTRIFUGE = register("centrifuge",
        () -> new CentrifugeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).requiresCorrectToolForDrops()
                .strength(3.5f, 6.0f).sound(SoundType.METAL)
                .lightLevel(state -> /* derive from blockstate */ 0)));
```
`lightLevel` commonly derives from blockstate properties via lambda. `register(name, supplier[, itemProperties])` handles block+item together; `registerWithoutItem` skips the auto `BlockItem`.

### 2.3 BlockEntity pattern
- Constructor: `(BlockPos pos, BlockState state)` → `super(CoralineBlockEntities.XXX.get(), pos, state)`.
- NBT: override `load`/`saveAdditional`, call `super` first in both.
- **Client sync**: override `getUpdatePacket()`/`getUpdateTag()`, and every field-mutating setter must trigger a `sendBlockUpdated` call. This was missing from Session 1 onward and is now the template for any future BE in this system.
- Random-tick-driven progress has no in-house precedent (Centrifuge uses an externally-owned `TimeAccelerationManager` session model, not per-block ticking) — Session 2 builds this from Vanilla `Block.randomTick`/crop-style conventions instead (see Section 3.2).

### 2.4 Block pattern
- `AbstractCauldronBlock` does **not** implement `EntityBlock` — our `BrewingCauldronBlock extends AbstractCauldronBlock implements EntityBlock` directly, confirmed working.
- Cauldron interactions dispatch via `Map<Item, CauldronInteraction>` per Section 1.2's rule, not via a Block-level `use()` override.
- `animateTick` (client-side, cosmetic) is the right hook for ambient bubbling/fizzing once that's built (Session 3).

---

## 3. Reference Files

### 3.1 Already reviewed in full, available in project context
- Vanilla: `AbstractCauldronBlock.java`, `CauldronBlock.java`, `LayeredCauldronBlock.java`, `CauldronInteraction.java`, `Block.java` (partial), `BlockBehaviour.java`, `EntityBlock.java`, `ItemStack.java`, `ItemUtils.java`, `InteractionResult.java`, `InteractionResultHolder.java`, `Player.java`, `PlayerInteractEvent.java`.
- Forge fluid-capability stack (`FluidType`, `IFluidHandler`, `FluidBucketWrapper`, etc.), `PotionItem.java`, `MilkBucketItem.java`, `BucketItem.java` — reviewed while scoping drink items; **deliberately not used** (no real `Fluid` is registered for any of the 5 substances — see 3.3). `PotionItem`/`MilkBucketItem` are the actual templates `AbstractCoralineDrinkItem` is built from.
- This mod: `CentrifugeBlockEntity.java`, `CentrifugeBlock.java`, `CoralineItems.java`, `CoralineBlocks.java`, `CoralineBlockEntities.java`, `CoralineSystems.java`, `BrewingCauldronBlock.java`, `BrewingCauldronBlockEntity.java`, `CultureType.java`, `BrewingCauldronInteractions.java`, `AbstractCoralineDrinkItem.java` + all 10 drink item classes, `CoralineFluidUtils.java`.

### 3.2 Needed but not yet reviewed — request before use
- `EntityBlock` ticker-validation pattern (`BlockEntityTicker<T>`, `createTickerHelper`) — needed for Session 2's tick wiring if a client-side ticker is ever added (progress itself is random-tick, not ticker-driven).
- Vanilla `CropBlock.java` (or `SweetBerryBushBlock`) — the actual precedent for Session 2's random-tick progress math. Nothing in-house covers this pattern. **Request at the start of Session 2.**

### 3.3 Deliberately out of scope, permanently
None of Wine/Kombucha/Tea/Dregs/Mulberry Juice need to be placeable or have real `Fluid`/`FluidType` registrations — they only need to be collectible-from-cauldron and drinkable. The entire Forge fluid-capability stack is out of scope for this reason, not deferred. Bucket-form items also skip `initCapabilities`/`FluidBucketWrapper` (no backing `Fluid` to wrap — would be a likely NPE source). Crafting-grid empty-container-remainder behavior comes for free from `Item.Properties.craftRemainder(...)`, independent of any capability.

---

## 4. Design Numbers Cross-Reference

Authority remains `cauldron_brewing_handoff.md`. Carry into Session 2 unchanged unless playtesting says otherwise:
- **Wine**: darkness (light 0–6) required; light ≥7 spoils the batch into Dregs (not a stall — hard failure). Target ~24,000 ticks (~1 MC day) at correct light — placeholder pending tuning.
- **Kombucha**: base 12,000 ticks at light 7–15; graduated punishment ticks at light 1–6 (5000/5000/4000/3000/2000/1000); indefinite stall + zero feedback at light 0.

---

## 5. Parked / Explicitly Deferred (not guessed at, not implemented)

- **Partial-water brew start**: `convertToBrewingCauldron` still hard-requires a full water cauldron (`LEVEL == 3`) before accepting the first solid. Relaxing this (starting a brew with less water, capping eventual yield accordingly) is a real future idea, not implemented.
- **Re-pouring a *finished* Wine/Kombucha**: currently seeds `impliedCulture`/`LEVEL`/`waterLevel` on pour-back but has no `brewProgress`/finished-state to restore, since that state doesn't exist yet. Whether this should even be legal is Session 2's call once finished-state exists.
- **`Stats.FILL_CAULDRON` vs `Stats.USE_CAULDRON`** for the fill interactions — chosen for consistency with `emptyBucket`'s own stat usage, not deeply verified against Vanilla's own Bottle-of-water convention (which uses `USE_CAULDRON`). Not gameplay-blocking; revisit if stat tracking correctness matters later.
- **Wine spoil / Kombucha completion collection target**: Section 2 (design doc scope) needs to decide whether spoiled/finished contents sit in the cauldron as directly-collectible via the existing universal drain path, or need a transient BE state distinct from "empty, ready for new solid." Flag explicitly at the start of that work, don't assume.
- Tea's drink *effect*, Wine's level-tied drink effect, Kombucha's respawn-set effect, Dregs' drinkability — all undesigned, all TODO stubs in their respective item classes. Not guessed at.
