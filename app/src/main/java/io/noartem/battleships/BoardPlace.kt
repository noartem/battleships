package io.noartem.battleships

fun Board.cloneValue() =
    Array(BOARD_SIZE) { value[it].clone() }

fun Board.set(i: Int, j: Int, cell: CellState): Board {
    val newValue = cloneValue()
    newValue[i][j] = cell
    return Board(newValue)
}
