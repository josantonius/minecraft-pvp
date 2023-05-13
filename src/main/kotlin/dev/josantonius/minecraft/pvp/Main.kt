package dev.josantonius.minecraft.pvp

import dev.josantonius.minecraft.messaging.Message
import dev.josantonius.minecraft.pvp.command.PvpCommandExecutor
import dev.josantonius.minecraft.pvp.command.PvpTabCompleter
import java.io.File
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class Main : JavaPlugin(), Listener {
    lateinit var configuration: PvpConfig
    lateinit var message: Message
    private var inviteTimeoutCheckTask: BukkitTask? = null
    private lateinit var pvpManager: PvpManager
    private var nonInvolvedPlayersCheckTask: BukkitTask? = null
    override fun onEnable() {
        load()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val disconnectedPlayer = event.player
        if (pvpManager.isPlayerInvolved(disconnectedPlayer)) {
            val disconnectedPlayerUUID = disconnectedPlayer.uniqueId
            val otherPlayerUUID =
                    pvpManager.inviterUuid?.let { inviterUuid ->
                        if (disconnectedPlayerUUID == inviterUuid) pvpManager.inviteeUuid
                        else inviterUuid
                    }
            if (otherPlayerUUID != null) {
                val otherPlayer = Bukkit.getPlayer(otherPlayerUUID)
                if (otherPlayer != null) {
                    pvpManager.cancelPvpOnDisconnect(otherPlayer, disconnectedPlayer)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val deadPlayer = event.entity
        if (pvpManager.isPlayerInvolved(deadPlayer)) {
            val otherPlayerUuid =
                    if (deadPlayer.uniqueId == pvpManager.inviterUuid) pvpManager.inviteeUuid
                    else pvpManager.inviterUuid

            val otherPlayer = otherPlayerUuid?.let { Bukkit.getPlayer(it) }
            if (otherPlayer != null) {
                pvpManager.endPvp(otherPlayer, deadPlayer)
            }
        }
    }

    @EventHandler
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        if (event.isCancelled) {
            return
        }
        val player = event.player
        if (pvpManager.isPlayerInvolved(player)) {
            val command = event.message.lowercase()
            if (!player.hasPermission("pvp.admin") && isCommandBlocked(command)) {
                event.isCancelled = true
                message.sendToPlayer(player, "error.command.not_allowed")
            }
        }
    }

    fun load() {
        val messagesFile = File(dataFolder, "messages.yml")
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false)
        }

        message = Message(messagesFile, this)
        message.setConsolePrefix("<blue>[<dark_aqua>PvP<blue>] <white>")
        message.setChatPrefix("<blue>[<dark_aqua>PvP<blue>] <white>")
        configuration = PvpConfig(this)

        val pvpStat = PvpStat(this)
        pvpManager = PvpManager(this, pvpStat)

        HandlerList.getHandlerLists().forEach { handlerList ->
            handlerList.unregister(this as Listener)
        }
        server.pluginManager.registerEvents(this, this)

        val pvpTabCompleter = PvpTabCompleter()
        val pvpCommandExecutor = PvpCommandExecutor(this, pvpManager)

        getCommand("pvp")?.setTabCompleter(pvpTabCompleter)
        getCommand("pvp")?.setExecutor(pvpCommandExecutor)
        getCommand("pvpaccept")?.setTabCompleter(pvpTabCompleter)
        getCommand("pvpaccept")?.setExecutor(pvpCommandExecutor)
        getCommand("pvpdeny")?.setTabCompleter(pvpTabCompleter)
        getCommand("pvpdeny")?.setExecutor(pvpCommandExecutor)
        getCommand("pvpcancel")?.setTabCompleter(pvpTabCompleter)
        getCommand("pvpcancel")?.setExecutor(pvpCommandExecutor)
        getCommand("pvphelp")?.setTabCompleter(pvpTabCompleter)
        getCommand("pvphelp")?.setExecutor(pvpCommandExecutor)
        getCommand("pvptop")?.setTabCompleter(pvpTabCompleter)
        getCommand("pvptop")?.setExecutor(pvpCommandExecutor)
        getCommand("pvpreload")?.setTabCompleter(pvpTabCompleter)
        getCommand("pvpreload")?.setExecutor(pvpCommandExecutor)
    }

    fun reload(sender: CommandSender) {
        load()
        sendMessage(sender, "plugin.reloaded")
    }

    fun scheduleInviteTimeoutCheck() {
        inviteTimeoutCheckTask =
                object : BukkitRunnable() {
                            override fun run() {
                                pvpManager.checkInviteTimeout()
                            }
                        }
                        .runTaskTimer(this, 0L, 20L * 5L)
    }

    fun cancelInviteTimeoutCheck() {
        inviteTimeoutCheckTask?.cancel()
        inviteTimeoutCheckTask = null
    }

    fun scheduleNonInvolvedPlayersCheck() {
        nonInvolvedPlayersCheckTask =
                object : BukkitRunnable() {
                            override fun run() {
                                pvpManager.checkNonInvolvedPlayers()
                            }
                        }
                        .runTaskTimer(this, 0L, 20L)
    }

    fun cancelNonInvolvedPlayersCheck() {
        nonInvolvedPlayersCheckTask?.cancel()
        nonInvolvedPlayersCheckTask = null
    }

    fun sendMessage(sender: CommandSender, key: String, vararg params: String) {
        if (sender is Player) {
            message.sendToPlayer(sender, key, *params)
        } else {
            message.sendToConsole(key, *params)
        }
    }

    private fun isCommandBlocked(command: String): Boolean {
        val blockedCommands = configuration.getBlockedCommands()
        return blockedCommands.any { command.startsWith(it) }
    }
}
