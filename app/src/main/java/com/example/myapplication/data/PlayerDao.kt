package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert
    suspend fun insertPlayer(player: Player): Long

    @Insert
    suspend fun insertScore(score: Score)

    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<Player>>

    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 10")
    fun getTopTenScores(): Flow<List<Score>>

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Int): Player?
}