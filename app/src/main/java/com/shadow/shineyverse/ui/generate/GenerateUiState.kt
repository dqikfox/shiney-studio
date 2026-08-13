package com.shadow.shineyverse.ui.generate

import com.shadow.shineyverse.data.remote.dto.ModelInfoDto
import com.shadow.shineyverse.data.remote.dto.MODEL_KIND_VIDEO_I2V

data class GenerateUiState(
    val models: Map<String, ModelInfoDto> = emptyMap(),
    val isLoadingModels: Boolean = true,
    val modelsError: String? = null,

    val selectedModelKey: String = "",
    val prompt: String = "",
    val negative: String = "",
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val cfg: Float = 8.0f,
    val sampler: String = "euler",
    val scheduler: String = "normal",
    val length: Int = 121,
    val fps: Int = 24,
    val seed: Long = 0L,
    val randomizeSeed: Boolean = true,
    val batchCount: Int = 1,
    val folder: String = "",
    val advancedExpanded: Boolean = false,

    val pickedImageName: String? = null,
    val isUploadingImage: Boolean = false,
    val uploadedImageFilename: String = "",

    val generalError: String? = null,
    val pendingPokemonReveal: PokemonReveal? = null,
) {
    val selectedModel: ModelInfoDto? get() = models[selectedModelKey]
    val needsImageUpload: Boolean get() = selectedModel?.kind == MODEL_KIND_VIDEO_I2V
    val isVideoModel: Boolean get() = selectedModel?.kind?.startsWith("video") == true

    val canGenerate: Boolean get() =
        selectedModel != null &&
            prompt.isNotBlank() &&
            !isUploadingImage &&
            (!needsImageUpload || uploadedImageFilename.isNotBlank())
}

data class PokemonReveal(val name: String, val shiny: Boolean)

/** Ported from the web app's STARTER_PROMPTS - inspiration buttons for "Surprise Me!". */
val STARTER_PROMPTS: List<Pair<String, String>> = listOf(
    "Unicorn" to "a cute fluffy unicorn with a sparkly rainbow mane, dreamy pastel colors",
    "Rainbow" to "a giant magical rainbow over a green meadow, fluffy clouds, sunshine",
    "Dragon" to "a friendly cartoon dragon with big round eyes, breathing colorful sparkles instead of fire",
    "Castle" to "a pink fairytale castle with tall towers, flags, and a rainbow in the sky behind it",
    "Puppy" to "a fluffy golden puppy playing in a field of flowers, big happy eyes",
    "Fairy" to "a tiny glowing fairy with butterfly wings sitting on a mushroom in an enchanted forest",
    "Mermaid" to "a friendly cartoon mermaid with a colorful tail swimming near a coral reef",
    "Ice cream" to "a giant rainbow ice cream cone with sprinkles, melting in a sunny park",
    "Dinosaur" to "a cute baby dinosaur with big eyes playing in a jungle, friendly and round-shaped",
    "Space" to "a cute astronaut cat floating among colorful stars and planets",
)

/** Ported from the web app's POKEMON_NAMES - used by "Random Pokemon!" (1-in-50 shiny). */
val POKEMON_NAMES: List<String> = listOf(
    "Pikachu", "Bulbasaur", "Charmander", "Squirtle", "Eevee", "Jigglypuff", "Snorlax", "Gengar",
    "Gyarados", "Dragonite", "Mewtwo", "Mew", "Lucario", "Garchomp", "Greninja", "Charizard",
    "Blastoise", "Venusaur", "Vaporeon", "Jolteon", "Flareon", "Umbreon", "Espeon", "Sylveon",
    "Pichu", "Togepi", "Cyndaquil", "Totodile", "Chikorita", "Torchic", "Mudkip", "Treecko",
    "Piplup", "Turtwig", "Chimchar", "Oshawott", "Snivy", "Tepig", "Froakie", "Fennekin",
    "Chespin", "Rowlet", "Litten", "Popplio", "Sobble", "Scorbunny", "Grookey", "Pidgeotto",
    "Psyduck", "Slowpoke", "Ditto", "Magikarp", "Lapras", "Articuno", "Zapdos", "Moltres",
)
