package dev.josantonius.minecraft.pvp

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class PvpConfig(plugin: JavaPlugin) {
    private val config: FileConfiguration
    private val world: World?
    private val disabledWorlds: List<String>
    private val pvpZoneRadius: Int?
    private val pvpZoneVerticalRadius: Int?
    private val pvpZoneCenter: Location?
    private val pvpZonePlayerOne: Location?
    private val pvpZonePlayerTwo: Location?
    private val pvpZoneSpectatorLocation: Location?
    private val doorLocations: MutableMap<Location, Material>?
    private val blockedCommands: List<String>
    private val doorMaterial: Material?
    private val inviteTimeout: Int
    private val setHealthAndSaturation: Boolean

    init {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        config = plugin.config

        world = getWorldFromConfig("world", plugin)
        blockedCommands = config.getStringList("blockedCommands")
        disabledWorlds = config.getStringList("disabledWorlds")
        pvpZonePlayerOne = getLocationFromConfig("pvpZonePlayerOne")
        pvpZonePlayerTwo = getLocationFromConfig("pvpZonePlayerTwo")
        pvpZoneCenter = getLocationFromConfig("pvpZoneCenter")
        pvpZoneSpectatorLocation = getLocationFromConfig("pvpZoneSpectatorLocation")
        pvpZoneRadius = config.getNullableInt("pvpZoneRadius")
        pvpZoneVerticalRadius = config.getNullableInt("pvpZoneVerticalRadius")
        doorLocations = setDoorLocations()
        doorMaterial = config.getNullableString("doorMaterial")?.let { Material.getMaterial(it) }
        inviteTimeout = config.getInt("inviteTimeout", 60)
        setHealthAndSaturation = config.getBoolean("setHealthAndSaturation", false)
    }

    fun getPvpZonePlayerOne(): Location? {
        return pvpZonePlayerOne
    }

    fun getPvpZonePlayerTwo(): Location? {
        return pvpZonePlayerTwo
    }

    fun getPvpZoneCenter(): Location? {
        return pvpZoneCenter
    }

    fun getPvpZoneRadius(): Int? {
        return pvpZoneRadius
    }

    fun getPvpZoneVerticalRadius(): Int? {
        return pvpZoneVerticalRadius
    }

    fun getPvpZoneSpectatorLocation(): Location? {
        return pvpZoneSpectatorLocation
    }

    fun getDoorLocations(): Map<Location, Material>? {
        return doorLocations
    }

    fun getBlockedCommands(): List<String> {
        return blockedCommands
    }

    fun getDoorMaterial(): Material? {
        return doorMaterial
    }

    fun getInviteTimeout(): Int {
        return inviteTimeout
    }

    fun isWorldDisabled(worldName: String): Boolean {
        return disabledWorlds.contains(worldName)
    }

    fun shouldSetHealthAndSaturation(): Boolean {
        return setHealthAndSaturation
    }

    private fun getWorldFromConfig(path: String, plugin: JavaPlugin): World? {
        val worldName = config.getString(path)
        return if (worldName != null && worldName.isNotEmpty()) plugin.server.getWorld(worldName)
        else null
    }

    private fun ConfigurationSection.getNullableString(path: String): String? {
        return if (contains(path)) getString(path) else null
    }

    private fun ConfigurationSection.getNullableInt(path: String): Int? {
        return if (contains(path)) getInt(path) else null
    }

    private fun getLocationFromConfig(configKey: String): Location? {
        return if (world != null && config.contains(configKey)) {
            Location(
                    world,
                    config.getDouble("$configKey.x"),
                    config.getDouble("$configKey.y"),
                    config.getDouble("$configKey.z")
            )
        } else {
            null
        }
    }

    private fun setDoorLocations(): MutableMap<Location, Material>? {
        return if (world != null) {
            val doorLocations = HashMap<Location, Material>()
            val doorLocationsSection = config.getConfigurationSection("doorLocations")
            if (doorLocationsSection != null) {
                for (key in doorLocationsSection.getKeys(false)) {
                    val doorLocation = getLocationFromConfig("doorLocations.$key")
                    doorLocation?.let { doorLocations[doorLocation] = doorLocation.block.type }
                }
            }
            doorLocations
        } else {
            null
        }
    }
}
