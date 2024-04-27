package com.example.tic_tac_toe_002
import kotlin.random.Random

data class GameModel (
    var gameId : String = "-1",
    var filledPos : MutableList<String> = mutableListOf("","","","","","","","",""),
    var winner : String ="",
    var gameStatus : GameStatus = GameStatus.CREATED,
    var pointsX: Int = 0, // Points for player X
    var pointsO: Int = 0  ,// Points for player O
    var currentPlayer : String = (arrayOf("X","O"))[Random.nextInt(2)],
    var playerXName: String = "", // Player X name
    var playerOName: String = ""  // Player O name
)



enum class GameStatus{
    CREATED,
    JOINED,
    INPROGRESS,
    FINISHED
}