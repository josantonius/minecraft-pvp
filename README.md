# Minecraft PvP Plugin

[![License](https://img.shields.io/github/license/josantonius/minecraft-pvp)](LICENSE)

Experience thrilling player-versus-player battles in Minecraft servers with this dynamic plugin.

## [Watch demo on YouTube](https://www.youtube.com/watch?v=6a3UklFQ0WY)

## Requirements

- Java 17 or higher.
- Purpur server 1.19.3 or Bukkit/Spigot/Paper server compatible with the Purpur API version used.

## Installation

1. Download the JAR file: [pvp-1.0.0-purpur-1.19.3.jar](/build/libs/pvp-1.0.0-purpur-1.19.3.jar).

1. Place the JAR file in the plugins folder of your Minecraft server.

1. Restart the server to load the plugin.

## Building

To build the plugin yourself, follow these steps:

1. Make sure you have `Java 17` or higher and `Gradle` installed on your system.

1. Clone the plugin repository on your local machine:

    ```bash
    git clone https://github.com/josantonius/minecraft-pvp.git
    ```

1. Navigate to the directory of the cloned repository:

    ```bash
    cd minecraft-pvp
    ```

1. Use Gradle to compile the plugin:

    ```bash
    gradle build
    ```

## How does it work?

- Player A challenges player B to a PvP battle.
  
- Player A has 60 seconds to cancel the challenge while player B does not accept.

  - If player A cancels the challenge:

    - The cancellation of the challenge is announced.

- Player B has 60 seconds to accept or reject the challenge:

  - If player B accepts the challenge:

    - Player B is teleported to player A's location:

      - The beginning of the PvP battle is announced:

        - When a player dies or disconnects:

          - The winner is announced.

          - The result is recorded.

          - The PvP ends.

    - [Both players are teleported to the PvP area and face towards the center:](#teleporting-players-to-specific-locations)

      - [Players inside the PvP area are teleported to the spectator location.](#teleporting-spectators-to-specific-locations)

      - [Doors of the PvP area are closed by placing blocks in them.](#blocking-doors)

      - The beginning of the PvP battle is announced:

        - When a player dies or disconnects:

          - The winner is announced.

          - The result is recorded.

          - The doors of the area are opened by removing the blocks placed at the beginning.

          - The PvP ends.

  - If player B does not accept or reject the challenge within 60 seconds:

    - The cancellation of the challenge is announced.

- Operators can cancel the PvP challenge at any time.

## Commands

- `/pvp <player>` - Challenge another player to a PvP battle

- `/pvpaccept` - Accept the PvP challenge

- `/pvpdeny` - Reject the PvP challenge

- `/pvpcancel` - Cancel the PvP challenge

- `/pvphelp` - Display help for the PvP plugin commands

- `/pvptop` - Display PvP win and loss statistics

- `/pvpreload` - Reload the plugin

All commands excepts `/pvpreload` requires the `pvp.use` permission to be used.

The `/pvpreload` command requires the `pvp.admin` permission.

## Configuration

The `plugins/PvP/config.yml` file contains specific plugin configurations.

### Teleporting players to specific locations

If you want players to be teleported to specific locations, for example inside
a coliseum, and for both to face the same direction, you can configure both
players' locations and the central point of the PvP zone as follows:

```yaml
world: world

pvpZonePlayerOne:
  x: 23.0
  y: 63.0
  z: -138.0

pvpZonePlayerTwo:
x: 23.0
y: 63.0
z: -176.0

pvpZoneCenter:
 x: 23.0
 y: 62.0
 z: -157.0
```

### Teleporting spectators to specific locations

If you want players within a certain horizontal and vertical radius from the center of the PvP area
to be teleported to a specific location, for example, a place where they can watch the fight, you
can configure the spectator location and the new vertical radius option as follows:

```yaml
world: world

pvpZoneRadius: 60

pvpZoneVerticalRadius: 5

pvpZoneCenter:
 x: 23.0
 y: 62.0
 z: -157.0

pvpZoneSpectatorLocation:
 x: 23.0
 y: 72.0
 z: -157.0
```

### Blocking doors

If you want to block the doors of the PvP area, for example, to prevent players from escaping
the fight, you can configure the location of the blocks that will be placed in the doors to
block them as follows:

```yaml
world: world

doorMaterial: "RED_STAINED_GLASS"

doorLocations:
  door_1_1:     # Location of gate 1 where a pre-determined block will be placed
    x: 46.0
    y: 66.0
    z: -158.0
  door_1_2:     # Location of gate 1 where a pre-determined block will be placed
    x: 46.0
    y: 66.0
    z: -157.0
  door_2_1:    # Location of gate 2 where a pre-determined block will be placed
    x: 46.0
    y: 66.0
    z: -156.0
  door_2_2:    # Location of gate 2 where a pre-determined block will be placed
    x: 46.0
    y: 65.0
    z: -158.0
```

**Keep in mind that the locations you set must be free (air blocks), as at the start of the fight,
blocks of the specified type in `doorMaterial` will be placed at the indicated locations and at the
end of the fight, they will be removed by replacing them again with air.**

**The door area should be a protected area that players cannot break.
This plugin does not handle that.**

### Change invite timeout for PvP challenge acceptance

To change the waiting time for accepting or rejecting the PvP challenge, you can configure the
time in seconds as follows:

```yaml
inviteTimeout: 60 
```

### Prevent players from using some commands during PvP

You can prevent players involved in a PvP battle from using certain commands during the battle,
for example, to prevent them from escaping the battle, by blocking them as follows:

```yaml
blockedCommands:
  - "/back"
  - "/spawn"
  - "/tp"
  - "/home"
  - "/ps"
```

### Set health and saturation to maximum at the beginning of the PvP

If you want players to start the battle with maximum health and saturation, you can
configure the plugin as follows:

```yaml
setHealthAndSaturation: true
```

### Disable PvP commands in certain worlds

If you want players to be unable to execute the PvP plugin commands in certain worlds, you can
configure the plugin as follows:

```yaml
disabledWorlds:
  - spawn
  - world_nether
```

### Full configuration example

```yaml
# Name of the world where the PvP zone is located.
world: world

# Names of the worlds where the use of PvP commands will be blocked.
disabledWorlds:
  - spawn

# Commands that players will not be allowed to use during a PvP.
blockedCommands:
  - "/coliseo"
  - "/back"
  - "/spawn"
  - "/tp"
  - "/home"
  - "/ps"

# Time in seconds that it will take for a challenge to be canceled if not accepted.
inviteTimeout: 60 

# If enabled, players will start PVP with the maximum level of hunger and health.
setHealthAndSaturation: true

# The radius of the PvP zone.
pvpZoneRadius: 60

# Vertical radius of the PvP zone for player height restrictions.
pvpZoneVerticalRadius: 5

# The central point of the PvP zone.
pvpZoneCenter:
 x: 23.0
 y: 62.0
 z: -157.0

# The location where the player who proposes the challenge will be teleported to start the PvP.
pvpZonePlayerOne:
  x: 23.0
  y: 63.0
  z: -138.0

# Location where the player who accepts the challenge will be teleported to start the PvP.
pvpZonePlayerTwo:
  x: 23.0
  y: 63.0
  z: -176.0

# Location where players not involved in the PvP will be teleported.
pvpZoneSpectatorLocation:
  x: 23.0
  y: 72.0
  z: -157.0

# Material that will be used to cover the doors of the PvP zone.
doorMaterial: "RED_STAINED_GLASS"

# Locations of the blocks that will be blocked during the PvP.
doorLocations:
  door_1_1:
    x: 46.0
    y: 66.0
    z: -158.0
  door_1_2:
    x: 46.0
    y: 66.0
    z: -157.0
  door_1_3:
    x: 46.0
    y: 66.0
    z: -156.0
  door_1_4:
    x: 46.0
    y: 65.0
    z: -158.0
  
  door_2_1:
    x: 24.0
    y: 66.0
    z: -134.0
  door_2_2:
    x: 23.0
    y: 66.0
    z: -134.0
  door_2_3:
    x: 22.0
    y: 66.0
    z: -134.0
  door_2_4:
    x: 24.0
    y: 65.0
    z: -134.0
```

## Messages

The `plugins/PvP/messages.yml` file contains all the messages that the plugin uses.
You can change the messages to your liking.

## Stats

The `plugins/PvP/stats.yml` file stores the win and loss statistics of players
in PvP battles.

## TODO

- [ ] Add new feature
- [ ] Create tests
- [ ] Improve documentation

## Changelog

Detailed changes for each release are documented in the
[release notes](https://github.com/josantonius/minecraft-pvp/releases).

## Contribution

Please make sure to read the [Contributing Guide](.github/CONTRIBUTING.md), before making a pull
request, start a discussion or report a issue.

Thanks to all [contributors](https://github.com/josantonius/minecraft-pvp/graphs/contributors)! :heart:

## Sponsor

If this project helps you to reduce your development time,
[you can sponsor me](https://github.com/josantonius#sponsor) to support my open source work :blush:

## License

This repository is licensed under the [MIT License](LICENSE).

Copyright © 2023-present, [Josantonius](https://github.com/josantonius#contact)
