package io.piotrjastrzebski.gdxjam.nta.utils.events;

import io.piotrjastrzebski.gdxjam.nta.game.City;
import io.piotrjastrzebski.gdxjam.nta.game.Player;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerLostCity extends Event{
    public Player player;
    public City city;

    @Override
    public int id () {
        return Events.PLAYER_LOST_CITy;
    }
}
