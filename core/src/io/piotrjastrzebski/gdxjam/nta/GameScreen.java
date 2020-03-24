package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.gdxjam.nta.game.*;

public class GameScreen extends BaseScreen {
    protected static final String TAG = GameScreen.class.getSimpleName();

    public static int IDS = 0;

    Stage gs;
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
        gs = new Stage(game.gameViewport, game.batch);

        neutral = new Player(0, "neutral");
        player = new Player(1, "player");
        enemy = new Player(2, "enemy");

        // TODO load map from somewhere?
        createContinents();
    }

    @Override
    public void show () {
        super.show();
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, gs, this));
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
        Continent continent = new Continent(++IDS);
        continent.init(cx, cy, radius);
        continent.owner(neutral);
        gs.addActor(continent);
        entities.add(continent);
        continents.add(continent);

        // more cities for bigger continents?
        Array<City> cc = new Array<>();
        for (int i = 0; i < 3; i++) {
            City city = new City(++IDS);
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
            for (Continent continent : continents) {
                continent.remove();
            }
            continents.clear();
            createContinents();
        }

        entities.sort();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        gs.act(delta);
        gs.draw();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose () {
        super.dispose();
        gs.dispose();
    }
}
