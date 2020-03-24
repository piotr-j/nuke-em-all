package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.gdxjam.nta.game.*;

import java.util.Iterator;

public class GameScreen extends BaseScreen {
    protected static final String TAG = GameScreen.class.getSimpleName();

    int entityIds = 0;

    Array<Entity> entities = new Array<>();
    // immobile stuff
    Array<Continent> continents = new Array<>();
    Array<City> cities = new Array<>();
    Array<Silo> silos = new Array<>();
    // mobile stuff
    Array<Submarine> submarines = new Array<>();
    Array<Nuke> nukes = new Array<>();

    final Player neutral;
    final Player player;
    final Player enemy;

    public GameScreen (NukeGame game) {
        super(game);

        neutral = new Player(0, "neutral");
        player = new Player(1, "player");
        enemy = new Player(2, "enemy");

        // TODO load map from somewhere?
        createContinents();
    }

    private void createContinents () {
        // eventually we want something more sensible, rng maybe?
        // via seed?
        // TODO rng player location
        continent(6, 15, 4.5f);
        continent(8, 5, 3.5f);


        continent(21, 14, 3.0f);
        continent(22, 6, 4.5f);

        continent(30, 15, 5.5f);

        continent(34, 4, 3f);

        // random initial positions
        Array<Continent> copy = new Array<>(continents);
        copy.shuffle();
        copy.pop().owner(player);
        copy.pop().owner(enemy);
    }

    private void continent (float cx, float cy, float radius) {
        Continent continent = new Continent(++entityIds);
        continent.init(cx, cy, radius);
        continent.owner(neutral);
        entities.add(continent);
        continents.add(continent);

        // more cities for bigger continents?
        Array<City> cc = new Array<>();
        for (int i = 0; i < 3; i++) {
            City city = new City(++entityIds);
            cc.add(city);
            entities.add(city);
            cities.add(city);
        }
        continent.city(cc);
    }

    @Override
    public void render (float delta) {
        super.render(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Gdx.app.log(TAG, "rebuild continents");
            entities.removeAll(continents, true);
            continents.clear();
            createContinents();
        }

        entities.sort();

        // need to fix the time step i guess
        for (Entity entity : entities) {
            entity.update(delta);
        }

        batch.setProjectionMatrix(gameCamera.combined);
        batch.enableBlending();
        batch.begin();
        for (Entity entity : entities) {
            entity.draw(batch);
        }
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(gameCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Entity entity : entities) {
            entity.drawDebug(shapes);
        }
        shapes.end();

        stage.act(delta);
        stage.draw();
    }

    Vector2 tp = new Vector2();
    Entity over = null;
    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        game.gameViewport.unproject(tp.set(screenX, screenY));
        over = entityAt(tp.x, tp.y);
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        game.gameViewport.unproject(tp.set(screenX, screenY));
        Entity over = entityAt(tp.x, tp.y);
        if (this.over == over && over != null) {
            over.click(tp.x, tp.y);
        }
        return super.touchUp(screenX, screenY, pointer, button);
    }

    private Entity entityAt (float x, float y) {
        for (Entity entity : entities) {
            if (entity.contains(x, y)) {
                return entity;
            }
        }
        return null;
    }
}
