# Crow Redesign — Decisions and Implementation Plan

Status: decisions locked on 2026-10-20. This document supersedes the earlier decision notes in this file.

Behavior IDs B01–B38 refer to `CROW_BEHAVIORS.md`. B39–B40 are new behaviors added by this redesign. The shoulder-sitting feature (B37/B38) is original work and is retained.

---

## 1. Entity snapshot

| Property | Decision |
| --- | --- |
| Base class | Keep `ShoulderRidingEntity`. |
| Category / size | Keep `CREATURE`, 0.45 × 0.45, tracking 10. |
| Attributes | Keep attack damage 1 and speed 0.2. Reduce max health from 8 to **6**. |
| Spawn placement | Keep on-ground, `MOTION_BLOCKING_NO_LEAVES`, light > 8. |
| Biome spawns | Keep the existing Forest / Taiga / plains / Vantage distribution. |
| Immunities | **Remove** `IN_WALL` and `CACTUS` immunity. Keep `FALL` immunity and the empty fall-damage handling. |
| Pathfinding malus | Keep fire, water, and water-border malus. **Remove** cocoa and fence malus. |
| Persisted state | Keep `Flying`, `Owner`, and vanilla `Sitting`. Remove `Command`, `CrowSitting`, and perch data. Migrate old saves. |
| Synced state | Keep `FLYING` and `ATTACK_TICK`. Use vanilla sitting pose. Remove `COMMAND`, custom `SITTING`, and `PERCH_POS`. |
| Ambient sound interval | Keep 60 ticks. |

**Code-reality fixes:**

- `canTrample` no longer exists in the current `Entity` API; delete the dead method.
- `causeFallDamage(float, float)` does not match the current `causeFallDamage(double, float, DamageSource)` signature; delete it or implement the correct signature to return `false`.
- `checkFallDamage` stays empty, and `isInvulnerableTo(FALL)` stays.
- `setOrderedToSit(boolean)` must call `super.setOrderedToSit(sit)` and `setInSittingPose(sit)`. The current override never sets the inherited `orderedToSit` flag, so `SitWhenOrderedToGoal` never actually engages.

---

## 2. Behavior decisions

### A. Locomotion and flight

| ID | Behavior | Decision |
| --- | --- | --- |
| B01 | Dual ground/flight navigator | **Keep.** |
| B02 | Arcade flight physics | **Keep.** |
| B03 | Auto-landing | **Keep.** |
| B04 | Stay/sit freezes movement | **Keep**, with the `setOrderedToSit`/sitting-pose fix above. |
| B05 | Environmental immunity and path preferences | Remove in-wall and cactus immunity; remove cocoa and fence malus; keep fall immunity. Update/remove the dead trample/fall methods. |
| B06 | Idle wander (walk or fly) | **Keep for wild crows only.** Tamed crows follow or sit; there is no wander order. Tamed crows fall back to idle only when their owner is unavailable. |
| B07 | Radial flight destination search | **Keep.** |
| B08 | Ground destination search | **Rewrite.** The existing implementation computes a radial point and then overwrites it with the crow's own position. Replace it with a correct landing search. |
| B09 | Water avoidance | **Keep.** |
| B10 | Look behaviors | **Keep.** |

### B. Taming, feeding, breeding

| ID | Behavior | Decision |
| --- | --- | --- |
| B11 | Taming via dropped seeds | **Rework.** No dropped-item taming. Use a parrot-style right-click feed with `#lamplight:crow_tameables`, but keep the crow's 60-tick eat delay. Taming chance is **30% per eaten seed** (higher than vanilla parrot's 10%, because of the wait). Implement directly in `mobInteract`; do not change `isFood`. |
| B12 | Hand-feeding and self-healing | **Keep**, driven by the new manual item interaction. Edible items are eaten after 60 ticks and heal 4. Wild crows can be fed non-taming food; no tame roll. |
| B13 | Breeding | **Keep.** `isFood` remains `#crow_breedables` **and** tamed, so wild crows do not breed. Breeding uses vanilla `Animal.mobInteract` only when the crow is adult, tamed, not sitting, at full health, and not already in love. |
| B14 | Held item hygiene | **Remove drop-on-damage.** Keep right-click removal. Add a guaranteed main-hand drop on death, since damage no longer drops it. |
| B15 | Voice | **Keep.** Sounds may be replaced later. |

