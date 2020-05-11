package minesweeper

import java.util.*
import kotlin.random.Random

class Minesweeper(val width: Int, val height: Int, val minesCount: Int) {
    private var markedCellsCount = 0
    private var hiddenCellsCount = width * height

    private var realField: Array<CharArray> = Array(height){ CharArray(width){ '/' } }
    private var displayedField: Array<CharArray> = Array(height){ CharArray(width){ '.' } }

    private var gameState = GameState.BeforeMinesGenerated

    private val scanner = Scanner(System.`in`)

    companion object {
        fun createGame(): Minesweeper {
            print("How many mines do you want on the field? ")
            val mines = readLine()!!.toInt()
            val width = 9
            val height = 9
            return Minesweeper(width, height, mines)
        }
    }

    fun play() {
        printField()
        while (gameState.canContinue()) {
            act()
        }
        when (gameState) {
            GameState.Win -> println("Congratulations! You found all mines!")
            GameState.Lose -> println("You stepped on a mine and failed!")
        }
    }

    private fun act() {
        print("Set/unset mines marks or claim a cell as free: ")
        val col = scanner.nextInt() - 1
        val row = scanner.nextInt() - 1
        when (scanner.next()) {
            "free" -> {
                if (gameState == GameState.BeforeMinesGenerated) {
                    generateMines(row, col)
                    gameState = GameState.AfterMinesGenerated
                }
                freeCell(row, col)
            }
            "mine" -> markCell(row, col)
        }
        printField()
    }

    private fun generateMines(firstCellRow: Int, firstCellCol: Int) {
        var minesCount = this.minesCount
        var all = height * width - 1
        for (i in realField.indices) {
            for (j in realField[i].indices) {
                if (i == firstCellRow && j == firstCellCol) {
                    continue
                }
                if (Random.nextInt(0, all--) < minesCount) {
                    minesCount--
                    addMine(i, j)
                }
                if (minesCount == 0) {
                    break
                }
            }
            if (minesCount == 0) {
                break
            }
        }
    }

    private fun addMine(row: Int, col: Int) {
        for (row1 in row - 1.. row + 1) {
            for (col1 in col - 1..col + 1) {
                if (row1 < 0 || row1 >= realField.size ||
                    col1 < 0 || col1 >= realField[row].size ||
                    realField[row1][col1] == 'X') {
                    continue
                }
                if (row1 == row && col1 == col) {
                    realField[row1][col1] = 'X'
                } else if (realField[row1][col1] == '/') {
                    realField[row1][col1] = '1'
                }  else {
                    realField[row1][col1]++
                }
            }
        }
    }

    private fun freeCell(row: Int, col: Int) {
        if (row < 0 || row >= height || col < 0 || col >= width ||
            displayedField[row][col] != '.' && displayedField[row][col] != '*') {
            return
        }

        displayedField[row][col] = realField[row][col]
        hiddenCellsCount--
        when (realField[row][col]) {
            'X' -> {
                for (row1 in realField.indices) {
                    for (col1 in realField[row1].indices) {
                        if (realField[row1][col1] == 'X') {
                            displayedField[row1][col1] = 'X'
                        }
                    }
                }
                gameState = GameState.Lose
            }
            '/' -> {
                for (row1 in row - 1.. row + 1) {
                    for (col1 in col - 1..col + 1) {
                        if (row1 == row && col1 == col) continue
                        freeCell(row1, col1)
                    }
                }
            }
        }
        if (gameState.canContinue() && minesCount == hiddenCellsCount + markedCellsCount) {
            gameState = GameState.Win
        }
    }

    private fun markCell(row: Int, col: Int) {
        when (displayedField[row][col]) {
            '.' -> {
                displayedField[row][col] = '*'
                hiddenCellsCount--
                markedCellsCount++
            }
            '*' -> {
                displayedField[row][col] = '.'
                hiddenCellsCount++
                markedCellsCount--
            }
        }
    }

    private fun printField() {
        println(" │${IntRange(1, width).joinToString("")}│")
        println("—│${"—".repeat(width)}│")
        for((i, row) in displayedField.withIndex()) {
            print("${i + 1}│")
            for (cell in row) {
                print(cell)
            }
            println("│")
        }
        println("—│${"—".repeat(width)}│")
    }

    private enum class GameState {
        BeforeMinesGenerated,
        AfterMinesGenerated,
        Win,
        Lose;

        fun canContinue() = this != Win && this != Lose
    }
}