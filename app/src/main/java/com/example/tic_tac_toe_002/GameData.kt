package com.example.tic_tac_toe_002

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object GameData {
    private var _gameModel : MutableLiveData<GameModel> = MutableLiveData()
    var gameModel : LiveData<GameModel> = _gameModel


    fun saveGameModel(model : GameModel){
        _gameModel.postValue(model)
    }

}