### C. Orders

| ID | Behavior | Decision |
| --- | --- | --- |
| B16 | Four-state command cycle | **Replace entirely with two states: FOLLOW and STAY.** No overlay messages. The visual difference is the sitting animation. FOLLOW is the default on taming. Remove `COMMAND`; migrate old values. |
| B17 | Command 1: follow owner | **Keep as the FOLLOW state.** |
| B18 | Command 3: gather/hoard | **Drop.** |
| B19 | Gathering disables idle flight | **Drop.** |

### D. Owner following

| ID | Behavior | Decision |
| --- | --- | --- |
| B20 | Owner orbit | **Keep.** Make the follow goal active whenever the crow is tamed and following, not only beyond a minimum distance. |
| B21 | Close-range shoulder approach | **Rewrite.** Approach to about 2.5 blocks, stop, and let the shoulder goal mount. Do not keep flying into the owner. |
| B22 | Long-range teleport | **Keep.** |

### E. Items

| ID | Behavior | Decision |
| --- | --- | --- |
| B23 | Item targeting | **Drop autonomous item AI.** No general dropped-item targeting. Replaced by B39 (manual interaction) and B40 (wild seed foraging). |
| B24 | Item approach and pickup | **Drop** for tamed crows and general items. Wild crows may pick up only the seeds they forage (B40). |
| B25 | Peck animation hook | **Keep.** Held-item damage is handled by B32; do not add a separate flat damage bonus. |
| B26 | Chest deposit through item frames | **Drop.** |
| B39 | Manual item handling | **New.** Right-click insert/remove/swap, with the precedence in §3.1. |
| B40 | Wild seed foraging | **New.** Grass/fern → seed item → pick up → eat, as described in §3.4. |

### F. Wild-only behaviors

| ID | Behavior | Decision |
| --- | --- | --- |
| B27 | Crop raiding | **Drop.** |
| B28 | Pumpkin fear | **Drop.** |
| B29 | Startle/scatter | **Keep.** While a player is sneaking and holding `#lamplight:crow_foodstuffs` in either hand, that player is excluded from the crow's startle selector. Other threats still scatter the crow. |
| B30 | Home-block healing and perch memory | **Drop.** |

### G. Combat

| ID | Behavior | Decision |
| --- | --- | --- |
| B31 | Retaliation and owner defense | **Keep.** |
| B32 | Aerial circling melee | **Keep the circling and dive.** Remove the hardcoded undead bonus and generic damage. Attack through vanilla `Mob.doHurtTarget` so the main-hand/mouth item supplies base damage, attributes, enchantments, and special effects such as the mace slam. |
| B33 | Tamed damage reduction | **Drop.** |
| B34 | Injury response | **Keep.** Being hurt cancels STAY. |

### H. Spawning / persistence

| ID | Behavior | Decision |
| --- | --- | --- |
| B35 | Spawn and biome distribution | **Keep.** |
| B36 | Save/load | **Keep, pruned.** Save `Flying`, `Owner`, and vanilla `Sitting`. Remove `Command`, `CrowSitting`, and perch data. Migrate old saves as described in §3.8. |

### I. Shoulder sitting (original work)

| ID | Behavior | Decision |
| --- | --- | --- |
| B37 | Shoulder mount | **Keep the feature and original code.** Change only the unreliable trigger: mount on proximity + line of sight instead of bounding-box intersection. |
| B38 | Shoulder sync and rendering | **Keep.** No licensing concern. |

---

## 3. Planned systems

### 3.1 Interaction contract (answers “come up with a system”)

No autonomous item targeting. The player interacts with the crow directly. One item occupies the beak at a time, regardless of stack size.

