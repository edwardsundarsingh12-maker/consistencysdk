package com.edapp.habittracker.di

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.vector.ImageVector
import com.edapp.habittracker.data.HabitEntity
import com.edapp.habittracker.data.HabitLogEntity
import com.edapp.habittracker.domain.Habit
import com.edapp.habittracker.domain.HabitLog
import com.edapp.habittracker.domain.HabitMonth
import com.edapp.habittracker.domain.HabitYear
import java.time.LocalDate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.edapp.habittracker.data.HabitTagEntity
import com.edapp.habittracker.domain.HabitTag
import com.edapp.habittracker.util.HabitStatusEnum
import com.edapp.habittracker.util.IconRepresentation
import compose.icons.AllIcons
import compose.icons.CssGgIcons
import compose.icons.EvaIcons
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.LineAwesomeIcons
import compose.icons.LineaIcons
import compose.icons.Octicons
import compose.icons.SimpleIcons
import compose.icons.TablerIcons
import compose.icons.WeatherIcons

object HabitMapper {

    @RequiresApi(Build.VERSION_CODES.O)
    fun mapToDomain(
        habit: HabitEntity,
        logs: List<HabitLogEntity>
    ): Habit {
        var todayHabitStatus = HabitStatusEnum.NOT_DONE
        val todayEpochDat = LocalDate.now().toEpochDay()
        val years = logs
            .groupBy {
                if (it.epochDay == todayEpochDat) {
                    todayHabitStatus = HabitStatusEnum.getObjByPercentage( it.status)
                }
                LocalDate.ofEpochDay(it.epochDay).year
            }
            .map { (year, yearLogs) ->
                HabitYear(
                    year = year,
                    months = yearLogs.groupBy { LocalDate.ofEpochDay(it.epochDay).monthValue }
                        .map { (month, monthLogs) ->
                            HabitMonth(
                                month = month,
                                logs = monthLogs.map { log ->
                                    val status = if (log.epochDay == todayEpochDat && HabitStatusEnum.getObjByPercentage(log.status) == HabitStatusEnum.NOT_DONE) {
                                        HabitStatusEnum.PROGRESS
                                    } else { HabitStatusEnum.getObjByPercentage(log.status) }
                                    HabitLog(
                                        habitOwnerId = log.habitOwnerId,
                                        epochDay = log.epochDay,
                                        status =status
                                    )
                                }
                            )
                        }
                )
            }

        return Habit(
            id = habit.habitId,
            title = habit.title,
            description = habit.description,
            icon = IconMapper.getIconByName(habit.iconName), // actual ImageVector
            years = years,
            consistencyIcon = IconMapper.getIconByName(habit.consistencyIconName),
            color = Color(habit.colorValue.toULong()),
            uncheckedColorValue = Color(habit.uncheckedColorValue.toULong()),
            todayHabitStatus = todayHabitStatus
        )
    }
}

object IconMapper {

    private val iconPacks: Map<String, List<ImageVector>> = mapOf(
        "Feather" to FeatherIcons.AllIcons,
        "FontAwesome" to FontAwesomeIcons.AllIcons,
        "Tabler" to TablerIcons.AllIcons,
        "LineAwesome" to LineAwesomeIcons.AllIcons,
        "Octicons" to Octicons.AllIcons,
        "SimpleIcons" to SimpleIcons.AllIcons,
        "Linea" to LineaIcons.AllIcons,
        "CssGg" to CssGgIcons.AllIcons,
        "Weather" to WeatherIcons.AllIcons,
        "Eva" to EvaIcons.AllIcons
    )

    /** Find icon by its string key (like `Feather.Heart`) */
    fun getIconByName(name: String): IconRepresentation = allIconsMap[name] ?: allIconsMap["Star"]!!

