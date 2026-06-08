# Scavenger Extra Trials

Scavenger Extra Trials is an unofficial NeoForge add-on for Scavenger.

It adds extra modifiers to the original Scavenger modifier wheel. In multiplayer, install it on both the client and the server.

## Requirements

- Minecraft 1.21.11+
- NeoForge 21.11+
- Scavenger
- Architectury API
- ShatterLib

## Modifiers

- `scavenger_extra_trials:closing_border`
- `scavenger_extra_trials:route_pressure`
- `scavenger_extra_trials:glass_heart`
- `scavenger_extra_trials:upside_down`
- `scavenger_extra_trials:enderman_blood`
- `scavenger_extra_trials:hot_potato`
- `scavenger_extra_trials:repelling_loot`
- `scavenger_extra_trials:spicy_start`

## Commands

- `/scavengerextra modifier list`
- `/scavengerextra modifier set <modifier_id>`
- `/scavengerextra modifier get`
- `/scavengerextra modifier random`
- `/scavengerextra modifier testfire [modifier_id]`
- `/scavengerextra sync`
- `/scavengerextra win reset`
- `/scavengerextra status`
- `/scavengerextra reload`

`closing_border` uses real Overworld world border pressure. Default balanced values are start 1024, minimum 256, first pulse after 80 ticks, shrink delay 200 ticks, and shrink duration 2400 seconds. With `fastTestingDefaults=true`, it uses start 768, minimum 256, delay 120 ticks, and shrink duration 1200 seconds.

`route_pressure` forces the player to leave the current chunk within 15 seconds and throttles persistent warnings. `enderman_blood` safely teleports the player and drops configured armor/hand items without deleting them.

`hot_potato` now tracks main hand and offhand separately, warns after 25 ticks, and overheats after 60 ticks. `repelling_loot` is fast smooth anti-magnet loot: nearby drops accelerate away on X/Z through velocity only, keep natural falling, and remain collectible inside the inner pickup radius. `glass_heart` temporarily reduces max health after heavy damage and restores stacks over time.

`upside_down` rolls only the world camera while leaving GUI, chat, inventory, and menus normal. `spicy_start` sends the player to a safe Nether position once, grants hidden short protection windows, and adds a portal glitch when leaving the Nether.

Fabric dependency jars were found in the provided profile archive, but the project remains NeoForge-only until a dedicated multi-loader module is added.