#### Precedence

Before the crow-specific table, give `ItemStack.interactLivingEntity(player, crow, hand)` a chance to handle the click. If it returns `SUCCESS`, use that result. This preserves name tags, leads, and modded entity interactions without letting them bypass owner-only crow commands. Do not call `super.mobInteract` unconditionally; it would run vanilla breeding before the crow-specific logic.

Handle the first matching row:

| # | Crow | Hand | Beak | Sneaking | Result |
| --- | --- | --- | --- | --- | --- |
| 1 | Tamed, not owner | Any | Any | Any | No interaction. |
| 2 | Tamed | Empty | Any | Yes | Toggle FOLLOW / STAY. |
| 3 | Tamed | Empty | Full | No | Take the beak item. |
| 4 | Tamed | Empty | Empty | No | Toggle FOLLOW / STAY. |
| 5 | Tamed, adult, full health, not in love, not sitting | Breedable item | Any | Any | Vanilla breed interaction; item is not consumed and the beak is unchanged. |
| 6 | Tamed, baby | Non-food item | Any | Any | No interaction. |
| 7 | Tamed | Any other item | Any | Any | Insert one item. If the beak is full, drop the old item and swap. |
| 8 | Wild | Tameable item | Empty | Any | Insert one seed, set the feeder UUID, start the 60-tick eat. |
| 9 | Wild | Other foodstuff | Empty | Any | Insert one food item, start the 60-tick eat; no tame roll. |
| 10 | Wild | Empty, non-food, or full beak | Any | Any | No interaction. |

Additional rules:

- “Take” gives the item to the player, or drops it at the crow's feet if the inventory is full. Because sneak + empty hand always toggles the command, stop sneaking if you want to take an item from a full beak.
- “Insert” consumes one item from the player's stack (components preserved) and places one item in the main hand.
- “Swap” drops the old beak item at the crow's feet and inserts the new one, resetting the eat timer.
- A food item is eaten only when the crow is wild or injured. A full-health tamed crow holds the food until it is hurt.
- A non-food item never gets eaten.
- Tamed crows can be fed and commanded while sitting.
- Shoulder-mounted crows use vanilla shoulder behavior; don't add item interaction while mounted.
- Only the owner can command, feed, insert into, or take from a tamed crow.
- Taming still requires `#lamplight:crow_tameables`; other food only heals.

This resolves the old right-click conflict without needing a new keybind.

### 3.2 Command system

- Two states only:
  - **FOLLOW** — the crow follows/orbits the owner and can shoulder-mount. This is the default after taming.
  - **STAY** — ordered to sit; it stops navigation, clears flight, and plays the sit animation.
- No wander command. Wild crows wander; tamed crows follow or sit.
- No overlay messages. The sit animation is the visual state. Remove the `command_3` and `all.command_*` translation keys.
- Toggle controls are in §3.1. The convenient form is: empty hand toggles when the beak is empty; sneak + empty hand toggles even when the beak is full.
- `isFollowing()` = tamed and not ordered to sit.
- `isSitting()` = `isOrderedToSit() || isInSittingPose()`.
- `setOrderedToSit` calls `super`, sets the synced sitting pose, stops navigation, and sets flying false.
- If the owner is offline, in another dimension, or a spectator, a tamed crow falls back to wild-style idle wandering rather than standing frozen.
- Tamed crows do not use `WalkIdleGoal` while their owner is available.
- The follow goal is active whenever the crow is following, including when the owner is close; it orbits or approaches rather than stopping at a minimum distance.

### 3.3 Taming, feeding, breeding

- Taming interaction:
  1. Wild crow, empty beak, player right-clicks with a `#crow_tameables` item.
  2. Consume one item; put it in the beak; record the feeder UUID; reset `heldItemTime`.
  3. After 60 ticks, if the item is still there and edible: heal 4, play the eat sound, apply any crafting remainder, shrink the item.
  4. If the crow is wild and the feeder is known and the item is tameable: roll **30%**.
     - Success: tame to the feeder, set FOLLOW, trigger `TAME_ANIMAL` if the feeder is online, show hearts.
     - Failure: show smoke; remain wild; clear the feeder.
  5. If the item is removed before 60 ticks, no roll and the item is returned to the player.
