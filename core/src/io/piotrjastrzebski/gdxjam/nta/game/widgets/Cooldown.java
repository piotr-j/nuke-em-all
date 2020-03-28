package io.piotrjastrzebski.gdxjam.nta.game.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import io.piotrjastrzebski.gdxjam.nta.utils.FloatValue;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Cooldown extends Actor {
    protected final NukeGame game;
    protected final FloatValue value;
    protected final FloatValue cap;
    protected boolean ready;

    public Cooldown (NukeGame game, FloatValue value, FloatValue cap) {
        this.game = game;
        this.value = value;
        this.cap = cap;
        setBounds(0, 0, 1, 1);
        float a = MathUtils.clamp(value.get() / cap.get(), 0, 1);
        ready = a <= 0;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (cap.get() <= 0) return;

        ShapeDrawer shapes = game.shapes;
        float a = MathUtils.clamp(value.get() / cap.get(), 0, 1);
        if (a <= 0) {
            if (!ready) {
                ready = true;
                game.sounds.cooldown.play();
            }
            return;
        }
        if (a >= 1) {
            return;
        }
        ready = false;

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        float cx = x + width/2;
        float cy = y + height/2;

        // arc looks janky but whatever
        shapes.setColor(Color.DARK_GRAY);
        shapes.arc(cx, cy, width/2, MathUtils.PI /2, MathUtils.PI2 * a, .3f);
        shapes.setColor(Color.GREEN);
        shapes.arc(cx, cy, width/2, MathUtils.PI /2, MathUtils.PI2 * a, .15f);

    }

}
