package com.tournamenttracker.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TournamentStatus {
    IN_PROGRESS,
    PAUSED,
    DONE,
}

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val status: TournamentStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val championId: Long? = null,
    val bracketSize: Int,
)
