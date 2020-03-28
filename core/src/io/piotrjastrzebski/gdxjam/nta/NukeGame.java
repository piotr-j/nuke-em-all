package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.gdxjam.nta.utils.Events;
import lombok.extern.slf4j.Slf4j;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Main goal is to make this multi-player via firebase database of sorts
 *
 * Make this super simple, one device 'hosts' the game, ie makes an entry with some rng key in the db
 * Other client can either enter it or look for all open games
 *
 * We need this to be sorta turn based, perhaps abilities with longish cooldowns?
 * There are limits how much we can spam the db, so got to think about that, maybe
 *
 * The them is thermonuclear war, would be nice to include the current amusing pandemic
 *
 * Web version would be great...
 *
 *
 * Few seconds delay on launch
 * longish flight time, 30s end to end?
 * overlap nukes to destroy
 * choose between nuking places and defending
 *
 */
@Slf4j
public class NukeGame extends Game {
	public static final float SCALE = 32;
	public static final float INV_SCALE = 1/SCALE;
	public static final float WIDTH = 1280 * INV_SCALE;
	public static final float HEIGHT = 720 * INV_SCALE;

	public ExtendViewport gameViewport;
	public ScreenViewport uiViewport;

	public PolygonSpriteBatch batch;
	public TextureRegion white;
	public ShapeDrawer shapes;
	public Preferences prefs;

	@Override
	public void create () {
		log.info("Created");
		batch = new PolygonSpriteBatch();
		{
			Pixmap pixmap = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
			pixmap.setColor(Color.WHITE);
			pixmap.drawPixel(1, 1);
			white = new TextureRegion(new Texture(pixmap), 1, 1, 1, 1);
		}
		shapes = new ShapeDrawer(batch, white);

		gameViewport = new ExtendViewport(WIDTH, HEIGHT);
		uiViewport = new ScreenViewport();

		prefs = Gdx.app.getPreferences("prefs.nta");

		VisUI.load();
		setScreen(new GameScreen(this));
	}

	@Override
	public void resize (int width, int height) {
		log.debug("resize {}, {}", width, height);
		gameViewport.update(width, height, true);
		uiViewport.update(width, height, true);
		super.resize(width, height);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.4f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		white.getTexture().dispose();
		VisUI.dispose();
	}
}
