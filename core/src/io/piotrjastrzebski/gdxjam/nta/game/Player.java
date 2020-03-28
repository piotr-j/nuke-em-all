package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

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

    // can build on them
    Array<Continent> continents = new Array<>();

    Array<Silo> silos = new Array<>();
    Array<Submarine> submarines = new Array<>();
    Array<Nuke> nukes = new Array<>();

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
}
