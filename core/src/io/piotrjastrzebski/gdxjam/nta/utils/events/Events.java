package io.piotrjastrzebski.gdxjam.nta.utils.events;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.Telegraph;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Events {
    private static final MessageDispatcher dispatcher = new MessageDispatcher();

    public final static int LAUNCH_NUKE = 1;
    public final static int EXPLODE = 2;
    public final static int PLAYER_LOST_CITy = 3;

    public static void register (Telegraph listener, int msg) {
        dispatcher.addListener(listener, msg);
    }

    public static void register (Telegraph listener, int... msgs) {
        for (int msg : msgs) {
            register(listener, msg);
        }
    }

    public static void unregister (Telegraph listener, int msg) {
        dispatcher.removeListener(listener, msg);
    }

    public static void unregister (Telegraph listener, int... msgs) {
        for (int msg : msgs) {
            unregister(listener, msg);
        }
    }

    public static void update (float delta) {
        // timepiece is used for keeping track of time in events
        GdxAI.getTimepiece().update(delta);
        dispatcher.update();
    }


    public static void send (int msg) {
        dispatcher.dispatchMessage(msg);
    }

    public static void send (Event event) {
        dispatcher.dispatchMessage(event.id(), event);
    }

    public static void sendDelayed (float delay, int msg) {
        dispatcher.dispatchMessage(delay, msg);
    }

    public static void sendDelayed (float delay, Event event) {
        dispatcher.dispatchMessage(delay, event.id(), event);
    }
}
