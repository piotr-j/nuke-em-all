package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import io.piotrjastrzebski.gdxjam.nta.game.*;
import io.piotrjastrzebski.gdxjam.nta.game.Continents.ContinentData;
import lombok.extern.slf4j.Slf4j;

import static com.badlogic.gdx.utils.Align.center;

@Slf4j
public class GameScreen extends BaseScreen {
    public static int IDS = 0;

    Stage gameStage;
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

    // raw map from wiki https://en.wikipedia.org/wiki/World_map#/media/File:Winkel_triple_projection_SW.jpg
    Texture worldMap;

    public GameScreen (NukeGame game) {
        super(game);
        gameStage = new Stage(game.gameViewport, game.batch);

        neutral = new Player(0, "neutral");
        player = new Player(1, "player");
        enemy = new Player(2, "enemy");

        if (false) {
            worldMap = new Texture("world_map.jpg");
            Image image = new Image(worldMap);
            image.setFillParent(true);
            image.setScaling(Scaling.fit);
            gameStage.addActor(image);
        }

        createContinents();
    }

    @Override
    public void show () {
        super.show();

        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, gameStage, this));
    }

    private void createContinents () {
        // eventually we want something more sensible, rng maybe?
        // via seed?
        for (ContinentData continent : Continents.continents()) {
            continent(continent);
        }

        // random initial positions
        // more continents per player?
        Array<Continent> copy = new Array<>(continents);
        copy.shuffle();
        if (copy.size == 0) return;
        // there should be a few matching continents
        while (true) {
            Continent continent = copy.pop();
            if (continent.cities().size == 4) {
                overtakeContinent(continent, player);
                break;
            }
        }
        while (true) {
            Continent continent = copy.pop();
            if (continent.cities().size == 4) {
                overtakeContinent(continent, enemy);
                break;
            }
        }
    }

    private void overtakeContinent (Continent continent, Player player) {
        continent.owner(player);

        Rectangle bounds = continent.bounds();
        // city count roughly based on area
        int siloCount = Math.min(2 + Math.round(bounds.area()/20), 4);
        Array<Silo> silos = new Array<>();
        Array<City> cities = continent.cities();
        for (int i = 0; i < siloCount; i++) {
            Silo silo = new Silo(this, ++IDS);
            silo.owner(player);
            continent.addActor(silo);

            outer:
            for (int j = 0; j < 2000; j++) {
                silo.setPosition(
                    MathUtils.random(0, bounds.width - silo.getWidth()),
                    MathUtils.random(0, bounds.height - silo.getHeight())
                );
                float cx = silo.getX(center);
                float cy = silo.getY(center);
                // can be closer to cities
                for (City other : cities) {
                    float dst = Vector2.dst(other.getX(center), other.getY(center), cx, cy);
                    if (dst < silo.getWidth() * 2f) {
                        continue outer;
                    }
                }
                for (Silo other : silos) {
                    float dst = Vector2.dst(other.getX(center), other.getY(center), cx, cy);
                    if (dst < silo.getWidth() * 2.5f) {
                        continue outer;
                    }
                }

                if (continent.contains(cx, silo.getY())) {
                    break;
                }
            }
            silos.add(silo);
        }
    }

    private void continent (ContinentData cd) {
        Continent continent = new Continent(game, ++IDS);
        continent.init(cd);
        continent.owner(neutral);
        gameStage.addActor(continent);
        entities.add(continent);
        continents.add(continent);

        Rectangle bounds = continent.bounds();
        // city count roughly based on area
        int cityCount = Math.min(2 + Math.round(bounds.area()/20), 4);
        Array<City> cities = new Array<>();
        for (int i = 0; i < cityCount; i++) {
            City city = new City(game, ++IDS);
            continent.addActor(city);

            for (int j = 0; j < 1000; j++) {
                city.setPosition(
                    MathUtils.random(0, bounds.width - city.getWidth()),
                    MathUtils.random(0, bounds.height - city.getHeight())
                );
                boolean tooClose = false;
                for (City other : cities) {
                    float dst = Vector2.dst(other.getX(center), other.getY(center), city.getX(center), city.getY(center));
                    if (dst < other.getWidth() * 2) {
                        tooClose = true;
                    }
                }

                if (!tooClose && continent.contains(city.getX(center), city.getY())) {
                    break;
                }
            }
            cities.add(city);
        }

    }

    Vector2 v2 = new Vector2();
    public void launchNuke (Silo from, float tx, float ty) {
        from.localToStageCoordinates(v2.set(from.getWidth()/2, from.getHeight()/2));

        Nuke nuke = new Nuke(game, ++IDS);
        nuke.owner(from.owner());
        nuke.setPosition(v2.x, v2.y, center);
        nuke.target(tx, ty);

        gameStage.addActor(nuke);
    }

    @Override
    public void render (float delta) {
        super.render(delta);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        gameStage.act(delta);
        gameStage.draw();
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void dispose () {
        super.dispose();
        gameStage.dispose();
        if (worldMap != null) worldMap.dispose();
    }

    public Player player () {
        return player;
    }

    public Player enemy () {
        return enemy;
    }
}