    val allIconsList: List<Pair<String, IconRepresentation>> by lazy {
        buildList {
            // 1️⃣ Consistency Icons
            iconNamesConsistency.forEach { name ->
                add(name to IconRepresentation.Vector(mapNameToIconConsistency(name)))
            }

            // 2️⃣ Emojis
            emojiicons.forEach { (key, value) ->
                add(key to value)
            }

            // 3️⃣ Icon Packs
            iconPacks.forEach { (packName, icons) ->
                icons.forEach { icon ->
                    add("$packName.${icon.name}" to IconRepresentation.Vector(icon))
                }
            }
        }
    }

    private val allIconsMap: Map<String, IconRepresentation> by lazy {
        buildMap {
            // 1️⃣ Consistency Icons
            iconNamesConsistency.forEach { name ->
                put(name, IconRepresentation.Vector(mapNameToIconConsistency(name)))
            }

            // 2️⃣ Emojis
            putAll(emojiicons)

            // 3️⃣ Icon Packs
            iconPacks.forEach { (packName, icons) ->
                icons.forEach { icon ->
                    put("$packName.${icon.name}", IconRepresentation.Vector(icon))
                }
            }
        }
    }


    private fun mapNameToIconConsistency(name: String): ImageVector {
        return when (name) {
            "Star" -> Icons.Default.Star
            "Check" -> Icons.Default.Check
            "Favorite" -> Icons.Default.Favorite
            "Alarm" -> Icons.Default.Alarm
            "Home" -> Icons.Default.Home
            "Person" -> Icons.Default.Person
            "Settings" -> Icons.Default.Settings
            "Search" -> Icons.Default.Search
            "Calendar" -> Icons.Default.DateRange
            "Work" -> Icons.Default.Work
            "Email" -> Icons.Default.Email
            "Phone" -> Icons.Default.Phone
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Build" -> Icons.Default.Build
            "Camera" -> Icons.Default.Camera
            "Lock" -> Icons.Default.Lock
            "School" -> Icons.Default.School
            "Event" -> Icons.Default.Event
            "Group" -> Icons.Default.Group
            "Map" -> Icons.Default.Map
            "Place" -> Icons.Default.Place
            "ThumbUp" -> Icons.Default.ThumbUp
            "ThumbDown" -> Icons.Default.ThumbDown
            "Visibility" -> Icons.Default.Visibility
            "VisibilityOff" -> Icons.Default.VisibilityOff
            "Delete" -> Icons.Default.Delete
            "Edit" -> Icons.Default.Edit
            "Done" -> Icons.Default.Done
            "ExitToApp" -> Icons.Default.ExitToApp
            "FavoriteBorder" -> Icons.Default.FavoriteBorder
            "Help" -> Icons.Default.Help
            "Info" -> Icons.Default.Info
            "Notifications" -> Icons.Default.Notifications
            "NotificationsActive" -> Icons.Default.NotificationsActive
            "NotificationsNone" -> Icons.Default.NotificationsNone
            "Refresh" -> Icons.Default.Refresh
            "Send" -> Icons.Default.Send
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "StarBorder" -> Icons.Default.StarBorder
            "Sync" -> Icons.Default.Sync
            "Add" -> Icons.Default.Add
            "Remove" -> Icons.Default.Remove
            "MoreVert" -> Icons.Default.MoreVert
            "ChevronRight" -> Icons.Default.ChevronRight
            "ChevronLeft" -> Icons.Default.ChevronLeft
            "ArrowForward" -> Icons.Default.ArrowForward
            "ArrowBack" -> Icons.Default.ArrowBack
            "Menu" -> Icons.Default.Menu
            "Close" -> Icons.Default.Close
            "SearchOff" -> Icons.Default.SearchOff
            "PlayArrow" -> Icons.Default.PlayArrow
            "Pause" -> Icons.Default.Pause
            "Stop" -> Icons.Default.Stop
            "FastForward" -> Icons.Default.FastForward
            "FastRewind" -> Icons.Default.FastRewind
            "VolumeUp" -> Icons.Default.VolumeUp
            "VolumeDown" -> Icons.Default.VolumeDown
            "VolumeOff" -> Icons.Default.VolumeOff
            "Mic" -> Icons.Default.Mic
            "MicOff" -> Icons.Default.MicOff
            "CameraAlt" -> Icons.Default.CameraAlt
            "Photo" -> Icons.Default.Photo
            "Videocam" -> Icons.Default.Videocam
            "BrightnessHigh" -> Icons.Default.BrightnessHigh
            "BrightnessLow" -> Icons.Default.BrightnessLow
            "Cloud" -> Icons.Default.Cloud
            "CloudQueue" -> Icons.Default.CloudQueue
            "CloudOff" -> Icons.Default.CloudOff
            "AttachFile" -> Icons.Default.AttachFile
            "AttachMoney" -> Icons.Default.AttachMoney
            "Payment" -> Icons.Default.Payment
            "TrendingUp" -> Icons.Default.TrendingUp
            "TrendingDown" -> Icons.Default.TrendingDown
            "BarChart" -> Icons.Default.BarChart
            "PieChart" -> Icons.Default.PieChart
            "ShowChart" -> Icons.Default.ShowChart
            "AccountCircle" -> Icons.Default.AccountCircle
            "Badge" -> Icons.Default.Badge
            "Directions" -> Icons.Default.Directions
            "DirectionsBike" -> Icons.Default.DirectionsBike
            "DirectionsBoat" -> Icons.Default.DirectionsBoat
            "DirectionsBus" -> Icons.Default.DirectionsBus
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "DirectionsRailway" -> Icons.Default.DirectionsRailway
            "DirectionsRun" -> Icons.Default.DirectionsRun
            "DirectionsSubway" -> Icons.Default.DirectionsSubway
            "DirectionsTransit" -> Icons.Default.DirectionsTransit
            "DirectionsWalk" -> Icons.Default.DirectionsWalk
            "Flight" -> Icons.Default.Flight
            "Hotel" -> Icons.Default.Hotel
            "LocalCafe" -> Icons.Default.LocalCafe
            "LocalDining" -> Icons.Default.LocalDining
            "LocalDrink" -> Icons.Default.LocalDrink
            "LocalFlorist" -> Icons.Default.LocalFlorist
            "LocalGroceryStore" -> Icons.Default.LocalGroceryStore
            "LocalHospital" -> Icons.Default.LocalHospital
            "LocalLibrary" -> Icons.Default.LocalLibrary
            "LocalMall" -> Icons.Default.LocalMall
            "LocalMovies" -> Icons.Default.LocalMovies
            "LocalOffer" -> Icons.Default.LocalOffer
            "LocalParking" -> Icons.Default.LocalParking
            "LocalPharmacy" -> Icons.Default.LocalPharmacy
            "LocalPizza" -> Icons.Default.LocalPizza
            "LocalPlay" -> Icons.Default.LocalPlay
            "LocalPostOffice" -> Icons.Default.LocalPostOffice
            "LocalSee" -> Icons.Default.LocalSee
            "LocalShipping" -> Icons.Default.LocalShipping
            "LocalTaxi" -> Icons.Default.LocalTaxi
            "Restaurant" -> Icons.Default.Restaurant
            "Pool" -> Icons.Default.Pool
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "DirectionsRun" -> Icons.Default.DirectionsRun
            "EmojiEmotions" -> Icons.Default.EmojiEmotions
            "EmojiEvents" -> Icons.Default.EmojiEvents
            else -> Icons.Default.HelpOutline
        }
    }

