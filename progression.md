# Doors, Not Rooms

> A progression design doc for Lamplight.

The first two hours of the pack are vanilla with nicer hills. That's the thing worth fixing. Not because there isn't enough content — there's a *lot* of content — but because almost none of it is standing in the doorway when the player walks past.

This doc is about doors. Where they are, where they should be, and why a small structure with the right key is worth more than a big one with diamonds.

---

## 1. The diagnosis: rooms vs. doors

Vanilla progression is a chain of bottlenecks. Wood gets you stone. Stone gets you iron. Iron gets you a bucket, a shield, an anvil, and the confidence to go deeper. Diamonds get you enchanting. The Nether gets you brewing. The End gets you wings. Each bottleneck changes what you can *do*, not just what number is printed on your sword.

Lamplight has a lot of rooms:

- cooking and farming
- potion brewing
- enchanting with slots
- spell scrolls
- the back slot
- maps and waypoints
- crows, and eventually wisps
- building blocks
- structures
- terrain

But most of the doors into those rooms are behind the same bottlenecks vanilla already uses. The cooking pot is iron. Enchanting is diamonds, obsidian, lapis, and XP. Brewing is blaze powder. The spellbook is craftable, but nothing currently inscribes a scroll. The back slot is available on day one, but it accepts anything and no items are designed for it. The structures are places, not keys: the working ones are mostly decor, and the big template pools point at NBT that isn't in the repo. Vantage adds terrain and no resources.

So the shape of a playthrough is:

1. Vanilla early game with better scenery.
2. Vanilla midgame, with a pile of optional systems starting to appear.
3. Vanilla late game, with a pile of optional systems that never quite became the reason to do anything.

That's why it reads as vanilla. The mod is *beside* the path, not *on* it.

There's a second thing worth saying plainly: the one place you did change vanilla progression actually flattens it. Durability is turned off globally, and Mending, Unbreaking, and Sharpness are removed. That's a real change — but it's subtractive. Once you have diamond gear, the gear loop is over forever. Nothing replaced the treadmill. Gear becomes a thing you made once and then stop thinking about.

That's not a criticism of the choice. It's an opportunity: if gear no longer breaks, then *customization* has to become the loop that durability used to be. More on that later.

### The one-sentence test

Right now, a new player would describe the pack as "prettier terrain."

The target is: "it's a world you pack for and explore."

That's the whole design brief in one line. If a feature doesn't move the second sentence closer, it's probably a room without a door.

---

## 2. The principle

**Put a door on the ground floor, and make the world — not the mining ladder — the key.**

That's the rule I'd design by. A few corollaries:

- **Capability, not stats.** The reward for exploring should be a new verb, not a bigger number.
- **Sidegrades, not power creep.** A new thing should be good at something specific and bad at something else. It should not make an old thing pointless.
- **Soft gates, not hard locks.** Discovery should add a guaranteed path or a better path, not remove the ability to play. Never gate vanilla recipes. Never make the player unable to progress without a wiki.
- **Optional but legible.** Players should be able to ignore any given system, but they should always be able to tell what it is for and where the next piece is.
- **Learn by wandering, not reading.** The world should teach. A camp with a fire and a pan teaches camping better than a tooltip.
- **Small is fine.** A structure doesn't need to be big to be a key. It needs to be specific.

The goal is not to bury vanilla under content. It's to make vanilla feel like it's happening in *this* world.

---

## 3. The door map

Here's the current state, and where I'd move the first door for each system. The point isn't to move everything earlier; it's to make sure every system has at least one door the player can find before iron, and one door that opens later.

| System | Where the door is now | Where I'd put the first door | What changes |
| --- | --- | --- | --- |
| **Cooking** | Iron cooking pot | Campfire + copper pan on day one; the pot stays iron | Food becomes a plan, not a hunger bar |
| **Alchemy** | Nether brewing stand | Cauldron + foraged herbs at copper; brewing upgrades it later | Potions exist before the Nether; the Nether makes them better |
| **Enchanting** | Diamond table, random rolls | Guaranteed enchanted books in iron-tier structures | Gear customization starts at iron without raising the ceiling |
| **Magic** | No inscription path at all | Scroll pages in ruins and towers, day one to iron | Magic becomes something you find, not just something you craft |
| **Back slot** | Empty from day one | Bedroll and lantern on day one; quiver and tool roll at iron; pack later | The slot becomes a loadout, not a storage accident |
| **Companions** | Biome RNG, easy to miss | Crows at camps and along roads from day one | The first companion is part of the first loop, not a late surprise |
| **Mapping** | A keybind and a UI | A journal and map case from the first camp | Exploration gets a record and a direction |
| **Building** | Recipes exist, but no reason to seek them | Structure rewards and early patterns | Building becomes part of the loop, not just the reward at the end |

