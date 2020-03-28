package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.gdxjam.nta.utils.Events;
import io.piotrjastrzebski.gdxjam.nta.utils.command.PlayerLost;

/**
 * Player in a game
 */
public class Player {
    static Color[] tints = new Color[] {
        Color.WHITE, // neutral
        Color.GREEN, // p1
        Color.RED, // p2
        // more?
    };

    public final int id;
    public final String name;
    public final boolean playerControlled;
    // tint added to owned stuff
    public final Color tint = new Color(Color.WHITE);

    Array<Continent> continents = new Array<>();

    Array<Silo> silos = new Array<>();

    Array<City> cities = new Array<>();

    boolean isBot = false;

    Array<Player> players;

    public Player (int id, String name, boolean local, Array<Player> players) {
        this.id = id;
        this.name = name;
        this.playerControlled = local;
        this.players = players;
        this.tint.set(tints[id % tints.length]);
        players.add(this);
    }

    public void bot () {
        isBot = true;
    }

    float ticker;
    public void update (float delta) {
        if (!isBot) return;
        ticker += delta;
        if (ticker <= 1f) return;
        ticker -= 1;

        Array<Entity> enemyTargets = new Array<>();
        Array<Entity> neutralTargets = new Array<>();
        for (Player player : players) {
            if (player == this) continue;
            for (Silo silo : player.silos) {
                if (player.isPlayerControlled()) {
                    enemyTargets.add(silo);
                } else {
                    neutralTargets.add(silo);
                }
            }
            for (City city : player.cities) {
                if (player.isPlayerControlled()) {
                    enemyTargets.add(city);
                } else {
                    neutralTargets.add(city);
                }
            }
        }
        enemyTargets.shuffle();
        neutralTargets.shuffle();


        for (Silo silo : silos) {
            if (!(silo.canLaunch() && MathUtils.random() > .9f)) continue;
            // so its not always perfectly on target
            float ox =  MathUtils.random(-.5f, .5f);
            float oy =  MathUtils.random(-.5f, .5f);
            if (enemyTargets.size > 0 && MathUtils.random() > .25f) {
                Entity target = enemyTargets.pop();
                Vector2 sc = target.sc();
                silo.launch(sc.x + ox, sc.y + oy);
            } else if (neutralTargets.size > 0) {
                Entity target = neutralTargets.pop();
                Vector2 sc = target.sc();
                silo.launch(sc.x + ox, sc.y + oy);
            }

        }
    }

    public boolean isPlayerControlled () {
        return playerControlled;
    }

    public void add (Entity entity) {
        if (entity instanceof City) {
            City city = (City)entity;
            if (!cities.contains(city, true)) cities.add(city);
        } else if (entity instanceof Silo) {
            Silo silo = (Silo)entity;
            if (!silos.contains(silo, true)) silos.add(silo);
        } else if (entity instanceof Continent) {
            Continent continent = (Continent)entity;
            if (!continents.contains(continent, true)) continents.add(continent);
        }
    }

    public void remove (Entity entity) {
        if (entity instanceof City) {
            City city = (City)entity;
            cities.removeValue(city, true);
            if (cities.size == 0) {
                Events.sendDelayed(1/20f, Events.PLAYER_LOST, new PlayerLost(this));
            }
        } else if (entity instanceof Silo) {
            Silo silo = (Silo)entity;
            silos.removeValue(silo, true);
        } else if (entity instanceof Continent) {
            Continent continent = (Continent)entity;
            continents.removeValue(continent, true);
        }
    }
}
