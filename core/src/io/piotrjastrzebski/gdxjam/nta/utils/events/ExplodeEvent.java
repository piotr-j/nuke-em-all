package io.piotrjastrzebski.gdxjam.nta.utils.events;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExplodeEvent extends Event {
    public float cx;
    public float cy;
    public float radius;
    public float falloffRadius;
    public float damage;

    @Override
    public int id () {
        return Events.EXPLODE;
    }
}
