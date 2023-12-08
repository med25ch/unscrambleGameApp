package com.example.unscramble.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import com.example.unscramble.ui.GameUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    // Game UI state
    private val _uiState = MutableStateFlow(GameUiState())

    val uiState : StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var currentWord : String

    var userGuess by mutableStateOf("")
        private set

    // Set of words used in the game
    private var usedWords: MutableSet<String> = mutableSetOf()

    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()

        return if (usedWords.contains(currentWord)) {
            pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord) == word) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    init {
        resetGame()
    }

    fun checkUserGuess(){

        if (isGameEnded()){
            return
        }

        if (userGuess.equals(currentWord, ignoreCase = true)){
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateScore(updatedScore)
        }else{
            _uiState.update { currentState -> currentState.copy(
                isGuessedWordWrong = true,
            ) }
        }

        //Reset user guess
        updateUserGuess("")
        //Update word count
        updateWordCount()
    }

    private fun updateWordCount() {
        val wordCount = usedWords.size
        _uiState.update { currentState -> currentState.copy(currentWordCount = wordCount) }
    }

    private fun updateScore(updatedScore : Int){
        _uiState.update { currentState -> currentState.copy(
            isGuessedWordWrong = false,
            currentScrambledWord = pickRandomWordAndShuffle(),
            score = updatedScore
        )}
    }

    fun skipWord() {
        if (isGameEnded()){
            return
        }
        _uiState.update { currentState -> currentState.copy(
            currentScrambledWord = pickRandomWordAndShuffle(),
            currentWordCount = usedWords.size,
            isGuessedWordWrong = false
        ) }
    }

    private fun isGameEnded() : Boolean{
        return if (usedWords.size == MAX_NO_OF_WORDS){
            //End Game
            _uiState.update {currentState ->
                currentState.copy(
                    isGameOver = true
                )
            }

            true
        }else
        {
            false
        }
    }

}