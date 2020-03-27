package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BaseScreen implements Screen, InputProcessor {
    final NukeGame game;
    final OrthographicCamera gameCamera;
    final OrthographicCamera uiCamera;
    final PolygonSpriteBatch batch;
    final ShapeDrawer shapes;

    final Stage uiStage;

    public BaseScreen (NukeGame game) {
        this.game = game;
        batch = game.batch;
        shapes = game.shapes;
        gameCamera = (OrthographicCamera)game.gameViewport.getCamera();
        uiCamera = (OrthographicCamera)game.uiViewport.getCamera();

        uiStage = new Stage(game.uiViewport, batch);
    }

    @Override
    public void show () {
        // stage first so it takes priority, i guess?
        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, this));
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

    @Override
    public boolean keyDown (int keycode) {
        return false;
    }

    @Override
    public boolean keyUp (int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped (char character) {
        return false;
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved (int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled (int amount) {
        return false;
    }

    public NukeGame game () {
        return game;
    }
}
