package net.suyambu.icecream.model

import net.suyambu.hiper.http.Hiper
import org.json.JSONObject
import net.suyambu.icecream.Icecream
import net.suyambu.icecream.RequestCaller

data class Wallpaper(
    val id: String,
    val imageUrl: String,
    val licensed: Boolean,
    val title: String
) {
    private val hiper = Hiper.getInstance().async()
    private val directUrlQuery = "\n    query contentDownloadUrl(\$itemId: ID!) {\n      contentDownloadUrlAsUgc(itemId: \$itemId)\n    }\n  "
    fun directUrl(callback: (url: String, e: String?) -> Unit): RequestCaller? {
        try {
            val json = JSONObject("""
            {"variables":{"itemId":"$id"}}
        """.trimIndent())
            json.put("query", directUrlQuery)
            val queue = hiper.post(Icecream.API_URL, json = json) {
                if (it.isSuccessful && it.text != null) {
                    val obj = JSONObject(it.text!!)
                    if (obj.has("errors")) {
                        callback("", it.text!!)
                    } else {
                        val url = obj.getJSONObject("data").getString("contentDownloadUrlAsUgc")
                        callback(url, null)
                    }
                }
            }
            return RequestCaller(queue)
        } catch (e: Exception) {
            callback("", e.toString())
        }
        return null
    }
}
