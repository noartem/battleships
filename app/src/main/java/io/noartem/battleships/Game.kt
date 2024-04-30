package io.noartem.battleships

data class Game(
    val started: Boolean = false,
    val userBoard: Board = randomBoard(),
    val computerBoard: Board = emptyBoard(),
    val computerMissing: Int = 0,
) {
    val finished get() = userBoard.lost || computerBoard.lost

    fun start() =
        if (started || !userBoard.isValid())
            this
        else
            copy(
                started = true,
                computerBoard = randomBoard()
            )

    fun toggleUserCell(i: Int, j: Int, cell: CellState) =
        copy(
            userBoard = userBoard.set(
                i, j,
                when (userBoard.at(i, j)) {
                    CellState.EMPTY -> cell
                    else -> CellState.EMPTY
                }
            )
        )

    fun attack(i: Int, j: Int) =
        computerBoard.receiveAttack(i, j).let { (computerBoard, userAttackResult) ->
            Pair(copy(computerBoard = computerBoard), userAttackResult)
        }

    fun computerAttack(): Pair<Game, AttackResult?> {
        if (computerMissing > 0) {
            return Pair(copy(computerMissing = computerMissing - 1), null)
        }

        var computerMissingAfter = computerMissing
        val (userBoardAfter, computerAttackResult) = userBoard.receiveRandomAttack()
        if (computerAttackResult == AttackResult.MINE_BLOWN) {
            computerMissingAfter++
        }

        return Pair(
            copy(
                userBoard = userBoardAfter,
                computerMissing = computerMissingAfter
            ),
            computerAttackResult
        )
    }
}
