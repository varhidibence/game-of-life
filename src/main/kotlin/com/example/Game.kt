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
import javafx.scene.layout.TilePane
import javafx.scene.paint.Color
import javafx.stage.Stage

class Game : Application() {

    companion object {
        private const val WIDTH = 512
        private const val HEIGHT = 512
    }

    private lateinit var mainScene: Scene
    private lateinit var graphicsContext: GraphicsContext

    private lateinit var space: Image
    private lateinit var sun: Image

    private var lastFrameTime: Long = System.nanoTime()

    // use a set so duplicates are not possible
    private val currentlyActiveKeys = mutableSetOf<KeyCode>()

    override fun start(mainStage: Stage) {
        mainStage.title = "Event Handling"

        val root = Group()
        mainScene = Scene(root)
        mainStage.scene = mainScene

        val hbox = HBox()
        hbox.spacing = 10.0

        val startButton = Button("start")
        val stopButton = Button("stop")

        hbox.children.add(startButton)
        hbox.children.add(stopButton)

        val canvas = Canvas(WIDTH.toDouble(), HEIGHT.toDouble())
        root.children.add(canvas)
        root.children.add(hbox)


        prepareActionHandlers()

        graphicsContext = canvas.graphicsContext2D

        loadGraphics()

        // Main loop
        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                tickAndRender(currentNanoTime)
            }
        }.start()

        mainStage.show()
    }

    private fun prepareActionHandlers() {
        mainScene.onKeyPressed = EventHandler { event ->
            currentlyActiveKeys.add(event.code)
        }
        mainScene.onKeyReleased = EventHandler { event ->
            currentlyActiveKeys.remove(event.code)
        }
    }

    private fun loadGraphics() {
        // prefixed with / to indicate that the files are
        // in the root of the "resources" folder
        space = Image(getResource("/space.png"))
        sun = Image(getResource("/sun.png"))
    }

    private fun tickAndRender(currentNanoTime: Long) {
        // the time elapsed since the last frame, in nanoseconds
        // can be used for physics calculation, etc
        val elapsedNanos = currentNanoTime - lastFrameTime
        lastFrameTime = currentNanoTime

        // clear canvas
        graphicsContext.clearRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())

        // draw background
        graphicsContext.drawImage(space, 0.0, 0.0)

        // perform world updates


        // draw sun
        //graphicsContext.drawImage(sun, sunX.toDouble(), sunY.toDouble())

        // display crude fps counter
        val elapsedMs = elapsedNanos / 1_000_000
        if (elapsedMs != 0L) {
            graphicsContext.fill = Color.WHITE
            graphicsContext.fillText("${1000 / elapsedMs} fps", 10.0, 10.0)
        }
    }



}
