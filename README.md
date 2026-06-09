# Scavenger Extra Trials

Scavenger Extra Trials is an unofficial NeoForge/Fabric add-on for the Scavenger mod.

It adds extra challenge modifiers to the original Scavenger modifier wheel. The goal is simple: make short Scavenger runs more chaotic, risky, and replayable without editing the original Scavenger jar.

## Requirements

* Minecraft 1.21.11+
* NeoForge 21.11+ or Fabric Loader 0.19+
* Scavenger 1.0.2+
* Architectury API
* ShatterLib

Install the matching loader jar together with all required dependencies.

## Builds

`gradlew build` builds both loader jars and copies them into the root build output:

* NeoForge: `build/libs/scavenger_extra_trials-1.0.0.jar`
* Fabric: `build/libs/scavenger_extra_trials-fabric-1.0.0.jar`

## Modifiers

* `closing_border` — the world border starts to close in during the run.
* `route_pressure` — staying in the same chunk for too long becomes dangerous.
* `glass_heart` — heavy damage can temporarily reduce maximum health.
* `upside_down` — flips the world view while keeping the UI usable.
* `enderman_blood` — taking damage can teleport the player and drop equipment.
* `hot_potato` — holding the same item for too long makes it overheat.
* `repelling_loot` — dropped items try to move away from the player.
* `spicy_start` — starts the run in the Nether with short protection and portal instability.

## Commands

Operator-only test commands:

```mcfunction
/scavengerextra modifier list
/scavengerextra modifier set <modifier_id>
/scavengerextra modifier get
/scavengerextra modifier random
/scavengerextra modifier testfire
/scavengerextra sync
/scavengerextra win reset
```

These commands are mainly for testing and debugging modifiers during development.

## Multiplayer

Install the matching loader jar on both the client and the server.

All players should use the same mod version.

## Notes

This project is not affiliated with the original Scavenger author.

The Scavenger Extra Trials project is licensed under GPL-3.0-only.
