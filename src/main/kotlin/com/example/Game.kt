package com.example

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.awt.Rectangle
import kotlin.math.floor
import kotlin.random.Random

class Game : Application() {

    companion object {
        private const val WIDTH = 800
        private const val HEIGHT = 700
        private const val BOARD_W = 40
        private const val BOARD_H = 40
        private const val startX = 50.0
        private const val startY = 50.0
        private const val cellSize = 15.0

    }

    private lateinit var mainScene: Scene
    private lateinit var graphicsContext: GraphicsContext

    private lateinit var stopButton: Button
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var randButton: Button

    private var board = Array(BOARD_H){ IntArray(BOARD_W)}

    private lateinit var timer: AnimationTimer
    private var running = false
    private var lastUpdated: Long = System.nanoTime()
    private val fps = 4
    private var initialSeed = 50

    override fun start(mainStage: Stage) {
        mainStage.title = "Game of Life"

        val root = Group()
        mainScene = Scene(root)
        mainStage.scene = mainScene

        val controlBox = HBox()
        controlBox.spacing = 10.0
        startButton = Button("start")
        stopButton = Button("stop")
        resetButton = Button("reset")
        randButton = Button("randomize")

         timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                tickAndRender(currentNanoTime)
            }
        }


        controlBox.children.add(startButton)
        controlBox.children.add(stopButton)
        controlBox.children.add(resetButton)
        controlBox.children.add(randButton)

        val canvas = Canvas(WIDTH.toDouble(), HEIGHT.toDouble())

        root.children.add(canvas)
        root.children.add(controlBox)

        randomize()

        for (i in 2..2){
            for (j in 1..3)
                board[i][j] = 1
        }

        prepareActionHandlers()

        graphicsContext = canvas.graphicsContext2D

        // Initial drawing
        drawBoard()
        mainStage.show()
    }

    private fun randomize() {
        val rand = Random(initialSeed++)
        for (row in 0 until board.size) {
            for (col in 0 until board[row].size) {
                board[row][col] = (rand.nextInt() % 2)
            }
        }
    }

    private fun drawBoard(){

        graphicsContext.stroke = Color.BLACK
        graphicsContext.fill = Color.BLACK
        for (row in 0 until board.size){
            for (col in 0 until board[row].size){
                if (board[row][col] == 1){
                    graphicsContext.fillRect(startX + col*cellSize, startY + row*cellSize, cellSize, cellSize)
                } else {
                    graphicsContext.strokeRect(startX + col*cellSize, startY + row*cellSize, cellSize, cellSize)
                }

            }
        }
    }

    private fun nextGeneration():Array<IntArray>{
        val next = Array(BOARD_H){ IntArray(BOARD_W)}
        val height = board.size
        val width = board[0].size

        // Loop through every cell
        for (row in 1 until (height-1)){
            for (col in 1 until (width-1)){

                // finding number of neighbours that are alive
                var aliveNeighbours = 0
                for (i in -1..1){
                    for (j in -1..1){
                        val value = board[row + i][col + j]
                        aliveNeighbours += value
                    }
                }

                //The self cell should not been counted
                aliveNeighbours -= board[row][col]

                next[row][col] = when {
                    //Any live cell with two or three live neighbours survives.
                    board[row][col] == 1 && (aliveNeighbours == 2 || aliveNeighbours == 3) -> 1
                    //Any dead cell with three live neighbours becomes a live cell.
                    board[row][col] == 0 && aliveNeighbours == 3 -> 1
                    //All other live cells die in the next generation. Similarly, all other dead cells stay dead.
                    else -> 0
                }
            }
        }
        return next

    }

    private fun prepareActionHandlers() {
        startButton.onAction = EventHandler {
            if (!running) {
                timer.start()
                running = true
            }
        }
        stopButton.onAction = EventHandler {
            if (running){
                timer.stop()
                running = false
            }
        }

        resetButton.onAction = EventHandler {
            running = false
            board = Array(BOARD_H){ IntArray(BOARD_W)}
            repaint()
            timer.stop()
        }

        randButton.onAction = EventHandler {
            running = false
            board = Array(BOARD_H) { IntArray(BOARD_W) }
            randomize()
            repaint()
            timer.stop()
        }

        mainScene.onMouseClicked = EventHandler { event ->
            if (!running){
                val area = Rectangle(startX.toInt(), startY.toInt(), (board.size*cellSize).toInt(), (board.size*cellSize).toInt())
                if (area.contains(event.x, event.y)){
                    val colNth = floor((event.x - startX) / cellSize).toInt()
                    val rowNth = floor((event.y - startY) / cellSize).toInt()
                    println("Mouse clicked: $rowNth, $colNth")
                    if (rowNth >= 0 && rowNth < board.size && colNth >= 0 && colNth < board.size)
                        board[rowNth][colNth] = when (board[rowNth][colNth]){
                            0 -> 1
                            else -> 0
                        }
                    repaint()
                }
            }
        }
    }

    private fun repaint(){
        // clear canvas
        graphicsContext.clearRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())
        drawBoard()
    }

    private fun tickAndRender(currentNanoTime: Long) {

        // perform board updates
        if (currentNanoTime - lastUpdated > (1.0/fps) * 1_000_000_000) {
            println((1.0/fps) * 100_000_000)
            board = nextGeneration()
            lastUpdated = currentNanoTime
        }

        repaint()

    }

}
