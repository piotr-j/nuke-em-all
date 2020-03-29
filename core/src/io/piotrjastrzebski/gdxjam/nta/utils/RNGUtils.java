package io.piotrjastrzebski.gdxjam.nta.utils;

import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class RNGUtils {
    static Random random;

    public static void init (long seed) {
        random = new Random(seed);
    }

    public static <T> Array<T> shuffle(Array<T> array) {
        T[] items = array.items;
        for (int i = array.size - 1; i >= 0; i--) {
            int ii = random.nextInt(i + 1);
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
        return array;
    }

    public static float random(float start, float end) {
        return start + random.nextFloat() * (end - start);
    }

}
