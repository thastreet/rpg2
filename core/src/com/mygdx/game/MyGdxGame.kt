package com.mygdx.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils


class MyGdxGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var camera: OrthographicCamera
    private var stateTime = 0f

    private fun canMove(id: String, rect: Rectangle) =
        collisions
            .filterKeys { it != id }
            .values.none { it.overlaps(rect) }

    private val collisions = mutableMapOf<String, Rectangle>()
    private val player = Player(
        canMove = { rect -> canMove(Player.ID, rect) },
        moved = { rect -> collisions[Player.ID] = rect }
    )

    private lateinit var npcTexture: Texture
    private lateinit var npcTextureRegion: TextureRegion
    private val npcPos = Vector2(100f, 100f)

    override fun create() {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        camera = OrthographicCamera(width, height)

        val viewportWidth = 400f
        camera.setToOrtho(false, viewportWidth, viewportWidth * height / width)
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()

        player.create()

        npcTexture = Texture(Gdx.files.internal("sprites.png"))
        npcTextureRegion = TextureRegion.split(
            npcTexture,
            npcTexture.width / 12,
            npcTexture.height / 8
        )[0][10]

        collisions["npc"] = Rectangle(npcPos.x + 8, npcPos.y, npcTexture.width / 12f - 2 * 8, 16f)
        collisions[Player.ID] = player.rect
    }

    override fun render() {
        ScreenUtils.clear(1f, 0f, 0f, 1f)

        stateTime += Gdx.graphics.deltaTime

        camera.update()

        batch.setProjectionMatrix(camera.combined)
        shapeRenderer.projectionMatrix = camera.combined

        batch.begin()

        val playerRect = player.rect

        if (npcPos.y > playerRect.y) {
            batch.draw(npcTextureRegion, npcPos.x, npcPos.y)
        }

        player.draw(batch, stateTime)

        if (npcPos.y <= playerRect.y) {
            batch.draw(npcTextureRegion, npcPos.x, npcPos.y)
        }

        batch.end()

        if (DEBUG_SHAPES) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            collisions.values.forEach {
                shapeRenderer.rect(it.x, it.y, it.width, it.height)
            }
            shapeRenderer.end()
        }

        player.afterDraw()
    }

    override fun dispose() {
        batch.dispose()
        player.dispose()
        npcTexture.dispose()
        shapeRenderer.dispose()
    }

    companion object {
        private const val DEBUG_SHAPES = false
    }
}
