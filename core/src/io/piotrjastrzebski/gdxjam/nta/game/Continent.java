package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Land area with spots for building stuff
 *
 * Owned by a Player
 */
public class Continent extends Entity {
    protected Continents.ContinentData data;

    public Continent (NukeGame game, int id) {
        super(game, id, 0);
        setDebug(true);
        setTouchable(Touchable.childrenOnly);
    }

    public void init (Continents.ContinentData data) {
        this.data = data;
        Rectangle bounds = data.getBoundingRectangle();
        setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        ShapeDrawer shapes = game.shapes;
        Color t = owner.tint;
        shapes.setColor(t.r * .8f, t.g * .8f, t.b * .8f, t.a);
        for (Polygon polygon : data.polygons) {
            polygon(shapes, polygon);
        }
        shapes.setColor(t);
        for (Polygon polygon : data.polygons) {
            shapes.polygon(polygon.getTransformedVertices(), .15f,  JoinType.SMOOTH);
        }
    }

    private void polygon (ShapeDrawer shapes, Polygon polygon) {
        float[] vertices = polygon.getTransformedVertices();
        for (int i = 0, n = vertices.length - 4; i < n; i+=2) {
            // @off
            shapes.filledTriangle(
                vertices[0],
                vertices[1],
                vertices[i + 2],
                vertices[i + 3],
                vertices[i + 4],
                vertices[i + 5]
            );
            // @on
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
