package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
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
    public final boolean local;
    // tint added to owned stuff
    public final Color tint = new Color(Color.WHITE);

    Array<Continent> continents = new Array<>();

    Array<Silo> silos = new Array<>();

    Array<City> cities = new Array<>();

    public Player (int id, String name, boolean local) {
        this.id = id;
        this.name = name;
        this.local = local;
        this.tint.set(tints[id % tints.length]);
    }

    public boolean isLocal () {
        return local;
    }

    public void add (Entity entity) {
        if (entity instanceof City) {
            City city = (City)entity;
            if (cities.contains(city, true)) cities.add(city);
        } else if (entity instanceof Silo) {
            Silo silo = (Silo)entity;
            if (silos.contains(silo, true)) silos.add(silo);
        } else if (entity instanceof Continent) {
            Continent continent = (Continent)entity;
            if (continents.contains(continent, true)) continents.add(continent);
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
