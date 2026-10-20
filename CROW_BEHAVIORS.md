# Crow Behavior Audit

Working document for redesigning Menagerie's crow without reusing Alex's Mobs code or assets.

**Scope:** Every player-visible or AI-visible behavior currently implemented on the crow, with source references, so each can be marked keep / rewrite / remove. The shoulder-sitting feature is included only for completeness because it is stated to be original work, not ported.

**Source inventory:**

| Area | Files |
| --- | --- |
| Entity, goals, items | `src/main/java/com/aura/lamplight/menagerie/crow/CrowEntity.java` |
| Circle-crop goal | `.../crow/ai/CrowCircleCropsGoal.java` |
| Follow-owner goal | `.../crow/ai/CrowFollowOwnerGoal.java` |
| Melee goal | `.../crow/ai/CrowMeleeGoal.java` |
| Shoulder goal (original) | `.../crow/ai/CrowShoulderGoal.java` |
| Item targeting | `.../crow/ai/TargetItemsGoal.java`, `.../crow/TargetsDroppedItems.java` |
| Flight movement | `.../crow/ai/FlightMoveControl.java`, `.../crow/ai/DirectNavigator.java` |
| Orbit/circle math | `.../crow/CrowMaths.java`, `.../crow/CrowBlockPos.java` |
| Registry / spawns | `.../menagerie/registry/MenagerieEntities.java`, `.../menagerie/Menagerie.java` |
| Tags | `src/main/resources/data/lamplight/tags/{item,block,entity_type}/crow_*.json`, `scatters_crows.json` |
| Client | `src/client/java/com/aura/lamplight/menagerie/client/Crow*.java` |
| Shoulder sync (original) | `src/main/java/com/aura/lamplight/mixin/menagerie/{PlayerShoulderCrowMixin,ServerPlayerShoulderMixin}.java` |

---

## 1. Entity snapshot

| Property | Value | Source |
| --- | --- | --- |
| Base class | `ShoulderRidingEntity` (vanilla parrot shoulder-riding base) | `CrowEntity:88` |
| Category / size | `CREATURE`, 0.45 × 0.45, client tracking 10 | `MenagerieEntities:21-23` |
| Health / damage / speed | 8 HP, 1 attack damage, 0.2 movement speed | `CrowEntity:123-128` |
| Spawn placement | On ground, `MOTION_BLOCKING_NO_LEAVES`, raw light > 8 | `MenagerieEntities:43`, `CrowEntity:161-163` |
| Biome spawns | Forest 10 (1-2), taiga 8 (1-2), plains/sunflower plains/meadow/cherry grove 8 (1-2), listed Vantage biomes 8 (1-2) | `Menagerie.java:27-59` |
| Immunities | In-wall, fall, cactus; no fall damage, no trampling | `CrowEntity:194-204, 453-457` |
| Pathfinding malus | Fire −1, water −1, water border 16, cocoa −1, fence −1 | `CrowEntity:115-119` |
| Persisted state | `Flying`, `CrowSitting`, `Command`, perch X/Y/Z | `CrowEntity:378-414` |
| Synced state | `FLYING`, `ATTACK_TICK`, `SITTING`, `COMMAND`, `PERCH_POS` | `CrowEntity:89-93, 444-451` |
| Ambient sound interval | 60 ticks (~3 s) | `CrowEntity:471-473` |

---

## 2. Behavior catalog

Each behavior has an ID for the decision table at the end.

### A. Locomotion and flight

**B01 — Dual ground/flight navigator.** The crow swaps its entire navigation stack at runtime. Airborne: `FlightMoveControl` + `DirectNavigator` (no pathfinding, steer straight at a point). Grounded: vanilla `MoveControl` + `GroundPathNavigation`. `landNavigator` tracks which is active. `CrowEntity:182-192, 287-293`
*Note:* `DirectNavigator` overrides `tick()` to do nothing and every `moveTo` becomes a `setWantedPosition` call. It is a thin adapter rather than a real navigator.

**B02 — Arcade flight physics.** `FlightMoveControl` accelerates toward the wanted point by `speedModifier * 0.7 * 0.05 / distance`, then snaps body yaw to the velocity vector (or to the target when configured). It has no gravity/gravity compensation of its own; `CrowEntity.tick()` sets `noGravity(true)` while flying. `FlightMoveControl:21-45`, `CrowEntity:294-305`

