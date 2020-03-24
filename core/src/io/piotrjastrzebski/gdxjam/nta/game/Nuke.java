package io.piotrjastrzebski.gdxjam.nta.game;

/**
 * Nuke launched by a Player
 *
 * Will destroy anything in its blast radius when it hits its target
 *
 * Target area will be contaminated and not usable for building stuff for a while
 *
 */
public class Nuke extends Entity {

    public Nuke (int id) {
        super(id, 100);
    }
}
