package net.suyambu.icecream

import android.util.Log
import net.suyambu.icecream.model.Gif
import net.suyambu.hiper.http.Hiper
import org.json.JSONObject


class Kulfy {
    companion object {
        val LANGUAGES = listOf(
            "all",
            "telugu",
            "tamil",
            "hindi",
            "malayalam",
            "nigeria",
            "pakistan"
        )
        val MEDIA_TYPES = listOf(
            "gif",
            "video",
            "image",
            "list",
            "story",
            "sticker",
            "stickerpack"
        )
        private fun getUrl(page: Int, query: String, languages: List<String>, mediaTypes: List<String>): String {
            return "https://api.kulfyapp.com/V3/gifs/search?client=web&keyword=${query}&skip=${(page-1)*30}&limit=30&language=${languages.joinToString(",")}&transliteration=en&content=${mediaTypes.joinToString(",")}"
        }

        @Volatile private var instance: Kulfy? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: Kulfy()
        }
    }

    private val hiper = Hiper.getInstance().async()

    fun searchGifs(page: Int, query: String, languages: List<String> = LANGUAGES, callback: (List<Gif>, Throwable?) -> Unit) {
        val url = getUrl(page, query, languages, listOf("gif"))
        hiper.get(url = url) {
            try {
                if (it.isSuccessful && it.text != null) {
                    val json = JSONObject(it.text!!).getJSONArray("data")
                    val gifs = ArrayList<Gif>()
                    for (i in 0 until json.length()) {
                        val gif = json.getJSONObject(i)
                        if (gif.getString("content_type") == "gif") {
                            gifs.add(
                                Gif(
                                    name = gif.getString("name"),
                                    stickerUrl = gif.getString("sticker_url"),
                                    gifUrl = gif.getString("gif_url"),
                                    videoUrl = gif.getString("video_url")
                                )
                            )
                        }
                    }
                    callback.invoke(gifs, null)
                } else {
                    throw Exception("Error: Unexpected response from server: ${it.text}")
                }
            } catch (e: Exception) {
                callback.invoke(emptyList(), e)
            }
        }
    }
}