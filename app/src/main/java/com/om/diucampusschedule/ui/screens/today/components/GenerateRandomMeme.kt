package com.om.diucampusschedule.ui.screens.today.components

import com.om.diucampusschedule.R
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

// Data class to hold meme information
data class MemeData(
    val imageRes: Int,
    val description: String
)

// Session-based meme storage - these will persist throughout the app session
object MemeSession {
    private var sessionFridayMeme: MemeData? = null
    private var sessionRegularMeme: MemeData? = null

    // Get session-consistent Friday meme
    fun getSessionFridayMeme(): MemeData {
        if (sessionFridayMeme == null) {
            val fridayMemes = listOf(
                MemeData(R.drawable.friday_one, "Friday Meme 1"),
                MemeData(R.drawable.friday_two, "Friday Meme 2"),
                MemeData(R.drawable.friday_three, "Friday Meme 3"),
                MemeData(R.drawable.friday_four, "Friday Meme 4"),
                MemeData(R.drawable.friday_five, "Friday Meme 5"),
                MemeData(R.drawable.friday_six, "Friday Meme 6"),
                MemeData(R.drawable.friday_seven, "Friday Meme 7")
            )
            sessionFridayMeme = fridayMemes[Random.nextInt(fridayMemes.size)]
        }
        return sessionFridayMeme!!
    }

    // Get session-consistent regular meme
    fun getSessionRegularMeme(): MemeData {
        if (sessionRegularMeme == null) {
            val otherDaysMemes = listOf(
                MemeData(R.drawable.one, "Meme 1"),
                MemeData(R.drawable.two, "Meme 2"),
                MemeData(R.drawable.three, "Meme 3"),
                MemeData(R.drawable.four, "Meme 4"),
                MemeData(R.drawable.five, "Meme 5"),
                MemeData(R.drawable.six, "Meme 6"),
                MemeData(R.drawable.seven, "Meme 7"),
                MemeData(R.drawable.eight, "Meme 8"),
                MemeData(R.drawable.nine, "Meme 9"),
//                MemeData(R.drawable.ten, "Meme 10"),
                MemeData(R.drawable.eleven, "Meme 11"),
                MemeData(R.drawable.twelve, "Meme 12"),
                MemeData(R.drawable.thirteen, "Meme 13"),
                MemeData(R.drawable.fourteen, "Meme 14"),
                MemeData(R.drawable.fifteen, "Meme 15"),
                MemeData(R.drawable.sisxteen, "Meme 16"),
                MemeData(R.drawable.seventeen, "Meme 17"),
                MemeData(R.drawable.eighteen, "Meme 18"),
                MemeData(R.drawable.nineteen, "Meme 19"),
                MemeData(R.drawable.twenty, "Meme 20"),
                MemeData(R.drawable.twentyone, "Meme 21"),
                MemeData(R.drawable.twentytwo, "Meme 22"),
                MemeData(R.drawable.twentythree, "Meme 23"),
                MemeData(R.drawable.twentyfour, "Meme 24"),
                MemeData(R.drawable.twentyfive, "Meme 25")
            )
            sessionRegularMeme = otherDaysMemes[Random.nextInt(otherDaysMemes.size)]
        }
        return sessionRegularMeme!!
    }

    // Reset session memes (can be called when app restarts)
    fun resetSession() {
        sessionFridayMeme = null
        sessionRegularMeme = null
    }
}

// Function to get session-consistent meme based on the day
fun getRandomMeme(selectedDate: LocalDate): MemeData {
    val isFriday = selectedDate.dayOfWeek == DayOfWeek.FRIDAY

    return when {
        isFriday -> MemeSession.getSessionFridayMeme()
        else -> MemeSession.getSessionRegularMeme()
    }
}