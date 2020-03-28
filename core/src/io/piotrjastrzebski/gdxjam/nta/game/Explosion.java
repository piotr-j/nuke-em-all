package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Nuke launched by a Player
 *
 * Will destroy anything in its blast radius when it hits its target
 *
 * Target area will be contaminated and not usable for building stuff for a while
 *
 */
public class Explosion extends Actor {
    protected final NukeGame game;
    protected boolean exploded;
    protected float innerRadius;
    protected float outerRadius;

    public Explosion (NukeGame game, float innerRadius, float outerRadius) {
        super();
        this.game = game;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        setBounds(0, 0, outerRadius, outerRadius);
        setTouchable(Touchable.disabled);
    }

    float duration = .5f;
    float timer;
    @Override
    public void act (float delta) {
        super.act(delta);
        timer += delta;
        if (!exploded) {
            if (timer > duration) {
                timer -= duration;
                duration *= 2;
                exploded = true;
            }
        } else {
            if (timer > duration) {
                remove();
            }
        }
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        ShapeDrawer shapes = game.shapes;
        float cx = getX(Align.center);
        float cy = getY(Align.center);
        float a = 1 - timer / duration;
        if (exploded) {
            shapes.setColor(.3f, .3f, .3f, .75f * a);
            shapes.filledCircle(cx, cy, outerRadius);
            shapes.setColor(.1f, .1f, .1f, .9f * a);
            shapes.filledCircle(cx, cy, innerRadius);
        } else {
            if (a > .9f) {
                shapes.setColor(Color.WHITE);
                shapes.filledCircle(cx, cy, outerRadius);
                shapes.filledCircle(cx, cy, innerRadius);
            } else {
                shapes.setColor(1, .5f, .5f, .75f + a * .25f);
                shapes.filledCircle(cx, cy, outerRadius);
                shapes.setColor(1, 1, 0, .75f + a * .25f);
                shapes.filledCircle(cx, cy, innerRadius);
            }
        }
    }
}
