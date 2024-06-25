@file:OptIn(ExperimentalSerializationApi::class)

package com.example.c_app

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

/**
 * Represents an instance of the challenge. Consists of the name of the challenge and the challenge calendar.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Challenge(
    var name: String, @EncodeDefault val calendar: ChallengeCalendar = ChallengeCalendar()
)

/**
 * Represents calendar in which events of the challenge are marked.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ChallengeCalendar(
    @EncodeDefault var maxStreak: Int = 0,
    @EncodeDefault var currentStreak: Int = 0,
    @EncodeDefault var lastStreak: Int = 0,
    @EncodeDefault val list: MutableList<ChallengeDay> = mutableListOf()
) {


    private fun getStreaks() {
        // counting max and current from zero
        var count = 1
        var flag =
            true // it function will calculate current and max streaks at once so this flag is needed to stop calculate current streak
        val checked = list.filter { it.completed }.sortedBy { it.date }

        if (checked.isNotEmpty()) {
            try {
                val cL = checked.last().date
                val today = LocalDate.now()
                val daysBetween = Period.between(cL, today)
                if (daysBetween != Period.parse("P0D")) {
                    currentStreak = 0
                    flag = false
                } else if (daysBetween == Period.parse("P0D")) {
                    currentStreak = count
                    maxStreak = count
                }

                for (i in checked.lastIndex downTo 1) {
                    val checkedLater = checked[i].date
                    val checkedEarlier = checked[i - 1].date
                    val daysBetween = Period.between(checkedEarlier, checkedLater)

                    if (daysBetween == Period.parse("P1D")) {
                        count++
                        if (flag) currentStreak = count
                        maxStreak = if (count > maxStreak) count else maxStreak
                    } else {
                        flag = false
                        count = 1
                    }
                }
            } catch (e: NoSuchElementException) {
                currentStreak = 0
            }
        } else {
            maxStreak = 0
            currentStreak = 0
        }

    }

    private fun addNew(completedState: Boolean, calendar: LocalDate = LocalDate.now()) {
        list.add(ChallengeDay(completedState, calendar))
    }

    fun edit(completedState: Boolean) {
        val today = LocalDate.now()
        try {
            val ld = list.last().date
            if (ld == today) {
                list.last().completed = completedState
            } else {
                addNew(completedState)
            }
        } catch (e: NoSuchElementException) {
            if (list.isEmpty()) {
                addNew(completedState)
            }
        }
        getStreaks()
    }

    fun lastCheckedDateIsToday(): Boolean {
        return try {
            list.last().date == LocalDate.now() && list.last().completed
        } catch (e: NoSuchElementException) {
            false
        }
    }
}

/**
 * Represents a single challenge date. Completed property indicates whether the challenge completed on this date or not.
 */
@Serializable
data class ChallengeDay(
    var completed: Boolean, @Serializable(with = DateSerializer::class) val date: LocalDate
) // you could add property to comment a challenge day

/**
 * All date stored in JSON format so serializer is needed for every class.
 */
@Serializer(forClass = LocalDate::class)
object DateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}