package dev.josantonius.minecraft.pvp.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class PvpTabCompleter : TabCompleter {

    override fun onTabComplete(
            sender: CommandSender,
            cmd: Command,
            label: String,
            args: Array<String>
    ): List<String>? {
        if (sender !is Player) {
            return null
        }

        if (cmd.name.equals("pvp", ignoreCase = true)) {
            if (args.size == 1) {
                val playerNames = Bukkit.getOnlinePlayers().map { it.name }
                return playerNames
            }
        } else if (cmd.name.equals("pvpaccept", ignoreCase = true) ||
                        cmd.name.equals("pvpdeny", ignoreCase = true) ||
                        cmd.name.equals("pvpcancel", ignoreCase = true) ||
                        cmd.name.equals("pvphelp", ignoreCase = true) ||
                        cmd.name.equals("pvptop", ignoreCase = true) ||
                        cmd.name.equals("pvpreload", ignoreCase = true)
        ) {
            return ArrayList()
        }

        return null
    }
}
