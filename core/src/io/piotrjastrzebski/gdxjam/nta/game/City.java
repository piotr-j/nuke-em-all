package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

/**
 * City owned by a Player, generates resources
 */
public class City extends Entity {
    protected static final String TAG = City.class.getSimpleName();
    String tag;
    Rectangle bounds;
    Continent.Spot spot;

    public City (int id) {
        super(id, 11);
        tag = TAG + "#" +id;
        bounds = new Rectangle(-.5f, -.5f, 1, 1);
    }

    public void init (Continent.Spot spot) {
        this.spot = spot;
        spot.entity = this;
    }

    @Override
    public void drawDebug (ShapeRenderer shapes) {
        shapes.setColor(Color.GRAY);
        float x = spot.x() + bounds.x;
        float y = spot.y() + bounds.y;
        shapes.rect(x, y, bounds.width, bounds.height);
    }

    @Override
    public boolean contains (float x, float y) {
        return bounds.contains(x - spot.x(), y - spot.y());
    }

    @Override
    public boolean click (float x, float y) {
        Gdx.app.log(tag, "clicked!");
        return true;
    }
}
