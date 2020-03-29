package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import io.piotrjastrzebski.gdxjam.nta.game.*;
import io.piotrjastrzebski.gdxjam.nta.utils.Continents;
import io.piotrjastrzebski.gdxjam.nta.utils.Continents.ContinentData;
import io.piotrjastrzebski.gdxjam.nta.utils.FireBaseFunctions;
import io.piotrjastrzebski.gdxjam.nta.utils.RNGUtils;
import io.piotrjastrzebski.gdxjam.nta.utils.events.Events;
import io.piotrjastrzebski.gdxjam.nta.utils.events.ExplodeEvent;
import io.piotrjastrzebski.gdxjam.nta.utils.events.LaunchNukeEvent;
import io.piotrjastrzebski.gdxjam.nta.utils.events.PlayerLostCity;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Random;

import static com.badlogic.gdx.utils.Align.center;

@Slf4j
public class GameScreen extends BaseScreen implements Telegraph {
    public static int IDS = 0;
    final int maxCities = 4;

    Stage gameStage;
    long seed;
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
        createStartUI();
    }

    private void restart () {
        IDS = 0;
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
        local = null;
        remote = null;

        if (false) {
            worldMap = new Texture("world_map.jpg");
            Image image = new Image(worldMap);
            image.setFillParent(true);
            image.setScaling(Scaling.fit);
            gameStage.addActor(image);
        }

        seed = System.currentTimeMillis();
        seed = 1248712498L;
        // lets hope its stable enough between machines :D
        RNGUtils.init(seed);

        // eventually we want something more sensible, rng maybe?
        // via seed?
        for (ContinentData cd : Continents.continents()) {
            Continent continent = new Continent(game, ++IDS);
            continent.init(cd);
            continent.owner(neutral);
            gameStage.addActor(continent);
            entities.add(continent);
            continents.add(continent);

            Rectangle bounds = continent.rectBounds();
            // city count roughly based on area
            int cityCount = Math.min(2 + Math.round(bounds.area()/20), maxCities);
            Array<City> cities = new Array<>();
            for (int i = 0; i < cityCount; i++) {
                City city = new City(game, ++IDS);
                continent.addActor(city);

                for (int j = 0; j < 1000; j++) {
                    city.setPosition(
                        RNGUtils.random(0, bounds.width - city.getWidth()),
                        RNGUtils.random(0, bounds.height - city.getHeight())
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
        game.sounds.begin.play();
        // random initial positions
        // more continents per player?

        Array<Continent> shuffled = RNGUtils.shuffle(new Array<>(continents));
        if (shuffled.size == 0) return;
        // there should be a few matching continents
        while (true) {
            Continent continent = shuffled.pop();
            if (continent.cities().size == maxCities) {
                overtakeContinent(continent, local);
                break;
            }
        }
        while (true) {
            Continent continent = shuffled.pop();
            if (continent.cities().size == maxCities) {
                overtakeContinent(continent, remote);
                break;
            }
        }
    }

    private void overtakeContinent (Continent continent, Player player) {
        continent.owner(player);

        Rectangle bounds = continent.rectBounds();
        Array<Silo> silos = new Array<>();
        Array<City> cities = continent.cities();
        for (int i = 0; i < cities.size; i++) {
            Silo silo = new Silo(game, ++IDS);
            silo.owner(player);
            continent.addActor(silo);

            outer:
            for (int j = 0; j < 2000; j++) {
                silo.setPosition(
                    RNGUtils.random(0, bounds.width - silo.getWidth()),
                    RNGUtils.random(0, bounds.height - silo.getHeight())
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

    private void launchNuke (Silo silo, float sx, float sy, float tx, float ty) {
        Nuke nuke = new Nuke(game, ++IDS);
        nuke.owner(silo.owner());
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

        game.sounds.boom.play();

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

        uiStage.clear();
        Skin skin = game.skin;
        Dialog dialog = new Dialog("NUKE THEM ALL", skin);
        dialog.getTitleTable().pad(16);
        dialog.setModal(true);
        dialog.setMovable(false);

        Table content = dialog.getContentTable().pad(16);
        content.add(new Label("Congratulations!", skin)).left().row();
        // we assume two players
        if (!player.isPlayerControlled()) {
            content.add(new Label("Everyone lost, but you lost the least i guess?", skin)).left().row();
            dialog.addAction(Actions.delay(.5f, Actions.run(() -> game.sounds.won.play())));
        } else {
            content.add(new Label("Everyone lost, but you lost extra hard i guess?", skin)).left().row();
            dialog.addAction(Actions.delay(.5f, Actions.run(() -> game.sounds.lost.play())));
        }

        Table buttons = new Table();
        content.add(buttons).grow();
        {
            Button button = new ImageTextButton("RESTART", skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    dialog.hide();
                    createStartUI();
                }
            });
            buttons.add(button).expandX().row();
        }
        dialog.show(uiStage);

        restart();
    }

    @Override
    public void show () {
        super.show();
        Events.register(this, Events.LAUNCH_NUKE, Events.EXPLODE, Events.PLAYER_LOST_CITy);
        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, gameStage, this));

        restart();
        createStartUI();
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
    }

    @Override
    public void hide () {
        Events.unregister(this, Events.LAUNCH_NUKE, Events.EXPLODE, Events.PLAYER_LOST_CITy);
        super.hide();
    }

    @Override
    public void dispose () {
        super.dispose();
        gameStage.dispose();
        if (worldMap != null) worldMap.dispose();
    }

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.LAUNCH_NUKE: {
            LaunchNukeEvent ln = (LaunchNukeEvent)msg.extraInfo;
            launchNuke(ln.silo, ln.sx, ln.sy, ln.tx, ln.ty);
        } break;
        case Events.EXPLODE: {
            ExplodeEvent e = (ExplodeEvent)msg.extraInfo;
            explode(e.cx, e.cy, e.radius, e.falloffRadius, e.damage);
        } break;
        case Events.PLAYER_LOST_CITy: {
            PlayerLostCity pl = (PlayerLostCity)msg.extraInfo;
            if (!pl.player.hasCities()) {
                playerLost(pl.player);
            }
        } break;
        }
        return false;
    }
}
