package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Silo that can fire Nukes
 */
public class Silo extends Entity {

    public Silo (NukeGame game, int id) {
        super(game, id, 20);
        // do we make these smaller?
        setBounds(0, 0, .76f, .76f);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        ShapeDrawer shapes = game.shapes;
        float cx = getX(Align.center);
        float cy = getY(Align.center);
        shapes.setColor(Color.DARK_GRAY);
        shapes.filledCircle(cx, cy, .33f);
        shapes.setColor(Color.GRAY);
        shapes.filledCircle(cx, cy, .2f);
    }
}
