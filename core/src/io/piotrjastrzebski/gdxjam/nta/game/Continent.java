package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.gdxjam.nta.GameScreen;

/**
 * Land area with spots for building stuff
 *
 * Owned by a Player
 */
public class Continent extends Entity {
    Array<Spot> spots;

    public Continent (int id) {
        super(id, 0);
        setDebug(true);
        setTouchable(Touchable.childrenOnly);
        spots = new Array<>();
    }

    public void init (float cx, float cy, float radius) {
        setBounds(cx - radius, cy - radius, radius * 2, radius * 2);

        float scx = radius;
        float scy = radius;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                Spot spot = new Spot(this);
                float ox = MathUtils.random(-radius * .05f, radius * .05f) - .6f;
                float oy = MathUtils.random(-radius * .05f, radius * .05f) - .6f;
                spot.init(scx + x * radius * .45f + ox, scy + y * radius * .45f + oy);
                spot.debug();
//                spot.init(x * radius * .45f + ox, y * radius * .45f + oy);
                addActor(spot);
                spots.add(spot);
            }
        }
    }

    @Override
    public void drawDebug (ShapeRenderer shapes) {
        shapes.end();
        float cx = getX(Align.center);
        float cy = getY(Align.center);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.GOLDENROD);
        shapes.circle(cx, cy, getWidth()/2, 16);
        shapes.getColor().set(owner.tint).a = .2f;
        shapes.circle(cx, cy, getWidth()/2, 16);

        shapes.end();

        // TODO we want like thick outline instead
        shapes.begin(ShapeRenderer.ShapeType.Line);

        super.drawDebug(shapes);

        shapes.setColor(owner.tint);
        shapes.circle(cx, cy, getWidth()/2, 16);
    }

    public void city (Array<City> cities) {
        Array<Spot> s = new Array<>(spots);
        s.shuffle();
        for (City city : cities) {
            Spot spot = s.pop();
            city.init(spot);
        }
    }

    public static class Spot extends Entity {
        protected static final String TAG = Spot.class.getSimpleName();
        Continent continent;

        // what sits on parcel, can be null
        Entity entity = null;

        public Spot (Continent continent) {
            super(++GameScreen.IDS, continent.sort + 1);
            this.continent = continent;
            setTouchable(Touchable.enabled);
            addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {

                }
            });
        }

        public void init (float x, float y) {
            setBounds(x, y, 1.2f, 1.2f);
        }

        public void drawDebug (ShapeRenderer shapes) {
            shapes.end();

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(Color.ORANGE);
            shapes.circle(getX(Align.center), getY(Align.center), getWidth()/2, 16);

            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            super.drawDebug(shapes);
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