    // List of icon names
    private val iconNamesConsistency = listOf(
        "Star", "Check", "Favorite", "Alarm", "Home", "Person", "Settings", "Search",
        "Calendar", "Work", "Email", "Phone", "ShoppingCart", "Build", "Camera", "Lock",
        "School", "Event", "Group", "Map", "Place", "ThumbUp", "ThumbDown", "Visibility",
        "VisibilityOff", "Delete", "Edit", "Done", "ExitToApp", "FavoriteBorder", "Help",
        "Info", "Notifications", "NotificationsActive", "NotificationsNone", "Refresh",
        "Send", "ShoppingBag", "StarBorder", "Sync", "Add", "Remove", "MoreVert",
        "ChevronRight", "ChevronLeft", "ArrowForward", "ArrowBack", "Menu", "Close",
        "SearchOff", "PlayArrow", "Pause", "Stop", "FastForward", "FastRewind", "VolumeUp",
        "VolumeDown", "VolumeOff", "Mic", "MicOff", "CameraAlt", "Photo", "Videocam",
        "BrightnessHigh", "BrightnessLow", "Cloud", "CloudQueue", "CloudOff", "AttachFile",
        "AttachMoney", "Payment", "TrendingUp", "TrendingDown", "BarChart", "PieChart",
        "ShowChart", "AccountCircle", "Badge", "Directions", "DirectionsBike", "DirectionsBoat",
        "DirectionsBus", "DirectionsCar", "DirectionsRailway", "DirectionsRun", "DirectionsSubway",
        "DirectionsTransit", "DirectionsWalk", "Flight", "Hotel", "LocalCafe", "LocalDining",
        "LocalDrink", "LocalFlorist", "LocalGroceryStore", "LocalHospital", "LocalLibrary",
        "LocalMall", "LocalMovies", "LocalOffer", "LocalParking", "LocalPharmacy", "LocalPizza",
        "LocalPlay", "LocalPostOffice", "LocalSee", "LocalShipping", "LocalTaxi", "Restaurant",
        "Pool", "FitnessCenter", "EmojiEmotions", "EmojiEvents"
    )

