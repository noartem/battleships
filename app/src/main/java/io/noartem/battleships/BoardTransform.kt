package io.noartem.battleships

fun Board.mapCells(mapper: (CellState) -> CellState): Board {
    val newValue = cloneValue()
    for (i in 0 until BOARD_SIZE) {
        for (j in 0 until BOARD_SIZE) {
            newValue[i][j] = mapper(value[i][j])
        }
    }
    return Board(newValue)
}

private fun Board.mapIndexes(transform: (Int, Int) -> Pair<Int, Int>): Board {
    val newValue = cloneValue()
    for (i in 0 until BOARD_SIZE) {
        for (j in 0 until BOARD_SIZE) {
            val (x, y) = transform(i, j)
            newValue[x][y] = value[i][j]
        }
    }
    return Board(newValue)
}

private fun Board.rotate90() =
    mapIndexes { i, j -> Pair(j, BOARD_SIZE - i - 1) }

private fun Board.rotate180() =
    mapIndexes { i, j -> Pair(BOARD_SIZE - i - 1, BOARD_SIZE - j - 1) }

private fun Board.rotate270() =
    mapIndexes { i, j -> Pair(BOARD_SIZE - j - 1, i) }

private fun Board.flipHorizontal() =
    mapIndexes { i, j -> Pair(i, BOARD_SIZE - j - 1) }

private fun Board.flipVertical() =
    mapIndexes { i, j -> Pair(BOARD_SIZE - i - 1, j) }

fun Board.randomTransform() =
    when ((0..6).random()) {
        1 -> rotate90()
        2 -> rotate180()
        3 -> rotate270()
        4 -> flipHorizontal()
        5 -> flipVertical()
        else -> this
    }