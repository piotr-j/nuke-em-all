package io.piotrjastrzebski.gdxjam.nta.utils.events;

import io.piotrjastrzebski.gdxjam.nta.game.Silo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LaunchNukeEvent extends Event{
    public Silo silo;
    public float sx;
    public float sy;
    public float tx;
    public float ty;

    @Override
    public int id () {
        return Events.LAUNCH_NUKE;
    }

}
