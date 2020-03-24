package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Land area with spots for building stuff
 *
 * Owned by a Player
 */
public class Continent extends Entity {
    Circle bounds;
    Array<Spot> spots;

    public Continent (int id) {
        super(id, 0);
        spots = new Array<>();
    }

    public void init (float cx, float cy, float radius) {
        bounds = new Circle(cx, cy, radius);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                Spot spot = new Spot(this);
                float ox = MathUtils.random(-radius * .05f, radius * .05f);
                float oy = MathUtils.random(-radius * .05f, radius * .05f);
                spot.init(x * radius * .45f + ox, y * radius * .45f + oy);
                spots.add(spot);
            }
        }
    }

    @Override
    public void drawDebug (ShapeRenderer shapes) {
        shapes.setColor(Color.GOLDENROD);
        shapes.circle(bounds.x, bounds.y, bounds.radius, 16);
        shapes.getColor().set(owner.tint).a = .2f;
        shapes.circle(bounds.x, bounds.y, bounds.radius, 16);

        for (Spot spot : spots) {
            spot.drawDebug(shapes);
        }

        shapes.end();

        // TODO we want like thick circle instead
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(owner.tint);
        shapes.circle(bounds.x, bounds.y, bounds.radius, 16);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    public void city (Array<City> cities) {
        Array<Spot> s = new Array<>(spots);
        s.shuffle();
        for (City city : cities) {
            Spot spot = s.pop();
            city.init(spot);
        }
    }

    public static class Spot {
        Continent continent;
        Circle bounds = new Circle(0, 0, .6f);

        // what sits on parcel, can be null
        Entity entity = null;

        public Spot (Continent continent) {
            this.continent = continent;
        }

        public void init (float x, float y) {
            bounds.x = x;
            bounds.y = y;
        }

        public void drawDebug (ShapeRenderer shapes) {
            shapes.setColor(Color.ORANGE);
            shapes.circle(x(), y(), bounds.radius, 16);
        }

        public float x () {
            return continent.bounds.x + bounds.x;
        }

        public float y () {
            return continent.bounds.y + bounds.y;
        }
    }



    @Override
    public void owner (Player owner) {
        if (this.owner != null) {
            this.owner.continents.removeValue(this, true);
        }
        super.owner(owner);
        if (this.owner != null) {
            this.owner.continents.add(this);
        }
    }
}
