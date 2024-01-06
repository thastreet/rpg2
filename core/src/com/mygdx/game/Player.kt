package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class Player(private val listener: Listener) {
    interface Listener {
        fun canMove(rect: Rectangle): Boolean
        fun moved(rect: Rectangle)
        fun interceptAction(): Boolean
    }

    private lateinit var spritesTexture: Texture
    private lateinit var slashTexture: Texture
    private lateinit var region: Array<Array<TextureRegion>>
    private lateinit var walkRightAnimation: Animation<TextureRegion>
    private lateinit var walkLeftAnimation: Animation<TextureRegion>
    private lateinit var walkUpAnimation: Animation<TextureRegion>
    private lateinit var walkDownAnimation: Animation<TextureRegion>
    private lateinit var slashAnimation: Animation<TextureRegion>
    private var running = false
    private var speed = 1f
    private val movementDistance = 64
    private val walkFrameDuration = 1 / 6f
    var direction = Input.Keys.DOWN
        private set
    private var walking = false
    private val pos = Vector2()
    private var attacking = false
    private var animationTime = 0f

    val rect: Rectangle
        get() = getRect(pos.x, pos.y)

    private fun getRect(x: Float, y: Float) =
        Rectangle(x + 8, y, spritesTexture.width / 12f - 2 * 8, 16f)

    fun create() {
        spritesTexture = Texture(Gdx.files.internal("sprites.png"))
        slashTexture = Texture(Gdx.files.internal("slash.png"))

        region = TextureRegion.split(
            spritesTexture,
            spritesTexture.width / 12,
            spritesTexture.height / 8
        )

        walkDownAnimation = Animation(walkFrameDuration, com.badlogic.gdx.utils.Array<TextureRegion>(3).apply { addAll(region[0][6], region[0][7], region[0][8]) }, Animation.PlayMode.LOOP_PINGPONG)
        walkLeftAnimation = Animation(walkFrameDuration, com.badlogic.gdx.utils.Array<TextureRegion>(3).apply { addAll(region[1][6], region[1][7], region[1][8]) }, Animation.PlayMode.LOOP_PINGPONG)
        walkRightAnimation = Animation(walkFrameDuration, com.badlogic.gdx.utils.Array<TextureRegion>(3).apply { addAll(region[2][6], region[2][7], region[2][8]) }, Animation.PlayMode.LOOP_PINGPONG)
        walkUpAnimation = Animation(walkFrameDuration, com.badlogic.gdx.utils.Array<TextureRegion>(3).apply { addAll(region[3][6], region[3][7], region[3][8]) }, Animation.PlayMode.LOOP_PINGPONG)

        val slashRegion = TextureRegion.split(
            slashTexture,
            slashTexture.width / 5,
            slashTexture.height
        )
        slashAnimation = Animation(1 / 20f, com.badlogic.gdx.utils.Array<TextureRegion>(5).apply {
            addAll(slashRegion[0][0])
            addAll(slashRegion[0][1])
            addAll(slashRegion[0][2])
            addAll(slashRegion[0][3])
            addAll(slashRegion[0][4])
        }, Animation.PlayMode.NORMAL)
    }

    fun draw(batch: SpriteBatch, stateTime: Float, deltaTime: Float) {
        animationTime += deltaTime

        val textureRegion = if (walking) {
            val animation = when (direction) {
                Input.Keys.DOWN -> walkDownAnimation
                Input.Keys.LEFT -> walkLeftAnimation
                Input.Keys.RIGHT -> walkRightAnimation
                Input.Keys.UP -> walkUpAnimation
                else -> throw IllegalStateException()
            }

            animation.frameDuration = walkFrameDuration * (1 / speed)
            animation.getKeyFrame(stateTime, true)
        } else {
            when (direction) {
                Input.Keys.DOWN -> region[0][7]
                Input.Keys.LEFT -> region[1][7]
                Input.Keys.RIGHT -> region[2][7]
                Input.Keys.UP -> region[3][7]
                else -> throw IllegalStateException()
            }
        }
        batch.draw(textureRegion, pos.x, pos.y)

        if (attacking) {
            batch.draw(slashAnimation.getKeyFrame(animationTime, true), pos.x, pos.y, 64f, 64f)
            if (slashAnimation.getKeyFrameIndex(animationTime) == slashAnimation.keyFrames.size - 1) {
                attacking = false
                animationTime = 0f
            }
        }
    }

    fun afterDraw() {
        val destination = movementDistance * speed * Gdx.graphics.deltaTime
        var notifyMoved = false

        when {
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> {
                if (listener.canMove(getRect(pos.x + destination, pos.y))) {
                    pos.x += destination
                    notifyMoved = true
                } else if (direction != Input.Keys.RIGHT) {
                    notifyMoved = true
                }

                direction = Input.Keys.RIGHT
                walking = true
            }

            Gdx.input.isKeyPressed(Input.Keys.LEFT) -> {
                if (listener.canMove(getRect(pos.x - destination, pos.y))) {
                    pos.x -= destination
                    notifyMoved = true
                } else if (direction != Input.Keys.LEFT) {
                    notifyMoved = true
                }

                direction = Input.Keys.LEFT
                walking = true
            }

            Gdx.input.isKeyPressed(Input.Keys.UP) -> {
                if (listener.canMove(getRect(pos.x, pos.y + destination))) {
                    pos.y += destination
                    notifyMoved = true
                } else if (direction != Input.Keys.UP) {
                    notifyMoved = true
                }

                direction = Input.Keys.UP
                walking = true
            }

            Gdx.input.isKeyPressed(Input.Keys.DOWN) -> {
                if (listener.canMove(getRect(pos.x, pos.y - destination))) {
                    pos.y -= destination
                    notifyMoved = true
                } else if (direction != Input.Keys.DOWN) {
                    notifyMoved = true
                }

                direction = Input.Keys.DOWN
                walking = true
            }

            Gdx.input.isKeyPressed(Input.Keys.A) -> {
                if (!listener.interceptAction()) {
                    attacking = true
                }
            }

            else -> walking = false
        }

        running = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
        speed = if (running) 2f else 1f

        if (notifyMoved) {
            listener.moved(rect)
        }
    }

    fun dispose() {
        spritesTexture.dispose()
        slashTexture.dispose()
    }

    companion object {
        const val ID = "player"
    }
}