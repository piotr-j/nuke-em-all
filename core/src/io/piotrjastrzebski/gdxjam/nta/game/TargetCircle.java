package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class TargetCircle extends Actor {
    protected final NukeGame game;

    public TargetCircle (NukeGame game) {
        this.game = game;
        setBounds(0, 0, Nuke.blastRadiusOuter, Nuke.blastRadiusOuter);
        setTouchable(Touchable.disabled);
    }

    static Color tmp = new Color();
    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        Color c = getColor();
        ShapeDrawer shapes = game.shapes;

        float cx = getX(Align.center);
        float cy = getY(Align.center);
        float r = Nuke.blastRadiusInner;
        shapes.setColor(tmp.set(Color.RED).mul(c));
        shapes.circle(cx, cy, r -.05f, .1f);
        shapes.filledCircle(cx, cy, r * .25f);

        shapes.setColor(tmp.set(Color.ORANGE).mul(c));
        r = Nuke.blastRadiusOuter;
        shapes.circle(cx, cy, r, .05f);

        shapes.setColor(tmp.set(Color.RED).mul(c));
        r *= .72f;
        shapes.line(cx - r, cy - r, cx + r, cy + r, .1f);
        shapes.line(cx - r, cy + r, cx + r, cy - r, .1f);
    }
}