**B03 — Auto-landing.** While flying, `timeFlying` increments; after more than 8 ticks, touching the ground or a vertical collision below cancels flight. Any flight is also cancelled if sitting, a passenger, or in love. `WalkIdleGoal` separately drops flight on ground contact if `timeFlying > 10`. `CrowEntity:294-305`, `CrowEntity:689-691`

**B04 — Stay/sit freezes movement.** `SITTING` is synced. While set, `travel()` zeroes movement input and stops navigation, and `SitWhenOrderedToGoal` holds the crow in place. `CrowEntity:392-403`, `CrowEntity:141-142`

**B05 — Environmental immunity and path preferences.** No fall damage, no trampling, immune to in-wall/fall/cactus. Fire and water are strongly avoided by pathfinding (malus −1), water border is costly (16), fences and cocoa are avoided. `CrowEntity:115-119, 194-204, 453-457`
*Note:* `canTrample` and `causeFallDamage` lack `@Override` and do not match current signatures; only `checkFallDamage` is live.

**B06 — Idle wander (walk or fly).** A random idle goal: 1-in-30 chance per tick (always eligible while already flying). On the ground it randomly chooses to walk or take off; in the air it keeps flying 4/5 of the time until `timeFlying >= 200`. Walk targets use `LandRandomPos` (10 horizontal, 7 vertical); flight targets use the radial helpers B07. `CrowEntity:638-739`

**B07 — Radial flight destination search.** `getBlockInViewAway` picks a point 9-33 blocks away (plus an optional radius add) in a pseudo-random angle around the crow, finds ground beneath it, and chooses a height 1-5 above ground (or 4-13 above when the crow is already more than 8 above ground). Rejects points with a blocked line of sight or within 1 block. `CrowEntity:490-510`

**B08 — Ground destination search (currently flawed).** `getBlockGrounding` computes a radial point, but immediately overwrites it with `blockPosition()` before scanning downward, so the "grounding" point is always directly below the crow rather than at the intended radius. Worth rewriting or dropping regardless of provenance. `CrowEntity:512-529`

**B09 — Water avoidance.** `isOverWater()` scans straight down for fluid. While over water, idle flight is forced to continue and targets use `getBlockInViewAway` rather than attempting to land. `CrowEntity:536-542, 700-708`

**B10 — Look behaviors.** `LookAtPlayerGoal` for players (6 blocks) and for other `PathfinderMob`s (6 blocks), plus `RandomLookAroundGoal`. `CrowEntity:152-154`

### B. Taming, feeding, breeding

**B11 — Taming via dropped seeds.** A wild crow can pick up a dropped item in `#lamplight:crow_tameables` (currently pumpkin seeds) and remembers the entity that dropped/threw it. If it then holds the seed for 60 ticks, it eats it; a 30% roll tames the crow to that thrower, sets command to follow (1), fires the vanilla `TAME_ANIMAL` criterion, and shows heart or smoke particles. `CrowEntity:308-339, 563-577`

**B12 — Hand-feeding and self-healing.** A tamed, owned crow right-clicked with an edible item while its beak is empty takes one item into its beak. Any edible item held for 60 ticks is eaten when the crow is untamed or injured: heals 4 HP, plays `PARROT_EAT`, applies crafting remainder, shrinks the stack. This is the same loop that drives taming. `CrowEntity:241-249, 308-340`

**B13 — Breeding.** Standard `BreedGoal`, offspring is a new crow. `isFood` requires the item to be in `#lamplight:crow_breedables` (pumpkin seeds) **and** the crow to already be tamed, so wild crows never breed. `CrowEntity:148-149, 225-228, 459-463`

**B14 — Held item hygiene.** On damage, the held item is dropped. Right-clicking a crow that is holding something makes it drop the item instead of running the normal interaction. `CrowEntity:217-221, 234-240`

**B15 — Voice.** Idle caw every ~60 ticks, hurt/death screech, vanilla parrot eat sound when eating. `CrowEntity:471-488`, sounds in `assets/lamplight/sounds.json`

### C. Command / order system