- No taming from dropped or foraged seeds.
- If the feeder is offline at eat time, do not roll; keep it simple and avoid offline-owner edge cases.
- Feeding a tamed crow:
  - Insert one edible item; after 60 ticks it heals 4 if the crow is injured.
  - If the crow is at full health, the food stays in the beak until it is hurt.
  - Pumpkin seeds are both food and breedable; if the crow is adult, full health, and not in love, the breed interaction takes precedence. If it is hurt, feeding/healing takes precedence.
- Breeding:
  - Keep `isFood = #crow_breedables && isTame()`.
  - Use vanilla `Animal.mobInteract` for the breed case only.
  - Do not let wild crows breed.
  - Do not breed while sitting.
  - No item is consumed by the breed interaction.

### 3.4 Wild seed foraging (B40)

A single wild-only goal handles the whole loop. No general item-targeting goal is reintroduced.

- Eligibility: wild, adult, not tamed, not sitting, no live target, no item in beak, `mobGriefing` enabled, cooldown expired, and either injured or a low idle chance.
- Search for the nearest block in the new `#lamplight:crow_forageables` tag within 16 blocks horizontally and ±4 vertically. Start with:
  - `minecraft:short_grass`
  - `minecraft:tall_grass`
  - `minecraft:fern`
  - `minecraft:large_fern`
- Move to the plant; peck three times using the existing `peck()` animation.
- On completion:
  - Destroy the plant without normal drops. Handle both halves of tall grass/large fern.
  - With a 50% chance, spawn one item from `#lamplight:crow_foraged_seeds` at the plant position, with a small upward velocity and a 10-tick pickup delay.
  - Seed pool starts as wheat 50%, beetroot 20%, melon 15%, pumpkin 15%. A datapack tag can extend this later.
- If a seed was spawned, the crow moves to that exact item entity (tracked by UUID), picks up one, and eats it after 60 ticks for 4 HP. It never targets other dropped items, including player-dropped seeds.
- If the seed is taken by a player or another mob, the goal ends.
- Cooldown: 600–1200 ticks after an attempt, whether or not a seed was produced.
- Tamed crows never forage and never pick up items automatically.
- No crop raiding and no farm griefing; only the grass/fern tag.

This preserves a visible foraging loop while keeping the only item-form pickup in the game restricted to seeds.

### 3.5 Held-item combat (B25 + B32)

The mouth is the main hand. `CrowEntity` already uses `InteractionHand.MAIN_HAND`; foxes likewise store their mouth item in `EquipmentSlot.MAINHAND`. Use that vanilla route.

