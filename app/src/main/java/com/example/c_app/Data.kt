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

val challengeList = mutableListOf<Challenge>()/*.also {
    with(it) {
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))
        add(Challenge("Fuck"))

    }}*/


@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Challenge(var name: String,@EncodeDefault val calendar: ChallengeCalendar = ChallengeCalendar())

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ChallengeCalendar(
 @EncodeDefault   var maxStreak: Int = 0,
 @EncodeDefault  var currentStreak: Int = 0,
 @EncodeDefault  val list: MutableList<ChallengeDay> = mutableListOf()
) {


    private fun updateStreaks() { // used when adding new events at the end of the list
        currentStreak = if (list.size == 1 && list.first().checked) {
            1
        } else {
            val (penultimate,last) = list.takeLast(2)
            val daysBetween = Period.between(penultimate.date,last.date)
            println("${last}${penultimate}${daysBetween}/t${currentStreak}")

            if (penultimate.checked&&last.checked && daysBetween == Period.parse("P1D")) {
                currentStreak + 1
            } else if(last.checked) 1
            else 0

        }

        maxStreak = if (maxStreak > currentStreak) maxStreak else currentStreak
    }


    private fun getStreaks() { // used when adding/editing events in other places

        // counting max and current from zero
        var count = 1
        var flag = true // it function will calculate current and max streaks at once so this flag is needed to stop calculate current streak
        val checked = list.filter { it.checked }.sortedBy{it.date}

        if(checked.isNotEmpty()){
            try {
                val cL = checked.last().date
                val today = LocalDate.now()
                val daysBetween = Period.between(cL,today)
                if (daysBetween != Period.parse("P0D")) {
                    currentStreak = 0
                    flag = false
                }else if(daysBetween == Period.parse("P0D")){
                    currentStreak =count
                    maxStreak =count
                }

                for (i in checked.lastIndex downTo 1) {
                    val checkedLater = checked[i].date
                    val checkedEarlier = checked[i - 1].date
                    val daysBetween = Period.between(checkedEarlier,checkedLater)

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
            }} else {maxStreak =0;currentStreak =0}
    }


    private fun addNew(cs: Boolean, calendar: LocalDate = LocalDate.now()) {
        list.add(ChallengeDay(cs, calendar))
        updateStreaks()

    }

    fun edit(checkState: Boolean) {
        val today = LocalDate.now()
        try {
            val ld = list.last().date
            if (ld==today) {

                list.last().checked = checkState

            }
            else {

                addNew(checkState)
            }

        }
        catch (e:NoSuchElementException){
            if(list.isEmpty()){
                addNew(checkState)
            }
        }
        getStreaks()


    }
    fun lastDateIsToday():Boolean{
        return try{list.last().date==LocalDate.now()} catch (e:NoSuchElementException){false}
    }



}
@Serializable
data class ChallengeDay(
    var checked: Boolean,
   @Serializable(with=DateSerializer::class)
    val date: LocalDate
) { // you could add property to comment a challenge day

    override fun toString() = "(${date} -> ${checked})\n"
    //fun com.example.c_app.ChallengeDay.
}

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