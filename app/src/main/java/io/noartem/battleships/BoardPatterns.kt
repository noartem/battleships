package io.noartem.battleships

val o = CellState.EMPTY
val S = CellState.SHIP
val M = CellState.MINE

val ship1Pattern = arrayOf(
    arrayOf(S),
)

val ship2VPattern = arrayOf(
    arrayOf(S),
    arrayOf(S),
)

val ship2HPattern = arrayOf(
    arrayOf(S, S)
)

val ship3VPattern = arrayOf(
    arrayOf(S),
    arrayOf(S),
    arrayOf(S),
)

val ship3HPattern = arrayOf(
    arrayOf(S, S, S),
)

val ship4VPattern = arrayOf(
    arrayOf(S),
    arrayOf(S),
    arrayOf(S),
    arrayOf(S)
)

val ship4HPattern = arrayOf(
    arrayOf(S, S, S, S),
)

val minePattern = arrayOf(
    arrayOf(M),
)

val patterns = arrayOf(
    ship1Pattern,
    ship2HPattern,
    ship2VPattern,
    ship3HPattern,
    ship3VPattern,
    ship4HPattern,
    ship4VPattern,
    minePattern
)

val shipAliases = arrayOf(CellState.SHIP, CellState.SHIP_HIT)
val emptyAliases = arrayOf(CellState.EMPTY, CellState.MISS)
val mineAliases = arrayOf(CellState.MINE, CellState.MINE_BLOWN)

val cellAliases = mapOf(
    CellState.SHIP to shipAliases,
    CellState.MINE to mineAliases,
    CellState.EMPTY to emptyAliases,
)

fun Board.isEmptyAt(i: Int, j: Int) =
    emptyAliases.contains(at(i, j))

fun Board.isPatternAt(i: Int, j: Int, pattern: Array<Array<CellState>>): Boolean {
    for (k in -1..pattern.size) {
        for (l in -1..pattern[0].size) {
            val cell = at(i + k, j + l)

            if (k in pattern.indices && l in pattern[0].indices) {
                val patternCell = pattern[k][l]
                val patterCellAlias = cellAliases[patternCell] ?: arrayOf()
                if (!patterCellAlias.contains(cell)) {
                    return false
                }
            } else {
                if (!emptyAliases.contains(cell)) {
                    return false
                }
            }
        }
    }

    return true
}

fun Board.countPattern(pattern: Array<Array<CellState>>) =
    (0 until BOARD_SIZE).sumOf { i ->
        (0 until BOARD_SIZE).count { j ->
            isPatternAt(i, j, pattern)
        }
    }

fun Board.makePatternsMask(): Array<Array<Boolean>> {
    val matrix = Array(BOARD_SIZE) { Array(BOARD_SIZE) { false } }
    for (i in 0 until BOARD_SIZE) {
        for (j in 0 until BOARD_SIZE) {
            val pattern = patterns.find { isPatternAt(i, j, it) }
            if (pattern == null) {
                continue
            }

            for (k in 0 until pattern.size) {
                for (l in 0 until pattern[0].size) {
                    matrix[i + k][j + l] = true
                }
            }
        }
    }

    return matrix
}

enum class CellValidated {
    VALID,
    NOT_VALID,
    EMPTY
}

fun Board.makeValidatedMatrix(): Array<Array<CellValidated>> {
    val patternsMask = makePatternsMask()

    return Array(BOARD_SIZE) { i ->
        Array(BOARD_SIZE) { j ->
            if (emptyAliases.contains(value[i][j]))
                CellValidated.EMPTY
            else if (patternsMask[i][j])
                CellValidated.VALID
            else
                CellValidated.NOT_VALID
        }
    }
}


enum class ShipDirection {
    VERTICAL,
    HORIZONTAL
}

data class Ship(
    val size: Int,
    val direction: ShipDirection,
    val i: Int,
    val j: Int,
)

data class Mine(
    val i: Int,
    val j: Int
)

fun Board.iterateShips() = sequence {
    for (i in 0 until BOARD_SIZE) {
        for (j in 0 until BOARD_SIZE) {
            if (isPatternAt(i, j, ship1Pattern)) {
                yield(Ship(1, ShipDirection.HORIZONTAL, i, j))
            } else if (isPatternAt(i, j, ship2VPattern)) {
                yield(Ship(2, ShipDirection.VERTICAL, i, j))
            } else if (isPatternAt(i, j, ship2HPattern)) {
                yield(Ship(2, ShipDirection.HORIZONTAL, i, j))
            } else if (isPatternAt(i, j, ship3VPattern)) {
                yield(Ship(3, ShipDirection.VERTICAL, i, j))
            } else if (isPatternAt(i, j, ship3HPattern)) {
                yield(Ship(3, ShipDirection.HORIZONTAL, i, j))
            } else if (isPatternAt(i, j, ship4VPattern)) {
                yield(Ship(4, ShipDirection.VERTICAL, i, j))
            } else if (isPatternAt(i, j, ship4HPattern)) {
                yield(Ship(4, ShipDirection.HORIZONTAL, i, j))
            }
        }
    }
}

fun Board.iterateMines() = sequence {
    for (i in 0 until BOARD_SIZE) {
        for (j in 0 until BOARD_SIZE) {
            if (isPatternAt(i, j, minePattern)) {
                yield(Mine(i, j))
            }
        }
    }
}


fun Board.isValid(): Boolean {
    return countPattern(ship4HPattern) + countPattern(ship4VPattern) == 1 &&
            countPattern(ship3HPattern) + countPattern(ship3VPattern) == 2 &&
            countPattern(ship2HPattern) + countPattern(ship2VPattern) == 3 &&
            countPattern(ship1Pattern) == 2 &&
            countPattern(minePattern) == 2
}
