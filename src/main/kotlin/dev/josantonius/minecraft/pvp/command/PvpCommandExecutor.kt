package dev.josantonius.minecraft.pvp.command

import dev.josantonius.minecraft.pvp.Main
import dev.josantonius.minecraft.pvp.PvpManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PvpCommandExecutor(private val plugin: Main, private val pvpManager: PvpManager) :
        CommandExecutor {
    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<String>
    ): Boolean {

        val baseCommand = command.name.lowercase()

        when (baseCommand) {
            "pvpreload" -> {
                if (sender.hasPermission("pvp.admin")) {
                    plugin.reload(sender)
                    return true
                } else {
                    plugin.sendMessage(sender, "error.command.permission")
                    return false
                }
            }
            "pvp", "pvpaccept", "pvpdeny" -> {
                if (sender !is Player) {
                    plugin.sendMessage(sender, "error.command.for_players")
                    return false
                }
                if (!sender.hasPermission("pvp.admin") &&
                                plugin.configuration.isWorldDisabled(sender.world.name)
                ) {
                    plugin.sendMessage(sender, "error.command.not_executable")
                    return false
                }
            }
        }

        if (!sender.hasPermission("pvp.use")) {
            plugin.sendMessage(sender, "error.command.permission")
            return false
        }

        when (baseCommand) {
            "pvp" -> {
                if (args.size != 1) {
                    plugin.sendMessage(sender, "error.command.usage", "/pvp <player>")
                    return false
                }
                val target = Bukkit.getPlayer(args[0])
                if (target == null) {
                    plugin.sendMessage(sender, "error.player.offline")
                    return false
                }
                pvpManager.requestPvp(sender as Player, target)
            }
            "pvpaccept" -> pvpManager.acceptPvp(sender as Player)
            "pvpdeny" -> pvpManager.denyPvp(sender as Player)
            "pvpcancel" -> pvpManager.cancelPvp(sender)
            "pvphelp" -> pvpManager.showHelp(sender)
            "pvptop" -> pvpManager.showStats(sender)
            else -> {
                plugin.sendMessage(sender, "error.command.invalid")
                return false
            }
        }
        return true
    }
}
