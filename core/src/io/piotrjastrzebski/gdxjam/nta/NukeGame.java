package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

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
 */
public class NukeGame extends Game {
	// portrait i guess, for mobile
	public static final float SCALE = 32;
	public static final float INV_SCALE = 1/SCALE;
	public static final float WIDTH = 720 * INV_SCALE;
	public static final float HEIGHT = 1280 * INV_SCALE;

	OrthographicCamera camera;
	ExtendViewport viewport;

	SpriteBatch batch;
	ShapeRenderer shapes;
	Preferences prefs;

	@Override
	public void create () {
		batch = new SpriteBatch();
		shapes = new ShapeRenderer();
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(WIDTH, HEIGHT, camera);
		prefs = Gdx.app.getPreferences("prefs.nta");
		prefs.putString("hello", "there");
		prefs.flush();

		setScreen(new MainScreen(this));
	}

	@Override
	public void resize (int width, int height) {
		viewport.update(width, height, true);
		super.resize(width, height);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		shapes.setProjectionMatrix(camera.combined);
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		shapes.dispose();
	}
}