**B16 — Four-state command cycle.** Right-clicking a tamed, owned crow with an empty hand (and non-food) cycles `COMMAND` 0 → 1 → 2 → 3 → 0 and shows an overlay message. States: 0 wandering, 1 following, 2 staying, 3 gathering. State 2 also sets ordered-to-sit. `CrowEntity:241-258`
*Quirk:* command 3 has its own translation key (`command_3`, "is gathering items"); 0-2 use `all.command_N`. There is no `command_3` handling for sit, so the crow stands while gathering.

**B17 — Command 1: follow owner.** Sets the crow to follow mode; the actual movement is handled by the follow goal described in B20-B22. Follow mode also permits shoulder mounting on contact (B21, B37).

**B18 — Command 3: gather/hoard.** While idle, the crow flies a horizontal orbit around its recorded perch (radius 4-5, 2 blocks above, only through empty blocks). If it is holding an item, the deposit goal (B23) takes priority. `CrowEntity:663-668, 697-699, 588-599`

**B19 — Gathering disables other idle flight.** `aiItemFlag` and `aiItemFrameFlag` gate the idle goal and several others so item work and deposit work are not interrupted. `CrowEntity:651-666, 714-716, 765, 994`

### D. Owner following (command 1)

**B20 — Owner orbit.** When commanded to follow, not sitting, owner not spectator, and farther than 4 blocks, the crow looks at the owner and flies a circular orbit around them (radius 1-3, height 0-1 above the owner's feet, offset by eye height + 0.2), only through empty blocks. `CrowFollowOwnerGoal:42-123`

**B21 — Close-range shoulder approach.** Within 3 blocks of the owner, the crow flies straight at the owner's eye position at 0.7 speed. Contact is what triggers the shoulder mount (B35). `CrowFollowOwnerGoal:100-105`

**B22 — Long-range teleport.** Beyond 12 blocks (144 squared distance), checked every 10 ticks and only when not doing item work, the crow flies toward the owner and attempts to teleport to a random spot within ±3 X/Z and ±1 Y of the owner. It makes up to 10 attempts, rejects spots within 2 blocks horizontally of the owner, rejects spots with collision, and (per the flag passed as `true`) allows landing on leaves. `CrowFollowOwnerGoal:89-96, 125-157`

*Note:* the `maxDist` constructor parameter is accepted but never stored or used; only `minDist` matters.

### E. Dropped-item behavior

**B23 — Item targeting.** A target goal scans a 16-block cube every tick (execution chance forced to 1) for `ItemEntity`s older than 40 ticks. Wild crows target only edible items; tamed crows target *any* item. It refuses to start while holding an item, while sitting, or while riding. `CrowEntity:155, 548-560, 913-935`, `TargetItemsGoal:31-82`

**B24 — Item approach and pickup.** If the item is more than 8 blocks away or the crow is already flying, it flies to it (adjusting height when it lacks line of sight); otherwise it walks. Within 2 blocks it hovers over the item and pecks. Within 1 block it takes exactly one item into its beak; a previously held item is dropped. `CrowEntity:937-962, 548-577`, `TargetItemsGoal:114-121`

**B25 — Peck animation hook.** `peck()` sets `ATTACK_TICK` to 7, which drives `attackProgress` 0-5 for the client's head-bob animation. Used for eating, item pickup, crop raiding, chest deposit, and melee. `CrowEntity:343-350, 544-546`

**B26 — Chest deposit through item frames.** Command 3 crows holding an item scan a 32-block cube for an `ItemFrame` whose displayed item matches the held item and whose back block is a `Container` (chest, barrel, etc.). They fly to the frame, rise when colliding horizontally, and within 2 blocks insert the held stack through the frame's facing side. Insertion is simulated first, then performed through `WorldlyContainer` rules; leftovers stay in the beak, and a full container imposes a 20-tick cooldown. Success pecks. `CrowEntity:971-1071`

### F. Wild-only behaviors

**B27 — Crop raiding.** Adult, wild, non-sitting crows with no live target and no item work seek a block in `#lamplight:crow_foodblocks` (currently `#minecraft:crops`) within 32 blocks / 8 vertical. They first spend up to 200 ticks circling the crop in the air (radius 1-3, height 1-3 above the crop, 8°/tick orbit), then land and approach on foot. On arrival they peck 5 times, then:
- if the block is a `CropBlock`, decrement its age by 1 (destroy at age 0);
- otherwise destroy the block (with drops).
Requires line of sight, respects `mobGriefing`, and imposes a 1200-tick retry cooldown afterward. `CrowCircleCropsGoal:42-169`

**B28 — Pumpkin fear.** Wild crows periodically (every 70-220 ticks) search up to 20 blocks horizontally and −8..+2 vertically for blocks in `#lamplight:crow_fears` (carved pumpkin, jack-o-lantern), using an expanding-ring scan. On finding one they flee upward/outward for 200 ticks, repeatedly choosing escape points 10 blocks farther out, and all crop raiding is suppressed while the fear timer is active. `CrowEntity:829-911`

**B29 — Startle/scatter.** Wild crows scan a 5×5×5 box every few ticks for non-creative/non-spectator players or entities in `#lamplight:scatters_crows` (wolf, ocelot, cat), pick the nearest, and fly away to an upward point. The 20-block check is only a stop-leash: the goal ends if the threat gets farther than 20 blocks. It ignores tamed crows, passengers, and item-busy crows. `CrowEntity:741-827`

**B30 — Home-block healing and perch memory.** Every 50 ticks, a tamed crow standing on a block in `#lamplight:crow_home_blocks` (hay bale) heals 1 HP, emits happy-villager particles, and records a synced/saved perch position. The perch is what command 3 orbits. `CrowEntity:351-361, 364-375, 579-586`

*Note:* the angry-villager branch (event 68) exists in the handler but is never broadcast.

### G. Combat

**B31 — Retaliation and owner defense.** Targets come from: owner hurt by an entity, owner's target, and the crow being hurt (with `setAlertOthers`, ignoring player damage so hitting a crow does not make the flock retaliate against players). Tamed crows also consider their owner's pets and allies as allies. `CrowEntity:156-158, 165-180`

**B32 — Aerial circling melee.** Against a live target, unless sitting or in gather mode: orbit the target at radius 1-4 and height 0-1 above it for a random 20-120 ticks, then dive at 1.3 speed to the target's eye-height midpoint. Within 2 blocks, peck and deal **4 damage to `#minecraft:undead`, 1 damage otherwise**, using a generic damage source, then stop. `CrowMeleeGoal:25-75`

**B33 — Tamed damage reduction.** Incoming damage from anything that is not a player and not an arrow is reduced to `(amount + 1) / 4` when tamed. `CrowEntity:207-217`

**B34 — Injury response.** Being hurt cancels "stay" mode (`setOrderedToSit(false)`). `CrowEntity:212`

### H. Spawning and persistence

**B35 — Spawn and biome distribution.** See the entity snapshot. Crows spawn in small groups of 1-2 in forests, taigas, open plains/meadows/cherry groves, and a curated list of Vantage biomes. `Menagerie.java:27-59`, `CrowEntity:161-163`

**B36 — Save/load.** Flight state, sit state, command, and perch coordinates survive world reload. `CrowEntity:377-414`

### I. Shoulder sitting (original work, not ported)

**B37 — Shoulder mount.** `CrowShoulderGoal` mounts the crow onto a `ServerPlayer` owner when: command is 1, not sitting, owner not spectator/flying/in water/in powder snow, crow is not leashed, and the crow's bounding box intersects the player's. It uses vanilla `ShoulderRidingEntity.setEntityOnShoulder`. The follow goal's close-range charge (B21) is what delivers the crow into the player. `CrowShoulderGoal:14-38`, `CrowFollowOwnerGoal:100-105`

**B38 — Shoulder sync and rendering.** The player mixin adds two synced booleans (left/right crow shoulder), the server-player mixin derives them from the shoulder entity NBT tags, and client layers draw the crow plus its held item on the shoulder. This is the part stated to be original; it can remain regardless of what happens to the rest. `PlayerShoulderCrowMixin`, `ServerPlayerShoulderMixin`, `CrowOnShoulderLayer`, `CrowShoulderRenderState`

---

## 3. Goal priority map

Goal selector (higher priority = lower number):

| Priority | Goal | ID |
| --- | --- | --- |
| 0 | `FloatGoal` | — |
| 1 | `SitWhenOrderedToGoal` | B04 |
| 2 | `CrowMeleeGoal` | B32 |
| 2 | `CrowShoulderGoal` | B37 |
| 3 | `CrowFollowOwnerGoal` | B20-B22 |
| 4 | `DepositChestsGoal` | B26 |
| 4 | `ScatterGoal` | B29 |
| 5 | `AvoidPumpkinsGoal` | B28 |
| 5 | `BreedGoal` | B13 |
| 6 | `CrowCircleCropsGoal` | B27 |
| 7 | `WalkIdleGoal` | B06 |
| 8 | `LookAtPlayerGoal` (Player, 6) | B10 |
| 9 | `LookAtPlayerGoal` (PathfinderMob, 6) | B10 |
| 10 | `RandomLookAroundGoal` | B10 |

Target selector:

| Priority | Goal | ID |
| --- | --- | --- |
| 1 | `CrowTargetItemsGoal` (radius 16, 40-tick age, every tick) | B23 |
| 2 | `OwnerHurtByTargetGoal` | B31 |
| 3 | `OwnerHurtTargetGoal` | B31 |
| 4 | `HurtByTargetGoal` (ignores player damage, alerts others) | B31 |

---

## 4. Tags and tunables

| Tag / constant | Contents / value | Used by |
| --- | --- | --- |
| `#lamplight:crow_tameables` | `minecraft:pumpkin_seeds` | B11, B12 |
| `#lamplight:crow_breedables` | `minecraft:pumpkin_seeds` | B13 |
| `#lamplight:crow_foodstuffs` | eggs, sugar, seeds, wheat/pumpkin/melon/beetroot seeds, breedables | B12, B23 |
| `#lamplight:crow_foodblocks` | `#minecraft:crops` | B27 |
| `#lamplight:crow_fears` | carved pumpkin, jack-o-lantern | B28 |
| `#lamplight:crow_home_blocks` | hay block | B30 |
| `#lamplight:scatters_crows` | wolf, ocelot, cat | B29 |
| Taming chance | 30% per eaten seed | B11 |
| Eat delay | 60 ticks held | B11, B12 |
| Heal per meal | 4 HP | B12 |
| Item target radius / age | 16 blocks / 40 ticks | B23 |
| Pickup distance | 1.0 block | B24 |
| Crop search | 32 horizontal / 8 vertical | B27 |
| Crop circle phase | 200 ticks | B27 |
| Crop retry cooldown | 1200 ticks | B27 |
| Pumpkin search radius | 20 horizontal / −8..+2 vertical | B28 |
| Pumpkin fear duration | 200 ticks | B28 |
| Scatter detection | 5×5×5 box; stop beyond 20 blocks | B29 |
| Perch heal interval | 50 ticks, 1 HP | B30 |
| Melee undead / normal | 4 / 1 damage | B32 |
| Tamed damage taken | `(amount + 1) / 4` | B33 |

---

## 5. Decision worksheet

Mark one: **Keep**, **Rewrite**, **Remove**. "Rewrite" = keep the idea but reimplement from scratch with different mechanics/code.

### Locomotion

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B01 | Dual ground/flight navigator | Both | | |
| B02 | Arcade flight physics | Both | | |
| B03 | Auto-landing | Both | | |
| B04 | Stay/sit freezes movement | Tamed | | |
| B05 | Fall immunity / path maluses | Both | | |
| B06 | Idle wander (walk/fly) | Both | | |
| B07 | Radial flight destination search | Both | | |
| B08 | Ground destination search | Both | | |
| B09 | Water avoidance | Both | | |
| B10 | Look-at-player / random look | Both | | |

### Taming and upkeep

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B11 | Taming via dropped seeds (30%) | Wild | | |
| B12 | Hand-feeding and self-healing | Tamed | | |
| B13 | Breeding (tamed only, pumpkin seeds) | Tamed | | |
| B14 | Drop held item when hurt / right-clicked | Both | | |
| B15 | Idle/hurt/eat sounds | Both | | |

### Orders

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B16 | Four-state command cycle | Tamed | | |
| B17 | Command 1 enables follow mode | Tamed | | |
| B18 | Command 3 idle perch orbit | Tamed | | |
| B19 | Item/deposit work locks out idle goals | Tamed | | |

### Following

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B20 | Owner orbit | Tamed | | |
| B21 | Close-range shoulder approach | Tamed | | |
| B22 | Long-range teleport | Tamed | | |

### Items

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B23 | Dropped-item targeting (tamed: anything) | Both | | |
| B24 | Item approach and pickup | Both | | |
| B25 | Peck animation hook | Both | | |
| B26 | Chest deposit through matching item frames | Tamed | | |

### Wild behavior

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B27 | Crop raiding with aerial circling | Wild | | |
| B28 | Pumpkin fear | Wild | | |
| B29 | Startle/scatter from players and predators | Wild | | |
| B30 | Hay-bale healing and perch memory | Tamed | | |

### Combat

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B31 | Retaliation and owner defense | Tamed | | |
| B32 | Circling melee, dive, undead bonus | Both | | |
| B33 | Tamed damage reduction | Tamed | | |
| B34 | Hurt cancels stay | Tamed | | |

### Spawning / data

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B35 | Spawn rules and biome list | Wild | | |
| B36 | Save/load of flight, command, perch | Both | | |

### Shoulder (original)

| ID | Behavior | Default audience | Decision | Notes |
| --- | --- | --- | --- | --- |
| B37 | Shoulder mount on contact | Tamed | Keep | Original, no licensing concern |
| B38 | Shoulder sync and rendering | Tamed | Keep | Original, no licensing concern |

---

## 6. Entanglement notes for removal

Useful when deciding what to cut, because removing one behavior can orphan helpers or tags:

- **Circling/orbit math (B20, B27, B32, B18)** is the most reused ported idiom: `CrowMaths.STARTING_ANGLE`, `EIGHT_STARTING_ANGLE`, the `sin/cos` orbit loop, and `CrowBlockPos.fromVec3`. Removing all orbit behaviors lets `CrowMaths` and `CrowBlockPos` disappear entirely.
- **B07/B08 radial helpers** (`getBlockInViewAway`, `getBlockGrounding`, `crowGround`, `crowAngle`, `isTargetBlocked`, `isOverWater`) feed idle wandering (B06), startle (B29), and pumpkin fear (B28). Removing all three makes that whole cluster dead code.
- **B23/B24/B26** share `TargetItemsGoal`, `TargetsDroppedItems`, the held-item slot, `seedThrowerId`, `heldItemTime`, and `aiItemFlag` / `aiItemFrameFlag`. Dropping item hoarding removes a large amount of state from `CrowEntity`.
- **B11/B12/B13** share the 60-tick eat loop, the food tags, heel logic, and taming. Simplifying feeding simplifies all three.
- **B18/B26/B30** form the "gatherer" identity: perch recording, perch orbit, and chest deposit. Removing command 3 removes the perch fields and the two largest inner goal classes.
- **B27/B28** share `fleePumpkinFlag` and the crop/pumpkin block tags. Removing crop raiding also removes `CrowCircleCropsGoal` and its line-of-sight/destroy logic.
- **B02/B03** are only needed if the crow flies at all; the rest of the flight cluster (B01, B06, B07, B20, B22, B27, B29, B32) assumes them.
- **B37/B38** are independent of all ported code except the follow goal's contact charge (B21). If the follow goal is rewritten, preserve some way to reach the shoulder-mount trigger.

## 7. Known quirks and dead code

Worth fixing or discarding during a rewrite rather than carrying forward:

1. `CrowFollowOwnerGoal` accepts `maxDist` but never uses it. `CrowFollowOwnerGoal:32-39`
2. `getBlockGrounding` computes a radial point then overwrites it with the crow's own position. `CrowEntity:512-529`
3. `isFood` requires the crow to already be tamed, so wild breeding cannot occur. `CrowEntity:225-228`
4. `canTrample` and `causeFallDamage` do not override current API signatures; only `checkFallDamage` is effective. `CrowEntity:194-204`
5. Entity event 68 (angry villager particles) is handled but never sent. `CrowEntity:364-375`
6. `gatheringClockwise` is a `final` field initialized to `false`, so perch orbiting always runs one direction. `CrowEntity:111, 592`
7. `fleePumpkinFlag` is decremented in `tick()` but is not read client-side for animation, despite the name suggesting a visual state. `CrowEntity:283-285`
8. Tamed crows target *any* dropped item, including items with no food value, which makes them indiscriminate thieves. `CrowEntity:548-551`
9. Command 3 has no sit behavior; the crow stands while gathering. `CrowEntity:246-257`
10. The melee goal deals generic damage and hard-codes an undead bonus, rather than using the attack-damage attribute. `CrowMeleeGoal:56-61`
