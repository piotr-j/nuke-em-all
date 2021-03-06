package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import io.piotrjastrzebski.gdxjam.nta.utils.events.Events;
import io.piotrjastrzebski.gdxjam.nta.utils.events.ExplodeEvent;

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
    protected float healthCap = -1;
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
        if (this.owner != null) {
            this.owner.remove(this);
        }
        this.owner = player;
        if (this.owner != null) {
            this.owner.add(this);
        }
        // event of some sort?
    }

    public Player owner () {
        return owner;
    }

    float cx () {
        return getX(Align.center);
    }

    float cy () {
        return getY(Align.center);
    }

    public Vector2 sc () {
        localToStageCoordinates(v2.set(getWidth() / 2, getHeight() / 2));
        return v2;
    }

    public boolean damage (float damage) {
        if (healthCap < 0) return false;
        if (health <= 0) return true;

        health -= damage;
        if (health <= 0) {
            health = 0;
            onDestroy();
            return true;
        }
        return false;
    }

    protected void onDestroy () {
        if (owner != null) {
            owner.remove(this);
        }
        Vector2 sc = sc();
        Events.sendDelayed(1/20f, new ExplodeEvent(sc.x, sc.y, .5f, 1f, .2f));
        addAction(Actions.sequence(
            Actions.delay(1/20f),
            Actions.removeActor()
        ));
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
        Vector2 sc = sc();
        bounds.set(sc.x, sc.y, Math.max(getWidth(), getHeight())/2);
    }
}
