package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import io.piotrjastrzebski.gdxjam.nta.game.*;
import io.piotrjastrzebski.gdxjam.nta.utils.Continents;
import io.piotrjastrzebski.gdxjam.nta.utils.Continents.ContinentData;
import io.piotrjastrzebski.gdxjam.nta.utils.Events;
import io.piotrjastrzebski.gdxjam.nta.utils.command.Explode;
import io.piotrjastrzebski.gdxjam.nta.utils.command.LaunchNuke;
import io.piotrjastrzebski.gdxjam.nta.utils.command.PlayerLost;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

import static com.badlogic.gdx.utils.Align.center;

@Slf4j
public class GameScreen extends BaseScreen implements Telegraph {
    public static int IDS = 0;

    Stage gameStage;
    Array<Entity> entities;
    // immobile stuff
    Array<Continent> continents;
    Array<City> cities;
    Array<Silo> silos;
    // mobile stuff
    Array<Submarine> submarines;
    Array<Nuke> nukes;

    Array<Player> players;
    Player neutral;
    Player local;
    Player remote;

    // raw map from wiki https://en.wikipedia.org/wiki/World_map#/media/File:Winkel_triple_projection_SW.jpg
    Texture worldMap;

    public GameScreen (NukeGame game) {
        super(game);
        gameStage = new Stage(game.gameViewport, game.batch);

        restart();
    }

    private void restart () {
        gameStage.clear();

        entities = new Array<>();
        // immobile stuff
        continents = new Array<>();
        cities = new Array<>();
        silos = new Array<>();
        // mobile stuff
        submarines = new Array<>();
        nukes = new Array<>();

        players = new Array<>();
        neutral = new Player(0, "neutral", false, players);

        if (false) {
            worldMap = new Texture("world_map.jpg");
            Image image = new Image(worldMap);
            image.setFillParent(true);
            image.setScaling(Scaling.fit);
            gameStage.addActor(image);
        }

        createContinents();

        createStartUI();
    }