    // ✅ Main Map — String → IconRepresentation
    private val emojiicons: Map<String, IconRepresentation> = mapOf(

        // 😀 Faces
        "emoji_grinning" to IconRepresentation.Emoji("😀"),
        "emoji_grin" to IconRepresentation.Emoji("😁"),
        "emoji_laughing" to IconRepresentation.Emoji("😆"),
        "emoji_sweat_smile" to IconRepresentation.Emoji("😅"),
        "emoji_joy" to IconRepresentation.Emoji("😂"),
        "emoji_rofl" to IconRepresentation.Emoji("🤣"),
        "emoji_smile" to IconRepresentation.Emoji("😊"),
        "emoji_blush" to IconRepresentation.Emoji("☺️"),
        "emoji_wink" to IconRepresentation.Emoji("😉"),
        "emoji_slight_smile" to IconRepresentation.Emoji("🙂"),
        "emoji_upside_down" to IconRepresentation.Emoji("🙃"),
        "emoji_thinking" to IconRepresentation.Emoji("🤔"),
        "emoji_neutral" to IconRepresentation.Emoji("😐"),
        "emoji_expressionless" to IconRepresentation.Emoji("😑"),
        "emoji_no_mouth" to IconRepresentation.Emoji("😶"),
        "emoji_smirk" to IconRepresentation.Emoji("😏"),
        "emoji_unamused" to IconRepresentation.Emoji("😒"),
        "emoji_cry" to IconRepresentation.Emoji("😢"),
        "emoji_sob" to IconRepresentation.Emoji("😭"),
        "emoji_confounded" to IconRepresentation.Emoji("😖"),
        "emoji_weary" to IconRepresentation.Emoji("😩"),
        "emoji_tired" to IconRepresentation.Emoji("😫"),
        "emoji_angry" to IconRepresentation.Emoji("😠"),
        "emoji_rage" to IconRepresentation.Emoji("😡"),
        "emoji_flushed" to IconRepresentation.Emoji("😳"),
        "emoji_hot" to IconRepresentation.Emoji("🥵"),
        "emoji_cold" to IconRepresentation.Emoji("🥶"),
        "emoji_sleeping" to IconRepresentation.Emoji("😴"),
        "emoji_drooling" to IconRepresentation.Emoji("🤤"),
        "emoji_party" to IconRepresentation.Emoji("🥳"),
        "emoji_cowboy" to IconRepresentation.Emoji("🤠"),
        "emoji_clown" to IconRepresentation.Emoji("🤡"),
        "emoji_robot" to IconRepresentation.Emoji("🤖"),
        "emoji_ghost" to IconRepresentation.Emoji("👻"),
        "emoji_skull" to IconRepresentation.Emoji("💀"),
        "emoji_poop" to IconRepresentation.Emoji("💩"),

        // ❤️ Love & gestures
        "emoji_heart" to IconRepresentation.Emoji("❤️"),
        "emoji_heart_eyes" to IconRepresentation.Emoji("😍"),
        "emoji_kiss" to IconRepresentation.Emoji("😘"),
        "emoji_two_hearts" to IconRepresentation.Emoji("💕"),
        "emoji_broken_heart" to IconRepresentation.Emoji("💔"),
        "emoji_pray" to IconRepresentation.Emoji("🙏"),
        "emoji_thumbsup" to IconRepresentation.Emoji("👍"),
        "emoji_thumbsdown" to IconRepresentation.Emoji("👎"),
        "emoji_wave" to IconRepresentation.Emoji("👋"),
        "emoji_clap" to IconRepresentation.Emoji("👏"),
        "emoji_muscle" to IconRepresentation.Emoji("💪"),
        "emoji_ok" to IconRepresentation.Emoji("👌"),
        "emoji_victory" to IconRepresentation.Emoji("✌️"),
        "emoji_crossed_fingers" to IconRepresentation.Emoji("🤞"),
        "emoji_handshake" to IconRepresentation.Emoji("🤝"),

        // 🌍 Nature & Animals
        "emoji_dog" to IconRepresentation.Emoji("🐶"),
        "emoji_cat" to IconRepresentation.Emoji("🐱"),
        "emoji_mouse" to IconRepresentation.Emoji("🐭"),
        "emoji_rabbit" to IconRepresentation.Emoji("🐰"),
        "emoji_panda" to IconRepresentation.Emoji("🐼"),
        "emoji_bear" to IconRepresentation.Emoji("🐻"),
        "emoji_tiger" to IconRepresentation.Emoji("🐯"),
        "emoji_monkey" to IconRepresentation.Emoji("🐵"),
        "emoji_elephant" to IconRepresentation.Emoji("🐘"),
        "emoji_dolphin" to IconRepresentation.Emoji("🐬"),
        "emoji_fish" to IconRepresentation.Emoji("🐟"),
        "emoji_frog" to IconRepresentation.Emoji("🐸"),
        "emoji_snake" to IconRepresentation.Emoji("🐍"),
        "emoji_butterfly" to IconRepresentation.Emoji("🦋"),
        "emoji_flower" to IconRepresentation.Emoji("🌸"),
        "emoji_tree" to IconRepresentation.Emoji("🌳"),
        "emoji_fire" to IconRepresentation.Emoji("🔥"),
        "emoji_star" to IconRepresentation.Emoji("⭐"),
        "emoji_sun" to IconRepresentation.Emoji("☀️"),
        "emoji_moon" to IconRepresentation.Emoji("🌙"),
        "emoji_cloud" to IconRepresentation.Emoji("☁️"),
        "emoji_rainbow" to IconRepresentation.Emoji("🌈"),

        // 🍕 Food & drink
        "emoji_pizza" to IconRepresentation.Emoji("🍕"),
        "emoji_burger" to IconRepresentation.Emoji("🍔"),
        "emoji_fries" to IconRepresentation.Emoji("🍟"),
        "emoji_hotdog" to IconRepresentation.Emoji("🌭"),
        "emoji_taco" to IconRepresentation.Emoji("🌮"),
        "emoji_sushi" to IconRepresentation.Emoji("🍣"),
        "emoji_icecream" to IconRepresentation.Emoji("🍦"),
        "emoji_cake" to IconRepresentation.Emoji("🍰"),
        "emoji_candy" to IconRepresentation.Emoji("🍬"),
        "emoji_chocolate" to IconRepresentation.Emoji("🍫"),
        "emoji_beer" to IconRepresentation.Emoji("🍺"),
        "emoji_wine" to IconRepresentation.Emoji("🍷"),
        "emoji_coffee" to IconRepresentation.Emoji("☕"),
        "emoji_watermelon" to IconRepresentation.Emoji("🍉"),
        "emoji_apple" to IconRepresentation.Emoji("🍎"),
        "emoji_banana" to IconRepresentation.Emoji("🍌"),
        "emoji_grape" to IconRepresentation.Emoji("🍇"),
        "emoji_strawberry" to IconRepresentation.Emoji("🍓"),
        "emoji_lemon" to IconRepresentation.Emoji("🍋"),

        // ⚡ Misc & symbols
        "emoji_lightning" to IconRepresentation.Emoji("⚡"),
        "emoji_boom" to IconRepresentation.Emoji("💥"),
        "emoji_trophy" to IconRepresentation.Emoji("🏆"),
        "emoji_medal" to IconRepresentation.Emoji("🎖️"),
        "emoji_target" to IconRepresentation.Emoji("🎯"),
        "emoji_idea" to IconRepresentation.Emoji("💡"),
        "emoji_book" to IconRepresentation.Emoji("📖"),
        "emoji_pencil" to IconRepresentation.Emoji("✏️"),
        "emoji_lock" to IconRepresentation.Emoji("🔒"),
        "emoji_key" to IconRepresentation.Emoji("🔑"),
        "emoji_alarm" to IconRepresentation.Emoji("⏰"),
        "emoji_calendar" to IconRepresentation.Emoji("📅"),
        "emoji_mail" to IconRepresentation.Emoji("✉️"),
        "emoji_phone" to IconRepresentation.Emoji("📱"),
        "emoji_computer" to IconRepresentation.Emoji("💻"),
        "emoji_gamepad" to IconRepresentation.Emoji("🎮"),
        "emoji_music" to IconRepresentation.Emoji("🎵"),
        "emoji_camera" to IconRepresentation.Emoji("📷"),
        "emoji_palette" to IconRepresentation.Emoji("🎨"),
        "emoji_money" to IconRepresentation.Emoji("💰"),
        "emoji_globe" to IconRepresentation.Emoji("🌍")
    )

