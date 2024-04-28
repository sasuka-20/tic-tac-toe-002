package com.example.tic_tac_toe_002

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import com.example.tic_tac_toe_002.databinding.ActivityEnterNamesBinding

class EnterNamesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnterNamesBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var btnDefaultMusic: MediaPlayer
    private var isMuted: Boolean = false
    private var isSoundOff : Boolean = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEnterNamesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("HangmanPrefs", Context.MODE_PRIVATE)

        isMuted = sharedPreferences.getBoolean("isMuted", false)
        isSoundOff = sharedPreferences.getBoolean("isSoundOff", false)

        mediaPlayer = MediaPlayerManager.getMediaPlayer(this)
        btnDefaultMusic = MediaPlayer.create(this, R.raw.button)
        mediaPlayer.start()

        if(isMuted){
            mediaPlayer.setVolume(0f,0f)
        }else{
            mediaPlayer.setVolume(0.7f, 0.7f)
        }

        binding.SettingsBtn.setOnClickListener {
            if(!isSoundOff){
                btnDefaultMusic.start()
            }
            showCustomDialog()
        }

        binding.startGameBtn.setOnClickListener {
            if(!isSoundOff){
                btnDefaultMusic.start()
            }
            val playerXName = binding.playerXName.text.toString()
            val playerOName = binding.playerOName.text.toString()

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("playerXName", playerXName)
            intent.putExtra("playerOName", playerOName)
            startActivity(intent)
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