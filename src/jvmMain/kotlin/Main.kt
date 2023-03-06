import VideoStatus.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import javafx.application.Platform
import javafx.concurrent.Worker.State.*
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import java.awt.BorderLayout
import javax.swing.JPanel
import kotlin.random.Random

@Composable
@Preview
fun App(window: ComposeWindow, acceptExit: () -> Unit) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                var showNeverGonnaGiveUp by remember { mutableStateOf(false) }
                if (showNeverGonnaGiveUp) {
                    var loading by remember { mutableStateOf(Undefined) }
                    Column(
                        modifier =
                        if (loading == Undefined || loading == Failed) Modifier.height(0.dp)
                        else Modifier
                    ) {
                        if (loading == Running) LinearProgressIndicator(Modifier.fillMaxWidth())
                        Web(
                            composeWindow = window,
                            url = "https://youtu.be/dQw4w9WgXcQ",
                            onRunning = { loading = Running },
                            onSuccess = { loading = Success },
                            onFailed = { loading = Failed }
                        )
                    }

                    if (loading == Undefined) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    if (loading == Failed) Text(
                        "No internet connection",
                        style = MaterialTheme.typography.h6
                    )
                } else FirstWindow { showNeverGonnaGiveUp = true }
            }
        }
    }
}

enum class VideoStatus {
    Undefined, Running, Success, Failed
}

fun main() = application {
    var canExit by remember { mutableStateOf(false) }
    Window(
        title = "Modern test",
        resizable = true,
        onCloseRequest = {
            if (canExit)
                exitApplication()
        }
    ) {
        App(window) { canExit = true }
    }
}

@Composable
fun Web(
    composeWindow: ComposeWindow,
    modifier: Modifier = Modifier.fillMaxSize(),
    url: String = "https://google.com",
    onRunning: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onFailed: () -> Unit = {}
) {
    val jfxPanel = remember { JFXPanel() }
    Box(modifier = modifier) {
        ComposeJFXPanel(
            composeWindow = composeWindow,
            jfxPanel = jfxPanel,
            onCreate = {
                Platform.runLater {
                    val root = WebView()
                    val engine = root.engine
                    engine.loadWorker.stateProperty().addListener { _, _, newState ->
                        when (newState) {
                            RUNNING -> onRunning()
                            SUCCEEDED -> onSuccess()
                            FAILED -> onFailed()
                            else -> Unit
                        }
                    }
                    val scene = Scene(root)
                    jfxPanel.scene = scene
                    engine.load(url)
                }
            })
    }
}

@Composable
fun ComposeJFXPanel(
    composeWindow: ComposeWindow,
    jfxPanel: JFXPanel,
    onCreate: () -> Unit,
    onDestroy: () -> Unit = {}
) {
    val jPanel = remember { JPanel() }
    val density = LocalDensity.current.density
    Layout(
        content = {},
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero).round()
            val size = coordinates.size
            jPanel.setBounds(
                (location.x / density).toInt(),
                (location.y / density).toInt(),
                (size.width / density).toInt(),
                (size.height / density).toInt()
            )
            jPanel.validate()
            jPanel.repaint()
        },
        measurePolicy = { _, _ -> layout(0, 0) {} }
    )
    DisposableEffect(jPanel) {
        composeWindow.add(jPanel)
        jPanel.layout = BorderLayout(0, 0)
        jPanel.add(jfxPanel)
        onCreate()
        onDispose {
            onDestroy()
            composeWindow.remove(jPanel)
        }
    }
}

@Composable
fun FirstWindow(yesOnClick: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Are u loser?", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(16.dp))
            Row {
                Button(onClick = yesOnClick) {
                    Text("Yes")
                }
                Spacer(Modifier.width(16.dp))
                val random = remember { { Random.nextInt(-100, 100).dp } }
                var x by remember { mutableStateOf(0.dp) }
                var y by remember { mutableStateOf(0.dp) }
                val xAnim by animateDpAsState(x)
                val yAnim by animateDpAsState(y)
                Button(modifier = Modifier.offset(xAnim, yAnim), onClick = {
                    x = random()
                    y = random()
                }) {
                    Text("No")
                }
            }
        }
    }
}
