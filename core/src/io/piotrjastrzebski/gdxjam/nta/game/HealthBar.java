package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class HealthBar extends Actor {
    protected final NukeGame game;
    protected final Entity entity;

    public HealthBar (NukeGame game, Entity entity) {
        this.game = game;
        this.entity = entity;
        setBounds(0, 0, 1, .25f);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (entity.healthCap <= 0) return;
        ShapeDrawer shapes = game.shapes;
        float a = MathUtils.clamp(entity.health / entity.healthCap, 0, 1);

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
