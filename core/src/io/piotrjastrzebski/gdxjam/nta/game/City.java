package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import lombok.extern.slf4j.Slf4j;

/**
 * City owned by a Player, generates resources
 */
@Slf4j
public class City extends Entity {
    String tag;

    public City (NukeGame game, int id) {
        super(game, id, 11);
    }


    @Override
    public void drawDebug (ShapeRenderer shapes) {
        super.drawDebug(shapes);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.GRAY);
        shapes.rect(getX(), getY(), getWidth(), getHeight());

        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
    }
}
