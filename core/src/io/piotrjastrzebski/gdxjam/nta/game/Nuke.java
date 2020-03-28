package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import io.piotrjastrzebski.gdxjam.nta.utils.Events;
import io.piotrjastrzebski.gdxjam.nta.utils.command.Explode;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Nuke launched by a Player
 *
 * Will destroy anything in its blast radius when it hits its target
 *
 * Target area will be contaminated and not usable for building stuff for a while
 *
 */
public class Nuke extends Entity {

    public static float blastRadiusOuter = 1f;
    public static float blastRadiusInner = .5f;
    public static float blastDamage = 1.2f; // center
    public static float flySpeed = 2f;
    // stage coordinates
    protected Vector2 target = new Vector2();
    protected final static Vector2 v2 = new Vector2();
    protected TargetCircle targetCircle;

    public Nuke (NukeGame game, int id) {
        super(game, id, 100);
        setBounds(0, 0, .2f, 1.2f);
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        if (targetCircle == null) {
            targetCircle = new TargetCircle(game);
            targetCircle.setColor(1, 1, 1, .5f);
            targetCircle.setPosition(target.x, target.y, Align.center);
            getStage().getRoot().addActorBefore(this, targetCircle);
        }
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        ShapeDrawer shapes = game.shapes;
        float cx = cx();
        float cy = cy();
        // tint parts or something
        shapes.setColor(Color.GRAY);
        shapes.filledRectangle(getX(), getY(), getWidth(), getHeight(), getRotation() * MathUtils.degRad);
        float o = getWidth() * .2f;
        shapes.setColor(owner.tint);
        shapes.filledRectangle(getX() + o, getY() + o, getWidth() - o * 2, getHeight() - o * 2, getRotation() * MathUtils.degRad);

        shapes.setColor(owner.tint.r, owner.tint.g, owner.tint.b, .3f);
        shapes.circle(cx, cy, blastRadiusOuter, .05f);
    }

    void explode () {
        clearActions();
        targetCircle.remove();
        Events.send(Events.EXPLODE, new Explode(cx(), cy(), blastRadiusInner, blastRadiusOuter, blastDamage));
        remove();
    }

    public void target (float tx, float ty) {
        target.set(tx, ty);
        // assumes we have position
        float dst = target.dst(cx(), cy());
        float angle = v2.set(target).sub(cx(), cy()).angle();
        setRotation(angle - 90);
        float duration = dst/flySpeed;
        addAction(Actions.sequence(
            Actions.moveToAligned(tx, ty, Align.center, duration),
            Actions.run(this::explode)
        ));
    }
}
