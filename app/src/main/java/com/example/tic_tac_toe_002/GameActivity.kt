package com.example.tic_tac_toe_002

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.tic_tac_toe_002.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityGameBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var playerXName: String = ""
    private var playerOName: String = ""
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var btnDefaultMusic: MediaPlayer
    private var gameModel: GameModel? = null
    private var lastClickedPosition: Int = -1
    private var isMuted: Boolean = false
    private var isSoundOff : Boolean = false
    private var settingPopup : Dialog? = null
    private var winningPopup : Dialog? = null
    private lateinit var winingMusic: MediaPlayer
    private lateinit var DrawMusic: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("HangmanPrefs", Context.MODE_PRIVATE)

        isMuted = sharedPreferences.getBoolean("isMuted", false)
        isSoundOff = sharedPreferences.getBoolean("isSoundOff", false)

        val mediaPlayerManager = MediaPlayerManager
        mediaPlayer = mediaPlayerManager.getMediaPlayer(this)

        btnDefaultMusic = MediaPlayer.create(this, R.raw.button)
        winingMusic = MediaPlayer.create(this, R.raw.game_won)
        DrawMusic = MediaPlayer.create(this, R.raw.game_lost)

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

        binding.OBtn.text = "O"
        binding.XBtn.text = "X"

        binding.startGameBtn.setOnClickListener {
            if(!isSoundOff) {
                btnDefaultMusic.start()
            }
            startGame()
            binding.startGameBtn.visibility = View.INVISIBLE
        }

        binding.undoMoveBtn.setOnClickListener {
            if(!isSoundOff) {
                btnDefaultMusic.start()
            }
            undoLastMove()
        }

        binding.SettingsBtn.setOnClickListener {
            if(!isSoundOff){
                btnDefaultMusic.start()
            }
            showCustomDialog()
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

                    var name = ""

                    // Increment points for the winner
                    if (winner == "X") {
                        pointsX++
                        name = playerXName
                    } else {
                        pointsO++
                        name = playerOName
                    }

                    val winnerMessage = if (winner == "X") {
                        "Player X wins!"
                    } else {
                        "Player O wins!"
                    }
                    if(!isSoundOff){
                        winingMusic.start()
                    }
                    // Show popup message
                    showWinnerPopup(winnerMessage, name, "won")

                    updateGameData(this)
                    return
                }
            }

            if (filledPos.none { it.isEmpty() }) {
                if(!isSoundOff){
                    DrawMusic.start()
                }
                gameStatus = GameStatus.FINISHED
                showWinnerPopup("It's a draw!", "", "draw")

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

    private fun showWinnerPopup(message: String, winner: String, status : String) {

        val dialog = Dialog(this, R.style.CustomDialogTheme)
        winningPopup = dialog
        dialog.setContentView(R.layout.result_popup_activity)

        val close_btn : ImageView = dialog.findViewById(R.id.close_btn)

        close_btn.setOnClickListener{
            if(!isSoundOff){
                btnDefaultMusic.start()
            }
            dialog.hide()
        }

        val title_txt : TextView = dialog.findViewById(R.id.title_txt_popup)
        val status_txt : TextView = dialog.findViewById(R.id.status_txt_popup)
        val status_para : TextView = dialog.findViewById(R.id.status_para_popup)



        if (status.equals("won")){
            title_txt.text = "WON !"
            status_txt.text = winner + " Won"
            status_para.text = "Congrats on being the undisputed champion of pressing buttons like a pro."
        }else if (status.equals("draw")){
            title_txt.text = "Draw !"
            status_txt.text = "It’s a Draw!"
            status_para.text = "Congrats to both of you for equally excelling in the art of not winning."
        }

        dialog.show()
        binding.startGameBtn.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

        settingPopup?.dismiss()
        winningPopup?.dismiss()

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

    private fun updateMuteSwitch(sound_switch : Switch) {
        if(isMuted){
            sound_switch.isChecked = false
        }else{
            sound_switch.isChecked = true
        }
    }
    private fun toggleMuteState() {
        if (isMuted) {
            mediaPlayer.setVolume(0.7f, 0.7f)
        } else {
            mediaPlayer.setVolume(0f, 0f)
        }
        isMuted = !isMuted
        saveIsMutedToPrefs(isMuted)
    }

    private fun updateSoundOffSwitch(sound_switch : Switch) {
        if(isSoundOff){
            sound_switch.isChecked = false
        }else{
            sound_switch.isChecked = true
        }
    }

    private fun showCustomDialog() {
        val dialog = Dialog(this, R.style.CustomDialogTheme)
        settingPopup = dialog
        dialog.setContentView(R.layout.settings_popup_activity)

        val close_btn : ImageView = dialog.findViewById(R.id.close_btn)

        close_btn.setOnClickListener{
            if(!isSoundOff){
                btnDefaultMusic.start()
            }
            dialog.hide()
        }

        val sound_switch : Switch = dialog.findViewById(R.id.sound_switch)

        updateMuteSwitch(sound_switch)

        sound_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                isMuted = true
            }else{
                isMuted = false
            }
            toggleMuteState()
            updateMuteSwitch(sound_switch)
        }

        val music_switch : Switch = dialog.findViewById(R.id.music_switch)
        updateSoundOffSwitch(music_switch)

        music_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                isSoundOff = false
            }
            else{
                isSoundOff = true
            }
            saveIsSoundOffToPrefs(isSoundOff)
            updateSoundOffSwitch(music_switch)
        }

        dialog.show()
    }

    private fun saveIsSoundOffToPrefs(isSOff: Boolean) {
        sharedPreferences.edit().putBoolean("isSoundOff", isSOff).apply()
    }

    private fun saveIsMutedToPrefs(isMute: Boolean) {
        sharedPreferences.edit().putBoolean("isMuted", isMute).apply()
    }
}