- Replace the manual `target.hurt(target.damageSources().generic(), ...)` call with `crow.doHurtTarget(serverLevel, target)`.
- Do not add a separate flat damage value. `Mob.doHurtTarget` builds the attack from:
  - `Attributes.ATTACK_DAMAGE` (the crow's natural 1 plus main-hand item modifiers)
  - `getWeaponItem()` (the mouth/main-hand item)
  - `ItemStack.getDamageSource(crow)`
  - `EnchantmentHelper.modifyDamage(...)` (Sharpness, Smite, etc.)
  - `Item.getAttackDamageBonus(...)` (mace smash bonus, etc.)
  - `ItemStack.hurtEnemy(...)` (item on-hit behavior, durability, mace)
  - `EnchantmentHelper.doPostAttackEffects(...)` (Fire Aspect, Knockback, etc.)
  - normal knockback and attack sound
- Remove the hardcoded 4-damage-undead / 1-damage-normal branch.
- Keep the orbit/dive behavior and the `peck()` animation.
- If the crow has no item, the natural attack damage remains 1. With an item, the item's modifiers are part of the base attack value, exactly as they are for players and weapon-holding mobs; no extra crow-specific bonus is layered on top.
- **Mace caveat:** the mace slam requires fall distance. A no-gravity flying crow may not qualify even though the item hooks are routed correctly. If mace slams are desired in flight, track fall distance explicitly or accept that they only work on grounded/diving attacks.
- Because damage no longer drops the held item, set the main-hand drop chance to 1.0 (or override death loot) so the item still drops on death.

### 3.6 Following and shoulder mounting (B20–B22, B37)

- `CrowFollowOwnerGoal` is active whenever the crow is tamed and following. Remove the minimum-distance gate; the goal itself decides between orbit and approach.
- Distance handling:
  - More than 12 blocks: keep the existing teleport attempt (B22), every 10 ticks.
  - 3–12 blocks: orbit the owner at radius 1–3 and height 0 above the owner's feet (tune to 0–1 if desired).
  - Less than 3 blocks: approach to about 2.5 blocks and stop.
- `CrowShoulderGoal`:
  - Keep the original ownership/eligibility checks: owner is a `ServerPlayer`, not spectator, not creative-flying, not in water or powder snow; crow is following, not sitting, not leashed, and `canSitOnShoulder()` is true.
  - Add: don't mount while the crow has a live target.
  - Replace the bounding-box intersection trigger with `distanceTo(owner) <= 2.5` plus line of sight.
  - On success, call the existing `setEntityOnShoulder(owner)`.
- If the owner is ineligible for shoulder mounting, the crow orbits instead; it does not ram the player.
- Keep B22's teleport rules and leaf-friendly teleport flag.
- Keep B37/B38's shoulder sync and shoulder rendering unchanged.

### 3.7 Landing rewrite (B08)

Replace `getBlockGrounding` with a correct search:

1. Pick a horizontal point within roughly 4–12 blocks of the crow, using the existing random-angle style.
2. Find the ground with `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES` (or an equivalent downward scan from the crow's Y).
3. Reject the point if the ground is at the world minimum, is fluid/lava, or has no collision.
4. Return the **air/plant space above the ground**, not the ground block itself.
5. Reject the point if the line of sight is blocked.
6. If no point is found, keep flying; never fall back to the crow's own position.

`WalkIdleGoal` should use this landing search after the initial flight period and force a landing attempt after the long-flight timeout instead of switching to ground navigation while airborne.

### 3.8 State, persistence, and migration

- Synced:
  - `FLYING`
  - `ATTACK_TICK`
  - Vanilla sitting pose via `TamableAnimal`'s synced flags.
  - Remove `COMMAND`, custom `SITTING`, and `PERCH_POS`.
- Saved:
  - `Flying`
  - `Owner`
  - Vanilla `Sitting`
  - Remove `Command`, `CrowSitting`, `PerchX/Y/Z`.
- Migration on load:
  - If old `CrowSitting` is present, use it for STAY.
  - Else if old `Command` is present: 2 or 3 → STAY; 0 or 1 → FOLLOW.
  - Otherwise use vanilla `Sitting`.
  - Ignore and discard old perch coordinates.
- Held item:
  - Persist via the main-hand equipment slot.
  - `setDropChance(EquipmentSlot.MAINHAND, 1.0F)` so death drops it.
  - No drop on ordinary damage.
- Transient:
  - `heldItemTime`
  - feeder UUID
  - forage cooldown and tracked seed UUID

### 3.9 Goal list and cleanup

Target goal selector after the redesign:

| Priority | Goal |
| --- | --- |
| 0 | `FloatGoal` |
| 1 | `SitWhenOrderedToGoal` |
| 2 | `CrowMeleeGoal` |
| 2 | `CrowShoulderGoal` |
| 3 | `CrowFollowOwnerGoal` |
| 4 | `ScatterGoal` (wild only) |
| 5 | `BreedGoal` |
| 6 | `CrowForageGoal` (wild only) |
| 7 | `WalkIdleGoal` (wild only, plus tamed owner-unavailable fallback) |
| 8 | `LookAtPlayerGoal` (players, 6) |
| 9 | `LookAtPlayerGoal` (`PathfinderMob`, 6) |
| 10 | `RandomLookAroundGoal` |

Target selector:

| Priority | Goal |
| --- | --- |
| 1 | `OwnerHurtByTargetGoal` |
| 2 | `OwnerHurtTargetGoal` |
| 3 | `HurtByTargetGoal` (ignores player damage, alerts others) |

Delete or prune:

- `TargetItemsGoal`, `TargetsDroppedItems`, `CrowTargetItemsGoal`
- `DepositChestsGoal`, `CrowCircleCropsGoal`, `AvoidPumpkinsGoal`
- `gatheringVec`, `gatheringClockwise`, `aiItemFlag`, `aiItemFrameFlag`
- `seedThrowerId` (replace with the feeder UUID used by taming)
- `fleePumpkinFlag`, perch fields, `getPerchPos`/`setPerchPos`
- `COMMAND`, `SITTING`, `PERCH_POS` synced data
- `Command`, `CrowSitting`, `Perch*` saved data
- `insertInto`, and the `Container` / `WorldlyContainer` / `ItemFrame` / `Direction` / `BlockEntity` imports
- `CropBlock`, `GameRules`, `EntityTypeTags`, and the old melee damage imports (keep `DamageTypes` for the remaining immunity checks)
- The `command_*` lang keys

Tags:

- Keep: `crow_breedables`, `crow_foodstuffs`, `crow_tameables`, `scatters_crows`
- Delete: `crow_foodblocks`, `crow_fears`, `crow_home_blocks`
- Add: `crow_forageables`, `crow_foraged_seeds`

Keep:

- `CrowMaths` and `CrowBlockPos` for the owner orbit and melee orbit
- `CrowShoulderData`, `PlayerShoulderCrowMixin`, `ServerPlayerShoulderMixin`
- `CrowOnShoulderLayer`, `CrowShoulderRenderState`
- `CrowHeldItemLayer`, `CrowRenderState`, `CrowRenderer`, `CrowModel`

---

## 4. Tuning constants

| Constant | Value |
| --- | --- |
| Max health | 6 |
| Attack damage | 1 (unarmed base; item modifiers add) |
| Movement speed | 0.2 |
| Taming chance | 0.30 per eaten tameable |
| Eat delay | 60 ticks |
| Feed heal | 4 HP |
| Scatter detection | Existing 5×5×5 box; player ignored while sneaking + holding `#crow_foodstuffs` (effective radius 0; tune to 1 block if taming is too easy) |
| Follow orbit | Radius 1–3; height 0 (tune 0–1) |
| Teleport distance | 12 blocks |
| Shoulder mount distance | 2.5 blocks with line of sight |
| Landing search | 4–12 blocks horizontally |
| Forage search | 16 blocks horizontal, ±4 vertical |
| Forage seed chance | 0.50 |
| Forage cooldown | 600–1200 ticks |
| Foraged seed weights | Wheat 50%, beetroot 20%, melon 15%, pumpkin 15% |

These are safe to tune without changing the design.

---

## 5. Recommended implementation order

1. **State and commands:** remove `COMMAND`/`SITTING`/perch data; fix `setOrderedToSit`; add save migration; make tamed crows follow/sit only.
2. **Interaction and feeding:** rewrite `mobInteract` with the §3.1 precedence; implement tame/feed/breed; add death drop.
3. **Combat:** switch `CrowMeleeGoal` to `doHurtTarget`; remove the undead branch; verify held-item rendering and durability.
4. **Following, shoulder, landing:** rewrite the follow approach and shoulder trigger; fix the landing search; keep teleport.
5. **Wild foraging:** add the two tags and the new goal; remove the old item-target goals.
6. **Cleanup:** delete unused goals/tags/lang keys, update `MenagerieTags`, and prune imports and synced/saved fields.

No blocking design questions remain. The only open choices are the tuning constants in §4.
