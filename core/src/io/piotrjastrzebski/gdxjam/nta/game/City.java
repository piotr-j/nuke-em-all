package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import io.piotrjastrzebski.gdxjam.nta.game.widgets.HealthBar;
import lombok.extern.slf4j.Slf4j;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * City owned by a Player, generates resources
 */
@Slf4j
public class City extends Entity {
    public City (NukeGame game, int id) {
        super(game, id, 11);
        setBounds(0, 0, .76f, .76f);

        health = healthCap = 2;

        HealthBar healthBar = new HealthBar(game, () -> health, () -> healthCap);
        healthBar.setPosition(getWidth() * .5f, -.2f, Align.center);
        addActor(healthBar);
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        ShapeDrawer shapes = game.shapes;

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();
//        shapes.setColor(Color.GRAY);
//        shapes.filledRectangle(x, y, width, height);
        shapes.setColor(Color.DARK_GRAY);
        shapes.setColor(Color.GRAY);
        shapes.filledRectangle(x + width * .1f, y, width * .2f, height * .75f);
        shapes.filledRectangle(x + width * .7f, y, width * .2f, height * .6f);

        shapes.filledRectangle(x + width * .4f, y, width * .2f, height);

        shapes.setColor(Color.DARK_GRAY);
        shapes.filledRectangle(x, y, width, height * .25f);
    }
}