Notice that none of these raise the power ceiling. They move the *entry point* earlier and make the *next* step something you find in the world. That's the whole trick.

The early doors matter most. A player who meets the mod before iron will remember it. A player who meets it after diamonds has already decided what kind of game they're playing.

---

## 4. The spine: fieldcraft

If I had to pick one thing to build first, it would be **fieldcraft**: the fantasy of packing for a trip, going somewhere, and coming back with something you didn't have before. It's the one fantasy vanilla doesn't serve, it uses almost everything you've already built, and it's visible in the first thirty minutes.

The spine is simple: **home is the base, the road is the progression, and the journal is the connective tissue.**

### Day one: the camp

- The back slot gets its first real items: a **bedroll** and a **tinderbox** or **lantern**. The bedroll lets you sleep in the wild without setting your spawn. The lantern gives light without torches. Now the slot is a choice.
- **Campfire cooking** with a copper or stone pan. The iron cooking pot is still the upgrade; the *idea* of cooking arrives immediately.
- A **field journal**. It records what you've learned and hints at where the next page is.
- A small **camp** structure within a few hundred blocks of spawn: a fire, a bedroll, a pan, a journal, and crows hanging around. It's a tutorial, a rest stop, and the first "oh, this mod is about *this*" moment.
- Crows become the first companion: tamed with pumpkin seeds, foraging food for the pot, hanging around camps. They're already built. They just need to be where the player is.

The point of day one isn't to give the player more power. It's to give them a reason to leave home earlier and stay out longer.

### Copper: the first real door

Copper is the new vanilla pre-iron tier, and Lamplight barely touches it. That's a gift. It's the exact point where vanilla is thinnest.

- **Copper kettle**, **copper lantern**, **copper still**.
- **Hedge alchemy**: foraged herbs, water, and heat in a cauldron. Short, modest tonics and teas. Not potions yet — the beginning of potions. The Nether upgrades them later.
- The first **herb seeds** or cuttings, so the alchemy loop has a garden.

This is where the pack starts to feel modded in normal play. Not because the player has a new tier, but because they're doing something vanilla doesn't ask them to do: foraging, brewing, and packing for a trip before they have iron.

### Iron: the first real choice

- Cooking pot, frying pan, waterskin, quiver, tool roll.
- First **guaranteed enchanted books** in structures. Not random loot — specific ones. A mine outpost can guarantee a Dash book; a coastal ruin, Water Breathing. This drags the movement and enchantment content into iron tier without touching the table's power.
- First **sigil page** in a ruin.
- The journal starts to fill in. The player can see what they know and what they don't.

This is the point where the back slot becomes a loadout: bedroll or lantern, quiver or tool roll, sword or scythe. The slot is small, but it's a decision every time you leave the house.

### Diamond: the gear loop

- Enchanting table and slots as they are now, but with sigils and books as a *targeted* alternative to RNG.
- Spellbook and spell pages.
- **Gear attunement**, the missing companion to the durability removal. More on this below.

Diamond is where the player expects the game to open up. The goal is for Lamplight to open a *different* door at the same time: not "more damage," but "more say in what your gear is."

### Nether and End: the far doors

- Brewing stand, reagent pages, fire and cold gear.
- Capstone back items, spell pages, trinkets, and cosmetics.
- The Nether should feel like the place where the early doors get *upgraded*, not the place where the real game finally starts.

The shape I want is: **early doors, long middles, late upgrades.** Each stage should add a verb, not replace the previous one.

### A few small pictures

- You're going to the Nether for blaze rods. You swap the lantern on your back for a fire-resistant cloak, pack the emberglass lantern, and leave. The trip is still dangerous; it's just *prepared*.
- You're settling a new valley. You set your bedroll by the fire, cook a proper meal, and the journal fills in a page. It's not combat gear. It's home gear.
- You find a diving tonic recipe in a sunken ruin. Now your trips to the ocean are longer, and the drowned take a moment to notice you.
- You've been using the same pickaxe for a hundred days. It has a name and a history. It's not the best pickaxe in the world. It's yours.

That last one is the real target. The pack should produce gear with a story, not just gear with a number.

---

## 5. The field journal

The journal is the piece that turns a pile of systems into a progression.

It should be a single thing — an item, a UI, or both — that records what the player has learned and gives them a sense of what's next. Not a quest log. A *field journal*. The distinction matters:

- A quest log says "you have 7 of 12 chores left."
- A field journal says "you know how to make three kinds of trail bread, one tonic, and one sigil; the notes mention a tower to the north."

The journal is a record of *capability*, not completion.

What it can hold:

