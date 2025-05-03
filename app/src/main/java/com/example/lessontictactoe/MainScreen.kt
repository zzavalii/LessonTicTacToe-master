package com.example.lessontictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

const val DIM = 3

enum class GameState {
    IN_PROGRESS,
    CROSS_WIN,
    NOUGHT_WIN,
    DRAW
}

enum class Player {
    CROSS,
    NOUGHT
}

enum class CellState {
    EMPTY,
    CROSS,
    NOUGHT
}

val Player.mark: CellState
    get() = when (this) {
        Player.CROSS -> CellState.CROSS
        Player.NOUGHT -> CellState.NOUGHT
    }

fun checkGameState(field: List<CellState>, size: Int): GameState {
    val lines = mutableListOf<List<Int>>()

    // Горизонталі
    for (row in 0 until size) {
        lines.add((0 until size).map { col -> row * size + col })
    }

    // Вертикалі
    for (col in 0 until size) {
        lines.add((0 until size).map { row -> row * size + col })
    }

    // Діагоналі
    lines.add((0 until size).map { it * size + it })
    lines.add((0 until size).map { it * size + (size - it - 1) })

    for (line in lines) {
        val values = line.map { field[it] }
        if (values.all { it == CellState.CROSS }) return GameState.CROSS_WIN
        if (values.all { it == CellState.NOUGHT }) return GameState.NOUGHT_WIN
    }

    return if (field.any { it == CellState.EMPTY }) GameState.IN_PROGRESS else GameState.DRAW
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var selectedBoardSize by remember { mutableStateOf<Int?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (selectedBoardSize == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Оберіть розмір поля", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf(3, 4, 5).forEach { size ->
                        Button(
                            onClick = { selectedBoardSize = size },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("${size}x$size")
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { isDarkTheme = !isDarkTheme }) {
                        Text("Змінити тему")
                    }
                }
            } else {
                GameScreen(
                    boardSize = selectedBoardSize!!,
                    onResetBoardSize = { selectedBoardSize = null },
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun GameScreen(
    boardSize: Int,
    onResetBoardSize: () -> Unit,
    onToggleTheme: () -> Unit
) {
    var field by remember { mutableStateOf(MutableList(boardSize * boardSize) { CellState.EMPTY }) }
    var currentPlayer by remember { mutableStateOf(Player.CROSS) }
    var gameState by remember { mutableStateOf(GameState.IN_PROGRESS) }

    var crossWins by remember { mutableStateOf(0) }
    var noughtWins by remember { mutableStateOf(0) }

    var timeLeft by remember { mutableStateOf(5) }

    LaunchedEffect(key1 = currentPlayer, key2 = gameState) {
        if (gameState == GameState.IN_PROGRESS) {
            timeLeft = 5
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            if (gameState == GameState.IN_PROGRESS) {
                gameState = if (currentPlayer == Player.CROSS) GameState.NOUGHT_WIN else GameState.CROSS_WIN
                if (gameState == GameState.CROSS_WIN) crossWins++ else noughtWins++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                Text("X: $crossWins", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("O: $noughtWins", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Хід: ${if (currentPlayer == Player.CROSS) "X" else "O"}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Час: $timeLeft сек",
            fontSize = 18.sp,
            color = if (timeLeft <= 3) Color.Red else Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GameBoard(
            boardSize = boardSize,
            field = field,
            onCellClick = { index ->
                if (field[index] == CellState.EMPTY && gameState == GameState.IN_PROGRESS) {
                    field = field.toMutableList().also { it[index] = currentPlayer.mark }
                    gameState = checkGameState(field, boardSize)
                    when (gameState) {
                        GameState.CROSS_WIN -> crossWins++
                        GameState.NOUGHT_WIN -> noughtWins++
                        else -> {}
                    }
                    if (gameState == GameState.IN_PROGRESS) {
                        currentPlayer = if (currentPlayer == Player.CROSS) Player.NOUGHT else Player.CROSS
                    }
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    field = MutableList(boardSize * boardSize) { CellState.EMPTY }
                    gameState = GameState.IN_PROGRESS
                    currentPlayer = Player.CROSS
                    timeLeft = 5
                }, modifier = Modifier.weight(1f)) {
                    Text("Скинути")
                }

                Button(onClick = {
                    field = MutableList(boardSize * boardSize) { CellState.EMPTY }
                    gameState = GameState.IN_PROGRESS
                    currentPlayer = Player.CROSS
                    crossWins = 0
                    noughtWins = 0
                    timeLeft = 5
                }, modifier = Modifier.weight(1f)) {
                    Text("Нова гра")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onToggleTheme, modifier = Modifier.weight(1f)) {
                    Text("Тема")
                }

                Button(onClick = onResetBoardSize, modifier = Modifier.weight(1f)) {
                    Text("⬅ Назад")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (gameState != GameState.IN_PROGRESS) {
            Text(
                text = when (gameState) {
                    GameState.CROSS_WIN -> "Переможець: X"
                    GameState.NOUGHT_WIN -> "Переможець: O"
                    GameState.DRAW -> "Нічия"
                    else -> ""
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun GameBoard(
    boardSize: Int,
    field: List<CellState>,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        for (row in 0 until boardSize) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (col in 0 until boardSize) {
                    val index = row * boardSize + col
                    Card(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp)
                            .clickable { onCellClick(index) },
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        ) {
                            Text(
                                text = when (field[index]) {
                                    CellState.CROSS -> "X"
                                    CellState.NOUGHT -> "O"
                                    else -> ""
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold

                            )
                        }
                    }
                }
            }
        }
    }
}
