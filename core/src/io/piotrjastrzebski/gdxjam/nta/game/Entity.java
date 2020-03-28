package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.AddAction;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import io.piotrjastrzebski.gdxjam.nta.utils.Events;
import io.piotrjastrzebski.gdxjam.nta.utils.command.Explode;

/**
 * Base class for game stuff on the map
 */
public abstract class Entity extends Group implements Comparable<Entity> {
    protected final NukeGame game;
    public final int id;
    protected final int sort;
    protected static Vector2 v2 = new Vector2();
    protected Circle bounds = new Circle();

    protected Player owner;

    protected float health = 1;
    protected float healthCap = 2;
    protected float healthRepair = .05f;


    public Entity (NukeGame game, int id, int sort) {
        super();
        this.game = game;
        this.id = id;
        this.sort = sort;
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        // perhaps tap to repair? make things offline till it is fully repaired
        health += healthRepair * delta;
        if (health > healthCap) health = healthCap;
    }

    public void owner (Player player) {
        this.owner = player;
        // event of some sort?
    }

    public Player owner () {
        return owner;
    }

    public float cx () {
        return getX(Align.center);
    }

    public float cy () {
        return getY(Align.center);
    }

    public float health () {
        return health;
    }

    public float healthCap () {
        return healthCap;
    }

    public boolean damage (float damage) {
        if (health <= 0) return true;

        health -= damage;
        if (health <= 0) {
            health = 0;
            localToStageCoordinates(v2.set(getWidth()/2, getHeight()/2));
            Events.sendDelayed(1/20f, Events.EXPLODE, new Explode(v2.x, v2.y, .5f, 1f, .2f));
            addAction(Actions.sequence(
                Actions.delay(1/20f),
                Actions.removeActor()
            ));
            return true;
        }
        return false;
    }

    public Circle bounds () {
        return bounds;
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

    public void updateBounds () {
        localToStageCoordinates(v2.set(getWidth()/2, getHeight()/2));
        bounds.set(v2.x, v2.y, Math.max(getWidth(), getHeight())/2);
    }
}
