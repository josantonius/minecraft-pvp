package dev.josantonius.minecraft.pvp

import java.util.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PvpManager(private val plugin: Main, private val pvpStat: PvpStat) {
    private var inviter: Player? = null
    private var invitee: Player? = null
    private var pvpRequested = false
    private var pvpStarted = false
    private var inviteTimestamp: Long = 0

    val inviterUuid: UUID?
        get() = inviter?.uniqueId

    val inviteeUuid: UUID?
        get() = invitee?.uniqueId

    val inviterName: String
        get() = inviter?.name ?: ""

    val inviteeName: String
        get() = invitee?.name ?: ""

    val isPvpRequested: Boolean
        get() = pvpRequested

    init {
        pvpStarted = false
        pvpRequested = false
    }

    fun requestPvp(inviter: Player, invitee: Player): Boolean {
        if (pvpRequested) {
            plugin.sendMessage(
                    inviter,
                    "error.challenge.already_pending",
                    inviter.name,
                    invitee.name
            )
            return false
        } else if (pvpStarted) {
            plugin.sendMessage(
                    inviter,
                    "error.challenge.already_active",
                    inviter.name,
                    invitee.name
            )
            return false
        } else if (!inviter.hasPermission("pvp.admin") && inviter.uniqueId == invitee.uniqueId) {
            plugin.sendMessage(inviter, "error.challenge.yourself")
            return false
        }

        pvpRequested = true
        inviteTimestamp = System.currentTimeMillis()

        plugin.message.sendToPlayers("announcement.challenge", inviter.name, invitee.name)
        plugin.sendMessage(inviter, "clickable.component.cancel", "/pvpcancel")
        plugin.sendMessage(invitee, "clickable.component.accept_deny", "/pvpaccept", "/pvpdeny")

        plugin.scheduleInviteTimeoutCheck()

        this.inviter = inviter
        this.invitee = invitee

        return true
    }

    fun acceptPvp(player: Player): Boolean {
        if (!pvpRequested || player.uniqueId != invitee?.uniqueId) {
            plugin.sendMessage(player, "error.challenge.to_accept")
            return false
        } else if (pvpStarted && player.uniqueId == invitee?.uniqueId) {
            plugin.sendMessage(player, "error.challenge.already_accepted")
            return false
        }

        plugin.message.sendToPlayers("announcement.accepted", inviteeName, inviterName)

        pvpRequested = true
        inviteTimestamp = 0
        startPvp()

        return true
    }

    fun denyPvp(player: Player): Boolean {
        if (!pvpRequested || player.uniqueId != invitee?.uniqueId) {
            plugin.sendMessage(player, "error.challenge.to_deny")
            return false
        } else if (pvpStarted) {
            plugin.sendMessage(player, "error.challenge.started_reject")
            return false
        }
        plugin.message.sendToPlayers("announcement.denied", inviteeName, inviterName)

        resetPvp()
        return true
    }

    fun checkInviteTimeout() {
        if (!pvpRequested) {
            return
        }

        val timeoutMillis = plugin.configuration.getInviteTimeout() * 1000L

        if (inviteTimestamp != 0L && System.currentTimeMillis() - inviteTimestamp > timeoutMillis) {
            if (invitee != null && inviter != null) {
                plugin.message.sendToPlayers(
                        "error.challenge.invite_timeout",
                        inviteeName,
                        inviterName
                )
            }
            resetPvp()
        }
    }

    fun cancelPvp(player: CommandSender): Boolean {
        if (!pvpRequested) {
            plugin.sendMessage(player, "error.challenge.to_cancel")
            return false
        }
        if (player.hasPermission("pvp.admin")) {
            plugin.message.sendToPlayers("announcement.canceled", inviterName, inviteeName)
            resetPvp()
            return true
        }
        if (player is Player && player.uniqueId != inviter?.uniqueId) {
            plugin.sendMessage(player, "error.challenge.cannot_cancel", inviterName)
            return false
        }
        if (pvpStarted) {
            plugin.sendMessage(player, "error.challenge.started_cancel")
            return false
        }

        plugin.message.sendToPlayers("announcement.player_canceled", inviterName, inviteeName)
        resetPvp()
        return true
    }

    private fun startPvp() {
        if (pvpRequested && inviter != null && invitee != null) {
            val pvpZoneCenter = plugin.configuration.getPvpZoneCenter()
            val pvpZonePlayerOne = plugin.configuration.getPvpZonePlayerOne()
            val pvpZonePlayerTwo = plugin.configuration.getPvpZonePlayerTwo()

            teleportNonInvolvedPlayers()
            if (pvpZonePlayerOne != null && pvpZonePlayerTwo != null) {
                val inviterLocation = pvpZonePlayerOne.clone()
                if (pvpZoneCenter != null) {
                    val inviterDirection =
                            pvpZoneCenter
                                    .toVector()
                                    .subtract(inviterLocation.toVector())
                                    .normalize()
                    inviterLocation.direction = inviterDirection
                }
                inviter?.teleport(inviterLocation)

                val inviteeLocation = pvpZonePlayerTwo.clone()
                if (pvpZoneCenter != null) {
                    val inviteeDirection =
                            pvpZoneCenter
                                    .toVector()
                                    .subtract(inviteeLocation.toVector())
                                    .normalize()
                    inviteeLocation.direction = inviteeDirection
                }
                invitee?.teleport(inviteeLocation)
            } else {
                inviter?.let { inviterPlayer ->
                    invitee?.let { inviteePlayer ->
                        val inviterLocation = inviterPlayer.location
                        val nearbyLocation = getRandomNearbyLocation(inviterLocation)
                        inviteePlayer.teleport(nearbyLocation)
                    }
                }
            }
            val doorLocations = plugin.configuration.getDoorLocations()
            val doorMaterial = plugin.configuration.getDoorMaterial()

            if (doorLocations != null && doorLocations.isNotEmpty() && doorMaterial != null) {
                for (doorLocation in doorLocations.keys) {
                    doorLocation.block.type = doorMaterial
                    doorLocation.world.refreshChunk(
                            doorLocation.blockX shr 4,
                            doorLocation.blockZ shr 4
                    )
                }
            }

            if (plugin.configuration.shouldSetHealthAndSaturation()) {
                inviter?.let {
                    val maxHealth = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                    it.health = maxHealth
                    it.foodLevel = 20
                    it.saturation = 20f
                }
                invitee?.let {
                    val maxHealth = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                    it.health = maxHealth
                    it.foodLevel = 20
                    it.saturation = 20f
                }
            }

            plugin.message.sendToPlayers("announcement.started", inviterName, inviteeName)

            plugin.scheduleNonInvolvedPlayersCheck()
            pvpStarted = true
        }
    }

    fun endPvp(winner: Player, loser: Player) {
        if (pvpStarted) {
            plugin.message.sendToPlayers("announcement.winner", winner.name, loser.name)

            pvpStat.updatePlayerStats(winner.uniqueId, loser.uniqueId)
        } else {
            plugin.message.sendToPlayers("announcement.death_canceled", loser.name, winner.name)
        }
        resetPvp()
    }

    fun cancelPvpOnDisconnect(winner: Player, loser: Player) {
        if (pvpStarted) {
            plugin.message.sendToPlayers("announcement.disconnect_wins", loser.name, winner.name)
            pvpStat.updatePlayerStats(winner.uniqueId, loser.uniqueId)
        } else {
            plugin.message.sendToPlayers(
                    "announcement.disconnect_canceled",
                    loser.name,
                    winner.name
            )
        }
        resetPvp()
    }

    fun isPlayerInvolved(player: Player): Boolean {
        if (inviter == null || invitee == null) {
            return false
        }
        return inviter?.uniqueId == player.uniqueId || invitee?.uniqueId == player.uniqueId
    }

    fun checkNonInvolvedPlayers() {
        if (pvpStarted) {
            teleportNonInvolvedPlayers()
        }
    }

    fun showHelp(player: CommandSender) {
        plugin.sendMessage(player, "help.header")
        plugin.sendMessage(player, "help.pvp", "/pvp <player>")
        plugin.sendMessage(player, "help.pvpaccept", "/pvpaccept")
        plugin.sendMessage(player, "help.pvpdeny", "/pvpdeny")
        plugin.sendMessage(player, "help.pvpcancel", "/pvpcancel")
        plugin.sendMessage(player, "help.pvptop", "/pvptop")
        if (player.hasPermission("pvp.admin")) {
            plugin.sendMessage(player, "help.pvpreload", "/pvpreload")
        }
    }

    fun showStats(player: CommandSender) {
        pvpStat.showStats(player)
    }

    fun teleportToSpecatorZone(player: Player) {
        val pvpZoneSpectatorLocation = plugin.configuration.getPvpZoneSpectatorLocation()
        if (pvpZoneSpectatorLocation != null) {
            player.teleport(pvpZoneSpectatorLocation)
        }
    }

    private fun teleportNonInvolvedPlayers() {
        val pvpZoneCenter = plugin.configuration.getPvpZoneCenter()
        val pvpZoneRadius = plugin.configuration.getPvpZoneRadius()
        val pvpZoneSpectatorLocation = plugin.configuration.getPvpZoneSpectatorLocation()
        val pvpZoneVerticalRadius = plugin.configuration.getPvpZoneVerticalRadius()

        if (pvpZoneCenter != null && pvpZoneRadius != null && pvpZoneSpectatorLocation != null) {
            for (player in Bukkit.getOnlinePlayers()) {
                if ((!isPlayerInvolved(player) && !player.hasPermission("pvp.admin")) &&
                                player.world == pvpZoneCenter.world &&
                                player.location.y >= pvpZoneCenter.y &&
                                player.location.y <=
                                        pvpZoneCenter.y + (pvpZoneVerticalRadius ?: 2) &&
                                player.location.distance(pvpZoneCenter) <= pvpZoneRadius
                ) {
                    player.teleport(pvpZoneSpectatorLocation)
                }
            }
        }
    }

    private fun resetPvp() {
        inviter = null
        invitee = null
        inviteTimestamp = 0
        pvpRequested = false
        pvpStarted = false

        val doorLocations = plugin.configuration.getDoorLocations()
        if (doorLocations != null && doorLocations.isNotEmpty()) {
            for (doorLocation in doorLocations.keys) {
                doorLocation.block.type = Material.AIR
                doorLocation.world.refreshChunk(
                        doorLocation.blockX shr 4,
                        doorLocation.blockZ shr 4
                )
            }
        }

        plugin.cancelInviteTimeoutCheck()
        plugin.cancelNonInvolvedPlayersCheck()
    }

    private fun getRandomNearbyLocation(location: Location): Location {
        val random = Random()
        val nearbyX = location.x + (random.nextDouble() * 10.0) - 5.0
        val nearbyZ = location.z + (random.nextDouble() * 10.0) - 5.0
        val highestBlockY = location.world.getHighestBlockYAt(nearbyX.toInt(), nearbyZ.toInt())
        val nearbyY = (highestBlockY + 2).toDouble()

        return Location(location.world, nearbyX, nearbyY, nearbyZ)
    }
}
