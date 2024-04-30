package io.noartem.battleships

const val BOARD_SIZE = 10

enum class CellState {
    EMPTY, SHIP, SHIP_HIT, MINE, MINE_BLOWN, MISS
}

enum class AttackResult {
    WON, SHIP_HIT, SHIP_BLOWN, MINE_BLOWN, MISS
}

fun emptyBoard() =
    Board(Array(BOARD_SIZE) { Array(BOARD_SIZE) { CellState.EMPTY } })

fun randomBoard() =
    Board(
        arrayOf(
            arrayOf(S, o, S, o, o, o, o, o, o, o),
            arrayOf(S, o, S, o, o, o, o, o, S, o),
            arrayOf(S, o, S, o, o, o, S, o, S, o),
            arrayOf(S, o, o, o, o, o, S, o, o, o),
            arrayOf(o, o, S, o, S, o, o, o, o, o),
            arrayOf(o, o, o, o, S, o, o, o, o, o),
            arrayOf(o, o, o, o, o, o, o, o, o, o),
            arrayOf(o, o, M, o, o, S, o, S, o, o),
            arrayOf(o, o, o, o, o, S, o, o, o, M),
            arrayOf(o, o, o, o, o, S, o, o, o, o),
        )
    ).randomTransform().randomTransform().randomTransform()


data class Board(val value: Array<Array<CellState>>) {
    val shipsCount get() = value.sumOf { it.count { it == CellState.SHIP } }

    val lost get() = shipsCount == 0

    fun at(i: Int, j: Int) =
        if (i in 0 until BOARD_SIZE && j in 0 until BOARD_SIZE) value[i][j] else CellState.EMPTY

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}