    // ✅ Lookup helpers
    private fun getIconByKey(key: String): IconRepresentation? = emojiicons[key]

    private fun getKeyByIcon(icon: IconRepresentation): String? =
        emojiicons.entries.find { it.value == icon }?.key

    val defaultTags = listOf(
        HabitTagEntity(title = "Fitness", icon = "FontAwesome.Running", colorValue = 0xFF2196F3),
        HabitTagEntity(title = "Reading", icon = "FontAwesome.Readme", colorValue = 0xFFFFC107),
        HabitTagEntity(title = "Meditation", icon = "FontAwesome.MediumM", colorValue = 0xFF4CAF50),
        HabitTagEntity(title = "Work", icon = "SimpleIcons.Libreoffice", colorValue = 0xFFE91E63),
        HabitTagEntity(title = "Sleep", icon = "FontAwesome.Bed", colorValue = 0xFF9C27B0),
        HabitTagEntity(title = "Study", icon = "LineAwesome.BookSolid", colorValue = 0xFFFF5722),
        HabitTagEntity(title = "Health", icon = "FontAwesome.PlusCircle", colorValue = 0xFF795548),
        HabitTagEntity(title = "Social", icon = "Tabler.Social", colorValue = 0xFF00BCD4),
        HabitTagEntity(title = "Hobby", icon = "CssGg.Timelapse", colorValue = 0xFF607D8B),
        HabitTagEntity(title = "Finance", icon = "LineAwesome.DumbbellSolid", colorValue = 0xFFFF9800)
    )

}


fun HabitTag.toEntity() : HabitTagEntity {
    return HabitTagEntity(tagId, title , icon, colorValue)
}

fun HabitTagEntity.toDomain() : HabitTag {
    return HabitTag(tagId, title , icon, colorValue)
}

fun HabitLog.toEntity() : HabitLogEntity {
    return HabitLogEntity(habitOwnerId, epochDay, status.percentage)
}

fun HabitLogEntity.toDomain() : HabitLog {
    return HabitLog(habitOwnerId, epochDay, HabitStatusEnum.getObjByPercentage(status))
}

