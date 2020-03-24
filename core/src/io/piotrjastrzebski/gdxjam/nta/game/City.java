package io.piotrjastrzebski.gdxjam.nta.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import lombok.extern.slf4j.Slf4j;

/**
 * City owned by a Player, generates resources
 */
@Slf4j
public class City extends Entity {
    String tag;
    Continent.Spot spot;

    public City (int id) {
        super(id, 11);
    }

    public void init (Continent.Spot spot) {
        this.spot = spot;
        spot.entity = this;
        spot.addActor(this);
        setBounds(spot.getWidth()/2 - .5f, spot.getHeight()/2 - .5f, 1, 1);
        addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

//                Gdx.app.log(tag, "Hi!");
            }
        });
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
