package net.suyambu.icecream

import android.util.Log
import org.json.JSONObject
import net.suyambu.icecream.model.Ringtone
import net.suyambu.icecream.model.Wallpaper
import net.suyambu.hiper.http.Hiper
import org.json.JSONArray
import java.net.URLEncoder

class Icecream {
    companion object {
        const val API_URL = "https://www.zedge.net/api/graphql"
        @Volatile private var instance: Icecream? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: Icecream()
        }
    }

    private val hiper = Hiper.getInstance().async()


    /* Ringtone */

    private fun extractRingtones(obj: JSONObject): List<Ringtone> {
        val ringtones = ArrayList<Ringtone>()
        val items = obj.getJSONArray("items")
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            ringtones.add(
                Ringtone(
                    id = item.getString("id"),
                    imageUrl = item.getJSONObject("meta").getString("thumbUrl"),
                    licensed = item.getBoolean("licensed"),
                    title = item.getString("title"),
                    audioUrl = item.getJSONObject("meta").getString("audioUrl"),
                    gradientStart = item.getJSONObject("meta").getString("gradientStart"),
                    gradientEnd = item.getJSONObject("meta").getString("gradientEnd")
                )
            )
        }
        return ringtones
    }

    fun buildQuery(query: String, next: String? = null): JSONObject {
        val jsonObject = JSONObject()

        jsonObject.put("query", "\n    query browse_filteredList(\$input: BrowseFilteredListFilterInput) {\n      browse_filteredList(input: \$input) {\n        ...browseFilteredListResource\n      }\n    }\n    \n  fragment browseFilteredListResource on BrowseContinuationItems {\n    items {\n      ...browseListItemResource\n\n      ... on BrowseProfileItem {\n        ...browseListProfileItemResource\n      }\n    }\n    next\n  }\n  \n  fragment browseListItemResource on BrowseItem {\n    ... on BrowseWallpaperItem {\n      id\n      licensed\n      title\n      type\n      paymentMethod {\n        type\n        price\n      }\n      meta {\n        previewUrl\n        microThumb\n        thumbUrl\n      }\n      profile {\n        id\n        name\n        avatarIconUrl\n        verified\n      }\n    }\n\n    ... on BrowseRingtoneItem {\n      id\n      licensed\n      title\n      type\n      paymentMethod {\n        type\n        price\n      }\n      meta {\n        audioUrl\n        duration\n        gradientStart\n        gradientEnd\n        thumbUrl\n      }\n      profile {\n        id\n        name\n        avatarIconUrl\n        verified\n      }\n    }\n\n    ... on BrowseNotificationSoundItem {\n      id\n      licensed\n      title\n      type\n      paymentMethod {\n        type\n        price\n      }\n      meta {\n        audioUrl\n        duration\n        gradientStart\n        gradientEnd\n        thumbUrl\n      }\n      profile {\n        id\n        name\n        avatarIconUrl\n        verified\n      }\n    }\n\n    ... on BrowseLiveWallpaperItem {\n      id\n      licensed\n      title\n      type\n      paymentMethod {\n        type\n        price\n      }\n      meta {\n        previewUrl\n        thumbUrl\n      }\n      profile {\n        id\n        name\n        avatarIconUrl\n        verified\n      }\n    }\n  }\n\n  \n  fragment browseListProfileItemResource on BrowseProfileItem {\n    id\n    type\n    avatarUrl\n    verified\n    name\n    shareUrl\n  }\n\n\n  ")
        jsonObject.put("operationName", "browse_filteredList")

        val variables = JSONObject()
        val input = JSONObject()

        input.put("itemType", "RINGTONE")
        input.put("categories", JSONArray())
        input.put("colors", JSONArray())
        input.put("sort", "RELEVANT")
        input.put("keywords", JSONArray(listOf(query)))
        input.put("maxPrice", JSONObject.NULL)
        input.put("minPrice", JSONObject.NULL)
        input.put("minDurationMs", JSONObject.NULL)
        input.put("maxDurationMs", JSONObject.NULL)
        input.put("profileType", "ANY")
        input.put("size", 24)

        // Set the next value if provided
        if (next != null) {
            input.put("next", next)
        } else {
            input.put("next", JSONObject.NULL)
        }

        variables.put("input", input)
        jsonObject.put("variables", variables)

        return jsonObject
    }

    fun searchRingtones(query: String, next: String?, callback: (List<Ringtone>, error: String?, next: String?) -> Unit): RequestCaller? {
        try {
            val json = buildQuery(query, next)
            Log.d("hello", json.toString())
            val queue = hiper.post(API_URL, json=json) {
                Log.d("hello", "${it.isSuccessful}, ${it.text}, ${it.statusCode}, ${it.statusMessage}")
                if (it.isSuccessful && it.text != null) {
                    var obj = JSONObject(it.text!!)
                    if (obj.has("errors")) {
                        callback.invoke(emptyList(), it.text!!, obj.getJSONObject("data").getJSONObject("browse_filteredList").getString("next"))
                    } else {
                        obj = obj.getJSONObject("data").getJSONObject("browse_filteredList")
                        callback.invoke(extractRingtones(obj), null, null)
                    }
                } else {
                    callback.invoke(emptyList(), "Request failed", null)
                }
            }
            return RequestCaller(queue)
        } catch (e: Exception) {
            callback(emptyList(), e.toString(), null)
        }
        return null
    }

    /* Wallpaper */
//    private fun extractWallpapers(obj: JSONObject): List<Wallpaper> {
//        val wallpapers = ArrayList<Wallpaper>()
//        val items = obj.getJSONArray("items")
//        for (i in 0 until items.length()) {
//            val item = items.getJSONObject(i)
//            val isLicensed = item.getBoolean("licensed")
//            if (!isLicensed) {
//                wallpapers.add(
//                    Wallpaper(
//                        id = item.getString("id"),
//                        imageUrl = item.getString("imageUrl"),
//                        licensed = item.getBoolean("licensed"),
//                        title = item.getString("title")
//                    )
//                )
//            }
//        }
//        return wallpapers
//    }
//
//
//    fun searchWallpapers(query: String, page: Int, callback: (List<Wallpaper>, error: String?) -> Unit): RequestCaller? {
//        try {
//            val json = JSONObject("""
//            {"variables":{"input":{"contentType":"WALLPAPER","keyword":"${URLEncoder.encode(query.replace("\"", ""), "UTF-8")}","page":$page,"size":24}}}
//        """.trimIndent())
//            json.put("query", searchQuery)
//            val queue = hiper.post(API_URL, json=json) {
//                if (it.isSuccessful && it.text != null) {
//                    var obj = JSONObject(it.text!!)
//                    if (obj.has("errors")) {
//                        callback.invoke(emptyList(), it.text!!)
//                    } else {
//                        obj = obj.getJSONObject("data").getJSONObject("searchAsUgc")
//                        callback.invoke(extractWallpapers(obj), null)
//                    }
//                } else {
//                    callback.invoke(emptyList(), "Request failed")
//                }
//            }
//            return RequestCaller(queue)
//        } catch (e: Exception) {
//            callback(emptyList(), e.toString())
//        }
//        return null
//    }
}