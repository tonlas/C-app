package com.example.c_app.ui.elements

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.c_app.Challenge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException


class AppViewModel : ViewModel() {
    private var firstLaunch = true
    val challenges = mutableStateListOf<Challenge>()
    /**
     * The pair stores the challenge instance and its index in a list of all challenges to restore it to the same location when undo remove is called.
     */
    private lateinit var lastDeletedChallenge: Pair<Challenge, Int>

    fun addNewChallenge(name: String) {
        challenges.add(Challenge(name))
    }

    fun undoRemoving() {
        val (challenge, index) = lastDeletedChallenge
        challenges.add(index, challenge)
    }

    fun removeChallenge(removed: Challenge) {
        lastDeletedChallenge = removed to challenges.indexOf(removed)
        challenges.remove(removed)
    }

    suspend fun saveInStorage(context: Context) {
        val saveInfo = Json.encodeToString(challenges.toList())
        withContext(Dispatchers.IO) {
            context.openFileOutput("data.txt", Context.MODE_PRIVATE)
                .use { it.write(saveInfo.toByteArray()) }
        }
    }

    suspend fun readFromStorage(context: Context) {
        if (firstLaunch) {
            try {
                withContext(Dispatchers.IO) {
                    val savedData =
                        context.openFileInput("data.txt").bufferedReader().useLines { lines ->
                            lines.fold("") { acc, text ->
                                "$acc$text"
                            }
                        }
                    challenges.addAll(Json.decodeFromString<List<Challenge>>(savedData))
                }
            } catch (_: IOException) {

            } finally {
                firstLaunch = false
            }
        }
    }
}
