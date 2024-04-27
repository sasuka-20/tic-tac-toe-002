package com.example.tic_tac_toe_002

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.example.tic_tac_toe_002.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityGameBinding
    private var playerXName: String = ""
    private var playerOName: String = ""

    private var gameModel: GameModel? = null
    private var lastClickedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerXName = intent.getStringExtra("playerXName") ?: "Player X"
        playerOName = intent.getStringExtra("playerOName") ?: "Player O"

        initializeGame()

        // Set OnClickListener for each button
        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)

        // Set OnClickListener for the "Start game" button
        binding.startGameBtn.setOnClickListener {
            startGame()
        }

        binding.undoMoveBtn.setOnClickListener {
            undoLastMove()
        }

        GameData.gameModel.observe(this) {
            gameModel = it
            setUI()
            setPlayerNames()
        }
    }

    private fun initializeGame() {
        // Initialize game state
        GameData.saveGameModel(
            GameModel(
                gameId = "1", // Set a default game ID or generate one
                filledPos = MutableList(9) { "" }, // Initialize empty board
                gameStatus = GameStatus.CREATED,
                currentPlayer = "X", // Player X starts first
                playerXName = playerXName,
                playerOName = playerOName
            )
        )
    }

    private fun setUI() {
        gameModel?.apply {
            // Update UI elements based on game state
            binding.btn0.text = filledPos[0]
            binding.btn1.text = filledPos[1]
            binding.btn2.text = filledPos[2]
            binding.btn3.text = filledPos[3]
            binding.btn4.text = filledPos[4]
            binding.btn5.text = filledPos[5]
            binding.btn6.text = filledPos[6]
            binding.btn7.text = filledPos[7]
            binding.btn8.text = filledPos[8]

            binding.gameStatusText.text =
                when (gameStatus) {
                    GameStatus.CREATED -> "Game ID: $gameId"
                    GameStatus.JOINED -> "Waiting for other player to start"
                    GameStatus.INPROGRESS -> "$currentPlayer's turn"
                    GameStatus.FINISHED -> if (winner.isNotEmpty()) "$winner wins!" else "It's a draw!"
                }
        }
    }

    private fun setPlayerNames() {
        gameModel?.apply {
            binding.playerXNameTextView.text = "$playerXName: $pointsX"
            binding.playerONameTextView.text = "$playerOName: $pointsO"
        }
    }

    fun startGame() {
        gameModel?.apply {
            updateGameData(
                GameModel(
                    gameId = gameId,
                    filledPos = MutableList(9) { "" }, // Clear the board
                    gameStatus = GameStatus.INPROGRESS,
                    currentPlayer = "X", // Reset current player to X
                    playerXName = playerXName,
                    playerOName = playerOName,
                    pointsX = pointsX, // Preserve points for player X
                    pointsO = pointsO // Preserve points for player O
                )
            )

            // Update UI after starting the game
            setUI()
        }
    }

    fun updateGameData(model: GameModel) {
        GameData.saveGameModel(model)
    }

    fun checkForWinner() {
        val winningPos = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6)
        )

        gameModel?.apply {
            for (i in winningPos) {
                if (
                    filledPos[i[0]] == filledPos[i[1]] &&
                    filledPos[i[1]] == filledPos[i[2]] &&
                    filledPos[i[0]].isNotEmpty()
                ) {
                    gameStatus = GameStatus.FINISHED
                    winner = filledPos[i[0]]

                    // Increment points for the winner
                    if (winner == "X") {
                        pointsX++
                    } else {
                        pointsO++
                    }

                    val winnerMessage = if (winner == "X") {
                        "Player X wins!"
                    } else {
                        "Player O wins!"
                    }

                    // Show popup message
                    showWinnerPopup(winnerMessage, winner)

                    updateGameData(this)
                    return
                }
            }

            if (filledPos.none { it.isEmpty() }) {
                gameStatus = GameStatus.FINISHED
                showWinnerPopup("It's a draw!", "")
            }

            updateGameData(this)
        }
    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            if (gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(applicationContext, "Game not started", Toast.LENGTH_SHORT).show()
                return
            }

            val clickedPos = (v?.tag as String).toInt()
            if (filledPos[clickedPos].isEmpty()) {
                lastClickedPosition = clickedPos // Update the last clicked position
                filledPos[clickedPos] = currentPlayer
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                checkForWinner()
                updateGameData(this)
            }
        }
    }

    private fun showWinnerPopup(message: String, winner: String) {
        val winnerName = if (winner.isNotEmpty()) {
            if (winner == "X") playerXName else playerOName
        } else {
            "Draw"
        }
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("$message\nWinner: $winnerName")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun undoLastMove() {
        gameModel?.apply {
            if (gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(applicationContext, "Game not started", Toast.LENGTH_SHORT).show()
                return
            }

            if (lastClickedPosition == -1) {
                Toast.makeText(applicationContext, "No moves to undo", Toast.LENGTH_SHORT).show()
                return
            }

            filledPos[lastClickedPosition] = ""
            currentPlayer = if (currentPlayer == "X") "O" else "X"
            gameStatus = GameStatus.INPROGRESS

            lastClickedPosition = -1

            setUI()
            updateGameData(this)
        }
    }
}