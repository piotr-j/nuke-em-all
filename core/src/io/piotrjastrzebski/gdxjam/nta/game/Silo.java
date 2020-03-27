package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.gdxjam.nta.GameScreen;
import lombok.extern.slf4j.Slf4j;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Silo that can fire Nukes
 */
@Slf4j
public class Silo extends Entity {
    protected final GameScreen gs;
    protected Vector2 target = new Vector2();
    protected boolean targeting;
    protected float reloadDuration = 2;
    protected float reloadTimer = 0;
    protected TargetCircle targetCircle;

    public Silo (GameScreen gs, int id) {
        super(gs.game(), id, 20);
        this.gs = gs;
        // do we make these smaller?
        setBounds(0, 0, .76f, .76f);
        setTouchable(Touchable.enabled);
    }

    @Override
    public void owner (Player player) {
        super.owner(player);
        clearListeners();

        if (player != gs.player()) return;

        addListener(new ActorGestureListener(.4f, 0.4f, 1.1f, 0.15f) {
            @Override
            public void tap (InputEvent event, float x, float y, int count, int button) {
                // speed up? nah :D
            }

            @Override
            public void pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
                if (reloadTimer > 0) return;
                target.set(x, y);
                targeting = true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (targeting) {
                    launch(target.x, target.y);
                }
                targeting = false;
            }
        });
    }

    public void launch (float x, float y) {
        if (reloadTimer > 0) {
            log.warn("Silo#{} cant fire now, reloading {}", id, reloadTimer);
            return;
        }
        if (targetCircle != null) targetCircle.remove();
        v2.set(x, y);
        localToStageCoordinates(v2);
        log.warn("Silo#{} launching at {}", id, v2);
        reloadTimer = reloadDuration;
        gs.launchNuke(this, v2.x, v2.y);
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        if (reloadTimer > 0) {
            reloadTimer -=delta;
            if (reloadTimer < 0) reloadTimer = 0;
        }
        if (targeting) {
            if (targetCircle == null) {
                targetCircle = new TargetCircle(gs.game());
            }
            if (targetCircle.getParent() == null) {
                getStage().addActor(targetCircle);
            }
            v2.set(target);
            localToStageCoordinates(v2);
            targetCircle.setPosition(v2.x, v2.y, Align.center);
        }
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

        { // reload timer
            float a = 1 - reloadTimer/ reloadDuration;
            shapes.setColor(Color.BLACK);
            shapes.filledRectangle(cx - .5f, cy - .6f, 1f, .24f);
            shapes.setColor(Color.GREEN);
            shapes.filledRectangle(cx - .44f, cy - .55f, .9f * a, .12f);
        }
    }
}
