package com.example

import javafx.animation.AnimationTimer
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
import java.util.*
import kotlin.random.Random

class Game : Application() {

    companion object {
        private const val WIDTH = 800
        private const val HEIGHT = 600
    }

    private lateinit var mainScene: Scene
    private lateinit var graphicsContext: GraphicsContext

    private lateinit var space: Image

    private var running = false


    private var board = Array(100){ IntArray(100)}

    private var lastFrameTime: Long = System.currentTimeMillis()
    private var lastUpdated = lastFrameTime
    private val fps = 5

    // use a set so duplicates are not possible
    private val currentlyActiveKeys = mutableSetOf<KeyCode>()

    override fun start(mainStage: Stage) {
        mainStage.title = "Game of Life"

        val root = Group()
        mainScene = Scene(root)
        mainStage.scene = mainScene

        val controlBox = HBox()
        controlBox.spacing = 10.0
        val startButton = Button("start")
        val stopButton = Button("stop")
        val resetButton = Button("reset")

        class Timertask: TimerTask(){
            override fun run() {
                tickAndRender(System.currentTimeMillis())
            }

        }
        var task = Timertask()
        var timer = Timer()

        startButton.onAction = EventHandler { event ->
            if (!running) {
                timer = Timer()
                task = Timertask()
                timer.schedule(task,1)
                running = true
            }
        }

        stopButton.onAction = EventHandler { event ->
            if (running){
                timer.cancel()
                running = false
            }
        }

        controlBox.children.add(startButton)
        controlBox.children.add(stopButton)
        controlBox.children.add(resetButton)

        val canvas = Canvas(WIDTH.toDouble(), HEIGHT.toDouble())


        root.children.add(canvas)
        root.children.add(controlBox)

        randomize()

        prepareActionHandlers()

        graphicsContext = canvas.graphicsContext2D
        drawBoard(50.0, 50.0)

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
        val cellSize = 5.0

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
        val next = board.clone()
        val height = board.size
        val width = board[0].size

        // Loop through every cell
        for (row in 1 until (height-1)){
            for (col in 1 until (width-1)){

                // finding number of neighbours that are alive
                var aliveNeighbours = 0
                for (i in -1..1){
                    for (j in -1..1){
                        aliveNeighbours += board[row + i][col + j]
                    }
                }

                //The self cell should not been counted
                aliveNeighbours -= board[row][col]

                next[row][col] = when {
                    //Any live cell with two or three live neighbours survives.
                    board[row][col] == 1 && (aliveNeighbours == 2 || aliveNeighbours == 3) -> 0
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
        mainScene.onKeyPressed = EventHandler { event ->
            currentlyActiveKeys.add(event.code)
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
        drawBoard(50.0, 50.0)

        // perform world updates
        if (currentNanoTime - lastUpdated > fps * 1_000) {
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
