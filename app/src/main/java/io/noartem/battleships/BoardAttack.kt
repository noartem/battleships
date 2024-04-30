package io.noartem.battleships


fun Board.receiveAttack(i: Int, j: Int) =
    when (at(i, j)) {
        CellState.EMPTY -> Pair(set(i, j, CellState.MISS), AttackResult.MISS)
        CellState.SHIP -> Pair(
            set(i, j, CellState.SHIP_HIT),
            if (shipsCount == 1) AttackResult.WON
            else if (isShipWillBeBlownAt(i, j)) AttackResult.SHIP_BLOWN
            else AttackResult.SHIP_HIT
        )

        CellState.MINE -> Pair(set(i, j, CellState.MINE_BLOWN), AttackResult.MINE_BLOWN)
        CellState.SHIP_HIT, CellState.MINE_BLOWN, CellState.MISS -> Pair(this, null)
    }


private fun Board.isChainNear(i: Int, j: Int, cell: CellState): Boolean =
    when (at(i, j)) {
        cell -> true
        CellState.EMPTY -> false
        else -> isChainNear(i - 1, j, cell)
                || isChainNear(i, j - 1, cell)
                || isChainNear(i + 1, j, cell)
                || isChainNear(i, j + 1, cell)
    }

private fun Board.isShipWillBeBlownAt(i: Int, j: Int): Boolean {
    var l = 1
    while (true) {
        if (at(i, j - l) == CellState.EMPTY) break
        if (at(i, j - l) != CellState.SHIP_HIT) return false
        l++
    }

    var r = 1
    while (true) {
        if (at(i, j + r) == CellState.EMPTY) break
        if (at(i, j + r) != CellState.SHIP_HIT) return false
        r++
    }

    var t = 1
    while (true) {
        if (at(i - t, j) == CellState.EMPTY) break
        if (at(i - t, j) != CellState.SHIP_HIT) return false
        t++
    }

    var b = 1
    while (true) {
        if (at(i + b, j) == CellState.EMPTY) break
        if (at(i + b, j) != CellState.SHIP_HIT) return false
        b++
    }

    return true
}


fun Board.receiveRandomAttack() =
    receiveAttack(
        (0..<BOARD_SIZE).random(), (0..<BOARD_SIZE).random()
    )
