package io.piotrjastrzebski.gdxjam.nta.game.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import io.piotrjastrzebski.gdxjam.nta.utils.FloatValue;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class HealthBar extends Actor {
    protected final NukeGame game;
    protected final FloatValue value;
    protected final FloatValue cap;

    public HealthBar (NukeGame game, FloatValue value, FloatValue cap) {
        this.game = game;
        this.value = value;
        this.cap = cap;
        setBounds(0, 0, 1, .25f);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (cap.get() <= 0) return;
        ShapeDrawer shapes = game.shapes;
        float a = MathUtils.clamp(value.get() / cap.get(), 0, 1);

        // hide if >= 1?
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        shapes.setColor(Color.BLACK);
        shapes.filledRectangle(x, y, width, height);

        // interpolate? green -> yellow -> red
        shapes.setColor(Color.GREEN);
        float border = height * .2f;
        shapes.filledRectangle(x + border, y + border, (width - border * 2) * a, height - border * 2);
    }
}
