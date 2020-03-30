package io.piotrjastrzebski.gdxjam.nta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
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
import io.piotrjastrzebski.gdxjam.nta.utils.Online;
import io.piotrjastrzebski.gdxjam.nta.utils.RNGUtils;
import io.piotrjastrzebski.gdxjam.nta.utils.events.Events;
import io.piotrjastrzebski.gdxjam.nta.utils.events.ExplodeEvent;
import io.piotrjastrzebski.gdxjam.nta.utils.events.LaunchNukeEvent;
import io.piotrjastrzebski.gdxjam.nta.utils.events.PlayerLostCity;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

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
    Player p1;
    Player p2;

    // raw map from wiki https://en.wikipedia.org/wiki/World_map#/media/File:Winkel_triple_projection_SW.jpg
    Texture worldMap;

    Online online;

    public GameScreen (NukeGame game) {
        super(game);
        gameStage = new Stage(game.gameViewport, game.batch);
        online = new Online(game.db);
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
        neutral = new Player(0, "neutral", false, players, Color.LIGHT_GRAY);
        p1 = null;
        p2 = null;

        // make sure all is good :v
        online.leave();
        online.cancelHost();
        online.cancelHosts();

        if (false) {
            worldMap = new Texture("world_map.jpg");
            Image image = new Image(worldMap);
            image.setFillParent(true);
            image.setScaling(Scaling.fit);
            gameStage.addActor(image);
        }

        seed = System.currentTimeMillis();
        for (ContinentData cd : Continents.continents()) {
            Continent continent = new Continent(game, ++IDS);
            continent.init(cd);
            continent.owner(neutral);
            gameStage.addActor(continent);
            entities.add(continent);
            continents.add(continent);
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
                    showOnlineDialog();
                }
            });
            buttons.add(button).expandX().left().row();
        }
        dialog.show(uiStage);
    }

    private void startLocalVsBot () {
        p1 = new Player(1, "player", true, players, Color.GREEN);
        p2 = new Player(2, "enemy", false, players, Color.RED);
        p2.bot();
        spawnPlayers(System.currentTimeMillis());
    }

    private void startLocalVsLocal () {
        p1 = new Player(1, "player", true, players, Color.GREEN);
        p2 = new Player(2, "enemy", true, players, Color.RED);
        spawnPlayers(System.currentTimeMillis());
    }

    private void startLocalVsRemove (String host, String other, long seed) {
        if (online.playerId().equals(host)) {
            p1 = new Player(1, host, true, players, Color.GREEN);
            p2 = new Player(2, other, false, players, Color.RED);
        } else {
            p1 = new Player(1, host, false, players, Color.RED);
            p2 = new Player(2, other, true, players, Color.GREEN);
        }
        spawnPlayers(seed);
    }

    private void spawnPlayers (long seed) {
        // lets hope its stable enough between machines :D
        RNGUtils.init(seed);
        for (Continent continent : continents) {
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

        Silo.IDS = 0;
        game.sounds.begin.play();
        // random initial positions
        // more continents per player?

        Array<Continent> shuffled = RNGUtils.shuffle(new Array<>(continents));
        if (shuffled.size == 0) return;
        // there should be a few matching continents
        while (true) {
            Continent continent = shuffled.pop();
            if (continent.cities().size == maxCities) {
                overtakeContinent(continent, p1);
                break;
            }
        }
        while (true) {
            Continent continent = shuffled.pop();
            if (continent.cities().size == maxCities) {
                overtakeContinent(continent, p2);
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
            Silo silo = new Silo(game);
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

    private void launchNuke (Silo silo, float tx, float ty) {
        Nuke nuke = new Nuke(game, ++IDS);
        nuke.owner(silo.owner());
        Vector2 sc = silo.sc();
        nuke.setPosition(sc.x, sc.y, center);
        nuke.target(tx, ty);
        nukes.add(nuke);
        gameStage.addActor(nuke);

        if (silo.owner().isPlayerControlled()) {
            online.launchNuke(silo.id, tx, ty);
        }
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
            Button button = new ImageTextButton("RESTART", skin, "radiation");
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
        if (p1 != null) p1.update(delta);
        if (p2 != null) p2.update(delta);
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
        online.dispose();
        gameStage.dispose();
        if (worldMap != null) worldMap.dispose();
    }

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.LAUNCH_NUKE: {
            LaunchNukeEvent ln = (LaunchNukeEvent)msg.extraInfo;
            launchNuke(ln.silo, ln.tx, ln.ty);
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

    private void showOnlineDialog () {
        Skin skin = game.skin;
        Dialog dialog = new Dialog("NUKE THEM ALL", skin);
        dialog.getTitleTable().pad(16);
        dialog.setModal(true);
        dialog.setMovable(false);

        Table content = dialog.getContentTable().pad(16);
        content.add(new Label("Lets try this online stuff!", skin)).left().row();
        content.add(new Label("You can host or join hosted game", skin)).left().row();
        content.add(new Label("(Start two clients to test)", skin)).left().row();

        Table buttons = new Table();
        content.add(buttons).growX().row();
        Table table = new Table();
        content.add(table).grow().row();


        table.add(new Label("Press HOST or JOIN to start!", skin));

        ButtonGroup<Button> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        group.setMaxCheckCount(1);
        {
            Button button = new ImageTextButton("HOST", skin, "toggle");
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    host(dialog, table, button.isChecked());
                    pack(dialog);
                }
            });
            buttons.add(button).expandX().uniformX().left();
            group.add(button);
        }
        {
            Button button = new ImageTextButton("JOIN", skin, "toggle");
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    join(dialog, table, button.isChecked());
                    pack(dialog);
                }
            });
            buttons.add(button).expandX().uniformX().right().row();
            group.add(button);
        }
        {
            Button button = new ImageTextButton("Cancel", skin, "warning");
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    createStartUI();
                }
            });
            buttons.add(button).expandX().uniformX().right().row();
            content.add(button);
        }
        dialog.show(uiStage);
    }

    private void pack (Dialog dialog) {
        dialog.pack();
        dialog.setPosition(Math.round((uiStage.getWidth() - dialog.getWidth()) / 2), Math.round((uiStage.getHeight() -  dialog.getHeight()) / 2));
    }

    void host (Dialog dialog, Table container, boolean enabled) {
        if (!enabled) {
            log.debug("Cancel hosting");
            online.cancelHost();
            container.clear();
            return;
        }
        log.debug("Start hosting");
        container.clear();
        online.host(gameListener());
        Skin skin = game.skin;
        container.add(new Label("You are now hosting with id:", skin)).row();
        container.add(new Label(online.playerId(), skin)).row();;
        container.add(new Label("When other player joins the game will start!", skin)).row();
    }

    void join (Dialog dialog, Table container, boolean enabled) {
        if (!enabled) {
            log.debug("Cancel joining");
            container.clear();
            online.cancelHosts();
            return;
        }
        log.debug("Start joining");
        container.clear();
        Skin skin = game.skin;
        container.add(new Label("Pick a host and join a game!", skin)).row();
        container.add(new Label("(auto refresh)", skin)).row();
        if (false) {
            Button button = new ImageTextButton("Refresh", skin, "poison");
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    join(dialog, container, true);
                }
            });
            container.add(button).row();
        }
        Table hostsTable = new Table();
        hostsTable.top().left();
        hostsTable.add(new Label("Loading...", skin));
        ScrollPane pane = new ScrollPane(hostsTable, skin);
        pane.setScrollingDisabled(true, false);
        container.add(pane).growX().height(250);

        online.hosts(hosts -> {
            hostsTable.clear();
            if (hosts.size == 0) {
                hostsTable.add(new Label("No hosts :(", skin));
                return;
            }

            for (Online.Player host : hosts) {
                hostsTable.add(new Label(host.id, skin)).padRight(8);
                Button button = new ImageTextButton("JOIN", skin, "fire");
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed (ChangeEvent event, Actor actor) {
                        joinGame(dialog, container, host);
                    }
                });
                hostsTable.add(button).padBottom(8).row();
            }
        });
    }

    private void joinGame (Dialog dialog, Table container, Online.Player host) {
        log.debug("Joining host {}", host);
        online.join(host.id, gameListener());

        Skin skin = game.skin;
        Dialog joinDialog = new Dialog("Joining...", skin);
        joinDialog.getTitleTable().pad(16);
        joinDialog.setModal(true);
        joinDialog.setMovable(false);

        Table content = joinDialog.getContentTable().pad(16);
        content.add(new Label("Please wait", skin)).left().row();
        joinDialog.show(uiStage);
    }

    private Online.GameListener gameListener () {
        return new Online.GameListener() {
            @Override
            public void start (String host, String other, long seed) {
                log.info("Start {}, {}", host, other);
                uiStage.clear();
                startLocalVsRemove(host, other, seed);
            }

            @Override
            public void nuke (String player, int siloId, float x, float y) {
                log.info("Nuke {}, {},{}", player, x, y);
                if (p1 != null && p1.name.equals(player) && !p1.isPlayerControlled()) {
                    Silo silo = p1.silo(siloId);
                    if (silo != null) {
                        launchNuke(silo, x, y);
                    } else {
                        log.warn("{} silo#{} not found", player, siloId);
                    }
                } else if (p2 != null && p2.name.equals(player) && !p2.isPlayerControlled()) {
                    Silo silo = p2.silo(siloId);
                    if (silo != null) {
                        launchNuke(silo, x, y);
                    } else {
                        log.warn("{} silo#{} not found", player, siloId);
                    }
                }
            }

            @Override
            public void end () {
                // someone left the game
                log.info("End");
                restart();
                createStartUI();
            }

            @Override
            public void fail () {
                log.warn("Failed!");
                failDialog("Failed to join game!");
            }
        };
    }

    private void failDialog (String text) {

        Skin skin = game.skin;
        Dialog dialog = new Dialog("We have a problem...", skin);
        dialog.getTitleTable().pad(16);
        dialog.setModal(true);
        dialog.setMovable(false);

        Table content = dialog.getContentTable().pad(16);
        content.add(new Label(text, skin)).left().row();

        {
            Button button = new ImageTextButton("OK", skin, "toggle");
            button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    restart();
                    createStartUI();
                }
            });
            content.add(button);
        }
        dialog.show(uiStage);
    }
}
