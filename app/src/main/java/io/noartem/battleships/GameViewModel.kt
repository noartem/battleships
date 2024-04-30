package io.noartem.battleships

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val state = MutableStateFlow(Game())
    val stateFlow: StateFlow<Game> = state.asStateFlow()

    fun toggleUserCell(i: Int, j: Int, cell: CellState) =
        state.update { it.toggleUserCell(i, j, cell) }

    fun start() =
        state.update { it.start() }

    fun restart() =
        state.update { Game() }

    fun attack(i: Int, j: Int): AttackResult? {
        val (newValue, userAttackResult) = state.value.attack(i, j)
        state.update { newValue }
        return userAttackResult
    }

    fun computerAttack(): AttackResult? {
        val (newValue, computerAttackResult) = state.value.computerAttack()
        state.update { newValue }
        return computerAttackResult
    }
}