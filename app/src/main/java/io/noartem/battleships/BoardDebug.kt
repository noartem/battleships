package io.noartem.battleships

fun Board.stringify() =
    value.joinToString(separator = "\n") { row ->
        row.joinToString(separator = " ") { cell ->
            when (cell) {
                CellState.EMPTY -> "."
                CellState.SHIP -> "S"
                CellState.SHIP_HIT -> "X"
                CellState.MINE -> "M"
                CellState.MINE_BLOWN -> "B"
                CellState.MISS -> "O"
            }
        }
    }