- cooking techniques and dishes
- potion and tonic recipes
- sigils and enchantment patterns
- trinkets and back-slot patterns
- companions and where they live
- places you've been and places you've heard about
- little observations: "crows like pumpkin seeds," "the water in the sunken ruins tastes like metal"

How pages work:

- Pages are found, not bought. They live in structures, camps, ruins, and the occasional very lost traveler.
- A page should unlock a **guaranteed path** or a **better path**, not the only path. If the player already knows the recipe from the recipe book or from experimenting, the page should still be useful: it should give the *good* version, the efficient version, or the version with the interesting side effect.
- Never gate vanilla recipes. Never make the player unable to play because they haven't found a page.
- A page can also be a *hint*: a map fragment, a note about a place, a name. The journal should be able to say "I don't know what this is yet" without feeling like a failure.

The journal is also the answer to "how do I make the world feel discovered?" It's the difference between a recipe book that lists everything and a notebook that fills in as you go.

The risk is that it becomes a checklist. The way to avoid that is to make it *descriptive* rather than *prescriptive*: it tells you what you can do and what you've heard, not what you owe the game. The player should be able to ignore it for a week and come back to find it still useful.

---

## 6. Structures as keys

Small structures are not the problem. **Unreadable rewards** are.

A camp with a new verb is better than a big dungeon with diamonds. A dungeon with diamonds is a place you leave; a camp with a pan and a journal is a place you *use*. The goal is for every structure to answer one question: *what can I do now that I couldn't before?*

Tier them by what they teach, not by how big they are:

| Structure | What it gives | Why it matters |
| --- | --- | --- |
| **Camp / roadside ruin** | Bedroll pattern, first journal, camp cooking page | Teaches fieldcraft on day one |
| **Mine outpost** | Guaranteed enchant book, provision recipe, ore-sense page | Makes iron-tier exploration pay into gear |
| **Mage tower / library** | Sigil or spell page, lectern | Makes magic discoverable |
| **Ocean ruin** | Diving tonic/wine, water-breathing book, tide-sense page | Gives the ocean a purpose beyond monuments |
| **Witch hut / alchemist's camp** | Cauldron recipe, herb cuttings, tonic page | Teaches hedge alchemy |
| **Nether structure** | Fire/cold reagent page, ember lantern pattern | Upgrades the early doors |
| **End structure** | Capstone page, cosmetic, heirloom pattern | The long tail |

A few rules for making them feel like keys:

- **One capability + one hint.** Not a chest of random loot. A page, a pattern, and a note about where to go next.
- **The station can be the reward.** A camp has a fire and a pan. A witch hut has a cauldron. A tower has a lectern. A village workshop has a bench. The player learns by using the place, not by reading a book about it.
- **Readable from the outside.** Smoke, a lantern, a banner, a strange light. The player should be able to tell "something is here" before they open a chest.
- **Specific, not random.** A structure should have a *thing* it gives. Random loot makes every structure interchangeable; a specific page makes each one a destination.
- **Small is fine.** The best structure in the pack might be a one-room camp with a fire, a bedroll, and a note. Don't build a cathedral until the camp is working.

The existing structures can carry this. They don't need to get bigger. They need to get *legible*.

---

## 7. The gear hole: attunement

Here's the thing about turning durability off: you removed the treadmill, but you didn't replace it. Gear is permanent now, which is good, but it also means gear stops being a progression after diamonds. You make the set, you enchant it, and then you're done.

The replacement loop should be **attunement**: gear gets better at being *itself* through use, and the player gets more say in what it is.

The shape:

- Gear earns attunement through use. Mining, fighting, traveling, cooking, whatever the item is for.
- Each attunement level unlocks something: an extra slot, the ability to swap an enchantment, a small quality-of-life perk.
- Sigils and books let the player *choose* an enchantment instead of rolling it. The table still exists; it's just no longer the only door.
- Looted gear can have quality: more slots, a head start on attunement, a quirk. That makes opening a chest exciting again.
- The ceiling stays at vanilla. The choices are utility, mobility, and elemental flavor, not raw damage. No Sharpness. No "this sword is just better."

This is the loop that makes the Arcanum system feel like a progression instead of a restriction. It's also what gives gear a history: a pickaxe that has been with you for a hundred days should feel different from one you made this morning, even if the numbers are the same.

If you only do one thing from this section, do the "looted gear has quality" part. It's the cheapest way to make gear exciting again without adding a single point of damage.

---

## 8. The materials idea

Progression in Minecraft is driven by materials. You don't just need a better tool; you need a thing you didn't have before. Lamplight adds very few materials. Food, wine, and a handful of ingots — that's most of it. That's part of why the world feels like vanilla: there's nothing new to seek, carry, and spend.

