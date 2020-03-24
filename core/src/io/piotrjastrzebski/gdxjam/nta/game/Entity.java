package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Base class for game stuff on the map
 */
public abstract class Entity extends Group implements Comparable<Entity> {
    public final int id;
    protected final int sort;

    protected Player owner;

    public Entity (int id, int sort) {
        super();
        this.id = id;
        this.sort = sort;
    }


    public void owner (Player player) {
        this.owner = player;
        // event of some sort?
    }

    public Player owner () {
        return owner;
    }

    @Override
    public int compareTo (Entity o) {
        // lets see if this works
        return Integer.compare(sort * 100000 + id, o.sort * 100000 + id);
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity)o;

        return id == entity.id;
    }

    @Override
    public int hashCode () {
        // bad hc but whatever
        return id;
    }
}
