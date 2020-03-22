package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class BaseScreen implements Screen {
    SpriteBatch batch;
    ShapeRenderer shapes;

    public BaseScreen (NukeGame game) {
        batch = game.batch;
        shapes = game.shapes;

    }

    @Override
    public void show () {

    }

    @Override
    public void render (float delta) {

    }

    @Override
    public void resize (int width, int height) {
        // handled by game
    }

    @Override
    public void pause () {

    }

    @Override
    public void resume () {

    }

    @Override
    public void hide () {

    }

    @Override
    public void dispose () {

    }
}
