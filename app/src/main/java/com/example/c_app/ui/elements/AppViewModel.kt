package com.example.c_app.ui.elements

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.example.c_app.Challenge
import com.example.c_app.challengeList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException


class AppViewModel : ViewModel() {
    var firstLaunch = true
    val challenges = mutableStateListOf<Challenge>().apply {
        addAll(challengeList)
    }
    lateinit var lastDeletedChallenge:Pair<Challenge,Int>

/*
init {
    runBlocking {
        val context= LocalContext.current
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

        }
    }
}
*/

    fun addNewChallenge(name: String) {
        challenges.add(Challenge(name))
    }
    fun undoRemoving() {
        val (challenge, index) = lastDeletedChallenge
        challenges.add(index,challenge)
    }

    fun removeChallenge(removed:Challenge) {
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

        if(firstLaunch){
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

            }
            finally {
                firstLaunch=false
            }
        }
    }

}
