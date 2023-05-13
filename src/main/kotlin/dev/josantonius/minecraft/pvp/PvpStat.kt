package dev.josantonius.minecraft.pvp

import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration

class PvpStat(private val plugin: Main) {
    private val playerStatsMap = HashMap<UUID, PvpPlayerStat>()
    private lateinit var statsFile: File
    private lateinit var statsConfig: FileConfiguration

    init {
        setupStatsFile(plugin)
        loadStats()
    }

    fun getPlayerStats(playerId: UUID): PvpPlayerStat {
        return playerStatsMap.getOrDefault(playerId, PvpPlayerStat())
    }

    fun setPlayerStats(playerId: UUID, stats: PvpPlayerStat) {
        playerStatsMap[playerId] = stats
    }

    fun getAllPlayerStats(): Map<UUID, PvpPlayerStat> {
        return HashMap(playerStatsMap)
    }

    fun updatePlayerStats(winnerId: UUID, loserId: UUID) {
        val winnerStats = getPlayerStats(winnerId)
        val loserStats = getPlayerStats(loserId)
        winnerStats.incrementWins()
        loserStats.incrementLosses()
        setPlayerStats(winnerId, winnerStats)
        setPlayerStats(loserId, loserStats)
        saveStats()
    }

    private fun setupStatsFile(plugin: Main) {
        statsFile = File(plugin.dataFolder, "stats.yml")
        if (!statsFile.exists()) {
            plugin.saveResource("stats.yml", false)
        }
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile()
            } catch (e: IOException) {
                Bukkit.getLogger().severe("Error creating stats.yml file: " + e.message)
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile)
    }

    fun loadStats() {
        val statsSection = statsConfig.getConfigurationSection("stats") ?: return
        for (uuidString in statsSection.getKeys(false)) {
            val uuid = UUID.fromString(uuidString)
            val wins = statsSection.getInt("$uuidString.wins")
            val losses = statsSection.getInt("$uuidString.losses")
            val stats = PvpPlayerStat()
            stats.wins = wins
            stats.losses = losses
            playerStatsMap[uuid] = stats
        }
    }

    fun saveStats() {
        for ((uuid, stats) in playerStatsMap) {
            statsConfig["stats.$uuid.wins"] = stats.wins
            statsConfig["stats.$uuid.losses"] = stats.losses
        }
        try {
            statsConfig.save(statsFile)
            loadStats()
        } catch (e: IOException) {
            Bukkit.getLogger().severe("Error saving stats.yml file: " + e.message)
        }
    }

    fun showStats(player: CommandSender) {
        val allStats = getAllPlayerStats()
        val nameStatsMap = HashMap<String, PvpPlayerStat>()

        for ((uuid, value) in allStats) {
            val p = Bukkit.getOfflinePlayer(uuid)
            nameStatsMap[p.name ?: "Unknown"] = value
        }

        val sortedStats =
                nameStatsMap.entries.sortedWith { e1, e2 ->
                    val stats1 = e1.value
                    val stats2 = e2.value
                    stats2.wins.compareTo(stats1.wins)
                }

        plugin.sendMessage(player, "stat.header")
        for ((index, entry) in sortedStats.withIndex()) {
            if (index >= 15) break

            val playerName = entry.key
            val stats = entry.value

            plugin.sendMessage(
                    player,
                    "stat.player",
                    playerName,
                    stats.wins.toString(),
                    stats.losses.toString()
            )
        }
    }
}
