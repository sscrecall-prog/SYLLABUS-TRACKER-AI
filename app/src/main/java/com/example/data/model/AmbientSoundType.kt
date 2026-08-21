package com.example.data.model

enum class AmbientSoundType(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val colorHex: String
) {
    NONE(
        title = "Muted / Silent",
        subtitle = "Standard silent study mode",
        emoji = "🔇",
        colorHex = "#9E9E9E"
    ),
    BROWN_NOISE(
        title = "Deep Brown Noise",
        subtitle = "Deep low-frequency rumble for deep work & ADHD focus",
        emoji = "🪐",
        colorHex = "#8D6E63"
    ),
    PINK_RAIN(
        title = "Gentle Rain Shower",
        subtitle = "Continuous soothing rainfall on windowpane",
        emoji = "🌧️",
        colorHex = "#42A5F5"
    ),
    WHITE_NOISE(
        title = "Crisp White Noise",
        subtitle = "Masks background chatter, traffic & external noises",
        emoji = "📻",
        colorHex = "#78909C"
    ),
    OCEAN_WAVES(
        title = "Soothing Ocean Tide",
        subtitle = "Rhythmic tidal waves with slow breathing pace",
        emoji = "🌊",
        colorHex = "#26A69A"
    ),
    BINAURAL_ALPHA(
        title = "Alpha Waves (10 Hz)",
        subtitle = "Stereo 10Hz binaural beats for memory retention & calm alertness",
        emoji = "🧠",
        colorHex = "#AB47BC"
    ),
    BINAURAL_BETA(
        title = "Beta Waves (18 Hz)",
        subtitle = "Stereo 18Hz binaural beats for analytical thinking & speed",
        emoji = "⚡",
        colorHex = "#FFB300"
    ),
    COZY_CAMPFIRE(
        title = "Campfire & Embers",
        subtitle = "Warm low-fi crackle with cozy ember pops",
        emoji = "🪵",
        colorHex = "#E64A19"
    ),
    CLOCK_TICKING(
        title = "Study Clock Rhythm",
        subtitle = "Gentle 60 BPM mechanical tick for pacing",
        emoji = "⏱️",
        colorHex = "#5C6BC0"
    )
}
