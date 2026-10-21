package com.tournamenttracker.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tournamenttracker.app.domain.model.Round

/**
 * A single match inside a bracket. A bye is represented by one of the player
 * columns being null; the present player wins automatically.
 */
@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player1Id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player2Id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["winnerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("tournamentId"),
        Index("player1Id"),
        Index("player2Id"),
        Index("winnerId"),
    ],
)
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    /** 0-based structural round, used for winner progression. */
    val roundNumber: Int,
    /** Display name of the round ("Quarter Finals", "Finals", ...). */
    val round: Round,
    val matchIndex: Int,
    val player1Id: Long? = null,
    val player2Id: Long? = null,
    val winnerId: Long? = null,
)