    private void createStartUI () {
        uiStage.clear();
        Skin skin = game.skin;
        Dialog dialog = new Dialog("NUKE THEM ALL", skin);
        dialog.getTitleTable().pad(16);
        dialog.setModal(true);
        dialog.setMovable(false);

        Table content = dialog.getContentTable().pad(16);
        content.add(new Label("Wipe out enemy cities with nukes to win!", skin)).left().row();
        content.add(new Label("Launch nuke by dragging from owned silo", skin)).left().row();
        content.add(new Label("YOU are [GREEN]green[]", skin)).left().row();
        content.add(new Label("ENEMY is [RED]red[]", skin)).left().row();

        Table buttons = new Table();
        content.add(buttons).grow();
        {
            Button button = new ImageTextButton("Player VS Bot", skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    dialog.hide();
                    startLocalVsBot();
                }
            });
            buttons.add(button).expandX().left().row();
        }
        {
            Button button = new ImageTextButton("Player VS Player Local", skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    dialog.hide();
                    startLocalVsLocal();
                }
            });
            buttons.add(button).expandX().left().row();
        }
        {
            Button button = new ImageTextButton("Player VS Player Online", skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    dialog.hide();
                    startLocalVsRemove();
                }
            });
            buttons.add(button).expandX().left().row();
        }
        dialog.show(uiStage);
    }

    private void startLocalVsBot () {
        local = new Player(1, "player", true, players);
        remote = new Player(2, "enemy", false, players);
        remote.bot();
        spawnPlayers();
    }

    private void startLocalVsLocal () {
        local = new Player(1, "player", true, players);
        remote = new Player(2, "enemy", true, players);
        spawnPlayers();
    }

    private void startLocalVsRemove () {
        local = new Player(1, "player", true, players);
        remote = new Player(2, "enemy", false, players);
        spawnPlayers();
    }

    private void spawnPlayers () {
        // random initial positions
        // more continents per player?
        Array<Continent> copy = new Array<>(continents);
        copy.shuffle();
        if (copy.size == 0) return;
        // there should be a few matching continents
        while (true) {
            Continent continent = copy.pop();
            if (continent.cities().size == 1) {
                overtakeContinent(continent, local);
                break;
            }
        }
        while (true) {
            Continent continent = copy.pop();
            if (continent.cities().size == 1) {
                overtakeContinent(continent, remote);
                break;
            }
        }
    }

    @Override
    public void show () {
        super.show();
        Events.register(this, Events.LAUNCH_NUKE, Events.EXPLODE, Events.PLAYER_LOST);
        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, gameStage, this));
    }

    private void createContinents () {
        // eventually we want something more sensible, rng maybe?
        // via seed?
        for (ContinentData continent : Continents.continents()) {
            continent(continent);
        }
    }

    private void overtakeContinent (Continent continent, Player player) {
        continent.owner(player);

        Rectangle bounds = continent.rectBounds();
        // city count roughly based on area
        int siloCount = Math.min(2 + Math.round(bounds.area()/20), 4);
        Array<Silo> silos = new Array<>();
        Array<City> cities = continent.cities();
        for (int i = 0; i < siloCount; i++) {
            Silo silo = new Silo(game, ++IDS);
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
            silo.updateBounds();
            silos.add(silo);
        }
        this.silos.addAll(silos);
    }

    private void continent (ContinentData cd) {
        Continent continent = new Continent(game, ++IDS);
        continent.init(cd);
        continent.owner(neutral);
        gameStage.addActor(continent);
        entities.add(continent);
        continents.add(continent);

        Rectangle bounds = continent.rectBounds();
        // city count roughly based on area
        int cityCount = 1;// Math.min(2 + Math.round(bounds.area()/20), 4);
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
            city.updateBounds();
            cities.add(city);
        }
        this.cities.addAll(cities);

    }

    private void launchNuke (Player player, float sx, float sy, float tx, float ty) {
        Nuke nuke = new Nuke(game, ++IDS);
        nuke.owner(player);
        nuke.setPosition(sx, sy, center);
        nuke.target(tx, ty);
        nukes.add(nuke);
        gameStage.addActor(nuke);

    }

    private void updateNukes () {
        // could be more efficient but whatever
        for (int i = 0; i < nukes.size; i++) {
            for (int j = 0; j < nukes.size; j++) {
                Nuke n1 = nukes.get(i);
                Nuke n2 = nukes.get(j);
                if (n1 == n2) continue;;
                if (n1.isDestroyed() || n2.isDestroyed()) continue;
                // dont nuke self
                if (n1.owner() == n2.owner()) continue;
                if (n1.bounds().overlaps(n2.bounds())) {
                    n1.explode();
                    n2.explode();
                }
            }
        }
    }

    private void explode (float cx, float cy, float radius, float falloffRadius, float damage) {
        Explosion explosion = new Explosion(game, radius,  falloffRadius);
        explosion.setPosition(cx, cy, Align.center);
        gameStage.addActor(explosion);

        Circle inner = new Circle(cx, cy, radius);
        Circle outer = new Circle(cx, cy, falloffRadius);

        {
            Iterator<City> it = cities.iterator();
            while (it.hasNext()) {
                City entity = it.next();
                if (!outer.overlaps(entity.bounds())) continue;
                if (inner.overlaps(entity.bounds())) {
                    if (entity.damage(damage)) {
                        it.remove();
                    }
                } else {
                    if (entity.damage(damage/2)) {
                        it.remove();
                    }
                }
            }
        }
        {
            Iterator<Silo> it = silos.iterator();
            while (it.hasNext()) {
                Silo entity = it.next();
                if (!outer.overlaps(entity.bounds())) continue;
                if (inner.overlaps(entity.bounds())) {
                    if (entity.damage(damage)) {
                        it.remove();
                    }
                } else {
                    if (entity.damage(damage/2)) {
                        it.remove();
                    }
                }
            }
        }
    }

    private void playerLost (Player player) {
        if (player == neutral) return;
        restart();

        uiStage.clear();
        Skin skin = game.skin;
        Dialog dialog = new Dialog("NUKE THEM ALL", skin);
        dialog.getTitleTable().pad(16);
        dialog.setModal(true);
        dialog.setMovable(false);

        Table content = dialog.getContentTable().pad(16);
        content.add(new Label("Congratulations!", skin)).left().row();
        if (player == remote) {
            content.add(new Label("Everyone lost, but you lost the least i guess?", skin)).left().row();
        } else {
            content.add(new Label("Everyone lost, but you lost extra hard i guess?", skin)).left().row();
        }

        Table buttons = new Table();
        content.add(buttons).grow();
        {
            Button button = new ImageTextButton("RESTART", skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    dialog.hide();
                    restart();
                }
            });
            buttons.add(button).expandX().left().row();
        }
        dialog.show(uiStage);
    }

    @Override
    public void update (float delta) {
        gameStage.act(delta);
        updateNukes();
        if (local != null) local.update(delta);
        if (remote != null) remote.update(delta);
        gameStage.draw();
        uiStage.act(delta);
        uiStage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            restart();
        }
    }

    @Override
    public void hide () {
        Events.unregister(this, Events.LAUNCH_NUKE, Events.EXPLODE, Events.PLAYER_LOST);
        super.hide();
    }

    @Override
    public void dispose () {
        super.dispose();
        gameStage.dispose();
        if (worldMap != null) worldMap.dispose();
    }

    public Player player () {
        return local;
    }

    public Player enemy () {
        return remote;
    }

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.LAUNCH_NUKE: {
            LaunchNuke ln = (LaunchNuke)msg.extraInfo;
            launchNuke(ln.player, ln.sx, ln.sy, ln.tx, ln.ty);
        } break;
        case Events.EXPLODE: {
            Explode e = (Explode)msg.extraInfo;
            explode(e.cx, e.cy, e.radius, e.falloffRadius, e.damage);
        } break;
        case Events.PLAYER_LOST: {
            PlayerLost pl = (PlayerLost)msg.extraInfo;
            playerLost(pl.player);
        } break;
        }
        return false;
    }
}
