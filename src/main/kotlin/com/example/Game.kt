package com.example

import javafx.animation.AnimationTimer
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.awt.Rectangle
import java.util.*
import kotlin.math.floor
import kotlin.random.Random

class Game : Application() {

    companion object {
        private const val WIDTH = 800
        private const val HEIGHT = 600
        private const val BOARD_W = 40
        private const val BOARD_H = 40
        private const val startX = 50.0
        private const val startY = 50.0
        private const val cellSize = 15.0

    }

    private lateinit var stopButton: Button
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var mainScene: Scene
    private lateinit var graphicsContext: GraphicsContext

    private lateinit var space: Image

    private var board = Array(BOARD_H){ IntArray(BOARD_W)}

    private lateinit var timer: AnimationTimer
    private var running = false
    private var lastFrameTime: Long = System.nanoTime()
    private var lastUpdated = lastFrameTime
    private val fps = 10

    // use a set so duplicates are not possible
    private val currentlyActiveKeys = mutableSetOf<KeyCode>()

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

         timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                tickAndRender(currentNanoTime)
            }
        }


        controlBox.children.add(startButton)
        controlBox.children.add(stopButton)
        controlBox.children.add(resetButton)

        val canvas = Canvas(WIDTH.toDouble(), HEIGHT.toDouble())


        root.children.add(canvas)
        root.children.add(controlBox)

        //randomize()

        for (i in 2..2){
            for (j in 1..3)
                board[i][j] = 1
        }

        prepareActionHandlers()

        graphicsContext = canvas.graphicsContext2D
        drawBoard(startX, startY)

        loadGraphics()

        // Main loop

        mainStage.show()
    }

    private fun randomize() {
        val rand = Random(500)
        for (row in 0 until board.size) {
            for (col in 0 until board[row].size) {
                board[row][col] = (rand.nextInt() % 2)
            }
        }
    }

    private fun drawBoard(startX: Double, startY: Double){

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
        startButton.onAction = EventHandler { event ->
            if (!running) {
                timer.start()
                running = true
            }
        }
        stopButton.onAction = EventHandler { event ->
            if (running){
                timer.stop()
                running = false
            }
        }

        resetButton.onAction = EventHandler { event ->
            running = false
            board = Array(BOARD_H){ IntArray(BOARD_W)}
            graphicsContext.clearRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())
            drawBoard(startX, startY)
            timer.stop()
        }

        mainScene.onMouseClicked = EventHandler { event ->
            if (!running){
                val area = Rectangle(startX.toInt(), startY.toInt(), (board.size*cellSize).toInt(), (board.size*cellSize).toInt())
                if (area.contains(event.x, event.y)){
                    val colNth = floor((event.x - startX) / cellSize).toInt()
                    val rowNth = floor((event.y - startY) / cellSize).toInt()
                    println("Mouse clicked: ${rowNth}, ${colNth}")
                    if (rowNth >= 0 && rowNth < board.size && colNth >= 0 && colNth < board.size)
                        board[rowNth][colNth] = 1
                    graphicsContext.clearRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())
                    drawBoard(startX, startY)
                }
            }
        }
    }

    private fun loadGraphics() {
        // prefixed with / to indicate that the files are
        // in the root of the "resources" folder
        space = Image(getResource("/space.png"))
    }

    private fun tickAndRender(currentNanoTime: Long) {
        // the time elapsed since the last frame, in miliseconds
        val elapsedNanos = currentNanoTime - lastFrameTime
        lastFrameTime = currentNanoTime

        // clear canvas
        graphicsContext.clearRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())

        //graphicsContext.drawImage(space, 0.0, 0.0)
        drawBoard(startX, startY)

        // perform world updates
        if (currentNanoTime - lastUpdated > fps * 100_000_000) {
            println(currentNanoTime - lastUpdated)
            board = nextGeneration()
            lastUpdated = currentNanoTime
        }

        // display crude fps counter
        //val elapsedMs = elapsedNanos / 1_000_000
        //if (elapsedMs != 0L) {
        //    graphicsContext.fill = Color.WHITE
        //    graphicsContext.fillText("${1000 / elapsedMs} fps", 10.0, 10.0)
        //}
    }



}
