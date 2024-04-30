package io.noartem.battleships

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.noartem.battleships.ui.theme.BattleshipsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BattleshipsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = Color.Black
                ) {
                    GameView()
                }
            }
        }
    }
}

@Composable
fun BoardView(
    board: Board,
    strokeSize: Float = 3.5f,
    onClick: ((Int, Int) -> Unit)? = null,
    onLongClick: ((Int, Int) -> Unit)? = null,
    enabled: Boolean = true
) {
    val ship1Image = ImageBitmap.imageResource(id = R.drawable.ship_1)
    val ship2Image = ImageBitmap.imageResource(id = R.drawable.ship_2)
    val ship3Image = ImageBitmap.imageResource(id = R.drawable.ship_3)
    val ship4Image = ImageBitmap.imageResource(id = R.drawable.ship_4)
    val mineImage = ImageBitmap.imageResource(id = R.drawable.mine)

    var cellWidth by remember { mutableFloatStateOf(0f) }
    var cellHeight by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (enabled && onClick != null) {
                            val x = (it.x / (cellWidth + strokeSize)).toInt()
                            val y = (it.y / (cellHeight + strokeSize)).toInt()
                            onClick(y, x)
                        }
                    },
                    onLongPress = {
                        if (enabled && onLongClick != null) {
                            val x = (it.x / (cellWidth + strokeSize)).toInt()
                            val y = (it.y / (cellHeight + strokeSize)).toInt()
                            onLongClick(y, x)
                        }
                    }
                )
            }
    ) {
        cellWidth = (size.width - strokeSize * (BOARD_SIZE + 1)) / BOARD_SIZE
        cellHeight = (size.height - strokeSize * (BOARD_SIZE + 1)) / BOARD_SIZE

        drawRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, size.height)
        )

        val validatedMatrix = board.makeValidatedMatrix()

        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                drawRect(
                    color = when (validatedMatrix[i][j]) {
                        CellValidated.NOT_VALID -> Color(0xFFFF2222)
                        else -> when (board.at(i, j)) {
                            CellState.MISS -> Color.LightGray
                            CellState.SHIP_HIT, CellState.MINE_BLOWN -> Color(0xFF890000)
                            else -> Color.Black
                        }
                    },
                    topLeft = Offset(
                        j * (cellWidth + strokeSize) + strokeSize,
                        i * (cellHeight + strokeSize) + strokeSize
                    ),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }

        for (ship in board.iterateShips()) {
            val shipX = ship.j * (cellWidth + strokeSize) + strokeSize
            val shipY = ship.i * (cellHeight + strokeSize) + strokeSize
            val shipOffset = Offset(shipX, shipY)

            val rotate = ship.size > 1 && ship.direction == ShipDirection.HORIZONTAL

            withTransform({
                rotate(
                    degrees = if (rotate) -90f else 0f,
                    pivot = shipOffset
                )
            }) {
                drawImage(
                    image = Bitmap.createScaledBitmap(
                        when (ship.size) {
                            1 -> ship1Image.asAndroidBitmap()
                            2 -> ship2Image.asAndroidBitmap()
                            3 -> ship3Image.asAndroidBitmap()
                            else -> ship4Image.asAndroidBitmap()
                        },
                        cellWidth.toInt(),
                        (cellWidth * ship.size + strokeSize * (ship.size - 1)).toInt(),
                        false,
                    ).asImageBitmap(),
                    topLeft = Offset(
                        shipOffset.x - (if (rotate) cellWidth else 0f),
                        shipOffset.y
                    ),
                    colorFilter = ColorFilter.tint(
                        Color(0xFFBBBBFF),
                        blendMode = BlendMode.Modulate
                    )
                )
            }
        }

        for (mine in board.iterateMines()) {
            val mineX = mine.j * (cellWidth + strokeSize) + strokeSize
            val mineY = mine.i * (cellHeight + strokeSize) + strokeSize

            drawImage(
                image = Bitmap.createScaledBitmap(
                    mineImage.asAndroidBitmap(),
                    cellWidth.toInt(),
                    cellHeight.toInt(),
                    false,
                ).asImageBitmap(),
                topLeft = Offset(mineX, mineY)
            )
        }
    }
}

@Composable
fun GameView(
    gameViewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var clickDisabled by remember { mutableStateOf(false) }

    val game by gameViewModel.stateFlow.collectAsState()

    val computerDisplayedBoard = game.computerBoard.mapCells {
        when (it) {
            CellState.SHIP, CellState.MINE -> CellState.EMPTY
            else -> it
        }
    }

    fun playAttackSound(attackResult: AttackResult) =
        MediaPlayer.create(
            context, when (attackResult) {
                AttackResult.WON -> R.raw.victory
                AttackResult.SHIP_HIT -> R.raw.hit
                AttackResult.SHIP_BLOWN -> R.raw.explosion
                AttackResult.MISS, AttackResult.MINE_BLOWN -> R.raw.shoot
            }
        ).start()

    suspend fun computerAttack() {
        while (true) {
            val computerAttackResult = gameViewModel.computerAttack() ?: break

            if (computerAttackResult == AttackResult.WON) {
                playAttackSound(AttackResult.SHIP_BLOWN)
            } else {
                playAttackSound(computerAttackResult)
            }
            delay(1500)

            if (
                computerAttackResult == AttackResult.MINE_BLOWN ||
                computerAttackResult == AttackResult.MISS
            ) {
                break
            }
        }
    }

    suspend fun attack(i: Int, j: Int) {
        if (game.finished || clickDisabled) {
            return
        }

        clickDisabled = true

        val attackResult = gameViewModel.attack(i, j) ?: return

        if (attackResult == AttackResult.WON) {
            playAttackSound(AttackResult.SHIP_BLOWN)
            delay(600)
            playAttackSound(AttackResult.WON)
        } else {
            playAttackSound(attackResult)
        }
        delay(1500)

        if (attackResult == AttackResult.MINE_BLOWN) {
            computerAttack()
            computerAttack()
        } else if (attackResult == AttackResult.MISS) {
            computerAttack()
        }

        clickDisabled = false
    }

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally)
        ) {
            if (game.started) {
                BoardView(
                    board = game.userBoard,
                    enabled = !game.finished
                )
            } else {
                BoardView(
                    board = game.userBoard,
                    onClick = { i, j -> gameViewModel.toggleUserCell(i, j, CellState.SHIP) },
                    onLongClick = { i, j -> gameViewModel.toggleUserCell(i, j, CellState.MINE) }
                )
            }
        }

        Spacer(
            Modifier.padding(0.dp, 16.dp, 0.dp, 0.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally)
        ) {
            if (game.started) {
                BoardView(
                    board = if (game.finished) game.computerBoard else computerDisplayedBoard,
                    enabled = !game.finished,
                    onClick = { i, j -> coroutineScope.launch { attack(i, j) } }
                )
            }
        }

        Spacer(
            Modifier.padding(0.dp, 16.dp, 0.dp, 0.dp)
        )

        if (game.started) {
            Button(
                onClick = { gameViewModel.restart() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
            ) {
                if (game.finished) {
                    Text(
                        text =
                        (if (game.userBoard.lost) "Вы проиграли. " else "Вы выиграли! ") + "Сыграть еще раз"
                    )
                } else {
                    Text(text = "Перезапустить")
                }
            }
        } else {
            Button(
                onClick = { gameViewModel.start() },
                enabled = game.userBoard.isValid(),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
            ) {
                Text(text = "Начать!")
            }
        }
    }
}
