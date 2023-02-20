package net.suyambu.icecreamexample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import net.suyambu.icecream.Icecream
import net.suyambu.icecream.Kulfy
import net.suyambu.icecreamexample.ui.theme.IcecreamExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IcecreamExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    App()
                }
            }
        }
    }
}


val icecream = Icecream.getInstance()
val kulfy = Kulfy.getInstance()

@Composable
fun App() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Box(contentAlignment = Alignment.Center) {
        Button(onClick = {
//            icecream.searchWallpapers("iron man", 1) { wallpapers, error ->
//                Log.d("hello", wallpapers[0].toString())
//                Log.d("hello", wallpapers[1].toString())
////                wallpapers[0].directUrl { url, e ->
////                    debug(url)
////                }
//            }
            kulfy.searchGifs(1, "varisu", listOf("all")) { gifs, error ->
                Log.d("hello", gifs[0].toString())
                Log.d("hello", gifs[1].toString())
            }
        }) {
            Text(text = "Go!")
        }
    }
}
