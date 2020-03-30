package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
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

	// https://github.com/czyzby/gdx-skins/tree/master/biological-attack
	public Skin skin;

	public Sounds sounds;
	// firebase realtime db we will connect to for online play
	public final String db = "https://nukethemall-d8ac7.firebaseio.com/";

	@Override
	public void create () {
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
		// cba to do asset manager
		skin = new Skin(Gdx.files.internal("skin/biological-attack-ui.json"));
		skin.getFont("font").getData().markupEnabled = true;
		skin.getFont("title").getData().markupEnabled = true;

		sounds = new Sounds();
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
    	if (screen != null) screen.hide();
    	if (screen != null) screen.dispose();
		batch.dispose();
		white.getTexture().dispose();
		VisUI.dispose();
		skin.dispose();
	}

	public static class Sounds {
		public final Sound rocket;
		public final Sound boom;
		public final Sound launch;
		public final Sound begin;
		public final Sound lost;
		public final Sound won;
		public final Sound scream;
		public final Sound cooldown;
		public float volume = 1;

		public Sounds () {
			rocket = newSound("sounds/rocket.wav", 1f);
			boom = newSound("sounds/boom.wav", 1f);
			launch = newSound("sounds/launch.wav", 1f);
			begin = newSound("sounds/begin.wav", 1f);
			lost = newSound("sounds/you_lost.wav", 1f);
			won = newSound("sounds/you_won.wav", 1f);
			scream = newSound("sounds/scream.wav", 1f);
			cooldown = newSound("sounds/cooldown.wav", 1f);
		}

		private Sound newSound (String path, float volume) {
			return new Sound(this, Gdx.audio.newSound(Gdx.files.internal(path)), volume);
		}

		public static class Sound {
			private final Sounds sounds;
			private final com.badlogic.gdx.audio.Sound sound;
			private float volume;

			public Sound (Sounds sounds, com.badlogic.gdx.audio.Sound sound, float volume) {
				this.sounds = sounds;
				this.sound = sound;
				this.volume = volume;
			}

			public void play () {
				float v = sounds.volume * volume;
				if (v > 0) {
					sound.play(v);
				}
			}
		}
	}
}
