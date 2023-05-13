package dev.josantonius.minecraft.pvp

class PvpPlayerStat {
    var wins: Int = 0
    var losses: Int = 0

    fun incrementWins() {
        wins++
    }

    fun incrementLosses() {
        losses++
    }
}