I wouldn't add a pile of ores. I'd add a small number of **curios**: found things with a clear source and more than one use. Three to five is enough. Each one should be a key, not a stat.

Some examples:

- **Emberglass** — volcanic and deep-cave structures. Used for heat-resistant back items and a warm lantern.
- **Tide salt** — ocean ruins and coral. Used for diving tonics and preserving food.
- **Bloom** — flower forests and meadows. Used for teas and dyes.
- **Soul dust** — soul sand valleys and wisp nests. Used for lamps and a potion base.
- **Crow feathers** — crows, obviously. Used for quills and fletching. The bird already exists; give it an economy.

The rules for curios:

- **One clear source.** The player should be able to remember where it comes from.
- **More than one use.** A curio that only makes one thing is a chore. A curio that opens three doors is a discovery.
- **Not an ore.** Found things are more interesting than another pickaxe target.
- **Small in number.** Five curios the player can name beats twenty they can't.

This is a supporting layer, not the spine. The spine is the doors; the curios are what you carry through them.

---

## 9. What not to do

A list of tempting things that would make the problem worse:

- **Don't add a new ore or a new rung on the ladder.** If you add a tier, make it a branch or a sidegrade. A new rung just makes the old rungs shorter.
- **Don't gate vanilla recipes.** The player should never be unable to play because they haven't found a page. Add a guaranteed path, not a lock.
- **Don't add mandatory thirst, temperature, or spoilage as the fix.** It fights the cozy tone, it's a balance nightmare, and it turns exploration into a timer. If you want seasons, make them gentle pressure, not a countdown.
- **Don't build bigger structures before the small ones pay into a system.** A cathedral with vanilla loot is still a room without a door.
- **Don't add more stat loot.** Loot is forgettable. Capability is memorable.
- **Don't add a fifth system before the existing ones have doors.** Cooking, alchemy, enchanting, magic, the back slot, companions, and mapping are already a lot. Give them entrances before you give them siblings.
- **Don't confuse content with progression.** More dishes, more blocks, more mobs — none of it changes the course of the game unless it changes what the player can do next.
- **Don't make the journal a chore.** It should describe, not prescribe.
- **Don't make the early game a tutorial.** The camp should feel like a place, not a lesson.

---

## 10. The first slice

If you want to test the whole thesis before committing to a big content pass, build this:

- One small **camp** structure, placed in the first biome, visible from a distance.
- One **bedroll** for the back slot.
- One **copper pan** for campfire cooking.
- One **field journal**.
- One **page** that teaches a real recipe — something with a clear, situational payoff, not a stat.
- One **guaranteed enchant book** in a nearby ruin.
- Crows at the camp, tameable with pumpkin seeds, foraging food.

That's it. No new ore, no new tier, no big dungeon.

Then play for three in-game days and ask two questions:

1. **Did I change what I did because of the mod?** Did I pack for a trip, sleep in the wild, cook something I wouldn't have cooked, or go to the camp instead of mining?
2. **Can I say what the mod is in one sentence?** If the answer is still "prettier terrain," the reward isn't legible yet.

If the answer to the first is yes and the second is "a world you pack for," you have the spine. Everything else is expansion.

---

## 11. Open questions

These are the choices I'd want to settle before building:

- Which system gets the first door? My answer is fieldcraft, because it's the most visible and the least dependent on other systems.
- Is the journal a physical item, a UI, or both? A physical item is cozier; a UI is more usable. Both is probably right, eventually.
- Do pages hard-gate recipes or add guaranteed paths? I'd say guaranteed paths, always. Hard gates make the game depend on a wiki.
- Is copper the right early hook, or is it too thin? Copper is the best candidate because vanilla leaves it empty, but it needs a reason to care beyond "it's early."
- How much of this depends on Almanac? Fieldcraft works without seasons. Seasons make it *better*, but they shouldn't be a prerequisite.
- Does attunement apply to all gear or only modded gear? All gear is more coherent, but it's a bigger balance change.
- What's the role of villages and trading? A village is a natural place for pages, patterns, and a workbench. It might be the cheapest structure door in the game.
- How do you keep the journal from becoming a checklist? Describe capability, not completion. Show what the player can do, not what they haven't done.
- How do you make structures readable without quest markers? Smoke, light, sound, silhouette, and rumors in the journal. The world should point.
- What's the failure state for a page or a pattern? My preference: pages are permanent knowledge; physical keys can be re-earned. The player should never lose a discovery to a mistake.

---

## 12. The one-sentence version

**Vanilla is the ladder. Lamplight should be the road.**

Or, if you prefer the shorter one:

**Doors on the ground floor, keys in the world.**

That's the whole doc. Everything else is a way of putting doors where the player is already walking.
