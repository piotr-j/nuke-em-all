package io.piotrjastrzebski.gdxjam.nta.utils;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Continents {
    private static Array<ContinentData> continents;


    public static Array<ContinentData> continents () {
        if (continents == null) {
            continents = new Array<>();
            {
                ContinentData cd = new ContinentData("???");
                cd.polygons.add(poly(721, 1078, 666, 1020, 516, 847, 592, 849, 782, 978));
                cd.polygons.add(poly(364, 1089, 446, 1089, 692, 1116, 480, 1171));
                cd.polygons.add(poly(563, 745, 488, 782, 451, 772, 551, 715));
                cd.polygons.add(poly(446, 1089, 442, 996, 632, 1070, 692, 1116));
                cd.polygons.add(poly(488, 782, 516, 847, 442, 996, 408, 896, 451, 772));
                cd.polygons.add(poly(516, 847, 666, 1020, 632, 1070, 442, 996));
                cd.createBorder();
                continents.add(cd);
            }
            {
                ContinentData cd = new ContinentData("???");
                cd.polygons.add(poly(696, 251, 710, 336, 688, 703, 638, 497, 660, 343));
                cd.polygons.add(poly(569, 598, 638, 497, 688, 703, 618, 711));
                cd.polygons.add(poly(827, 591, 688, 703, 710, 336, 803, 469));
                cd.createBorder();
                continents.add(cd);
            }
            {
                ContinentData cd = new ContinentData("???");
                cd.polygons.add(poly(1168, 918, 1217, 860, 1268, 918));
                cd.polygons.add(poly(1355, 780, 1268, 918, 1217, 860, 1084, 653, 1270, 701));
                cd.polygons.add(poly(1133, 386, 1190, 413, 1256, 534, 1252, 604, 1109, 551, 1097, 505));
                cd.polygons.add(poly(1313, 684, 1270, 701, 1084, 653, 1109, 551, 1252, 604));
                cd.polygons.add(poly(1082, 886, 1003, 873, 940, 784, 937, 711, 985, 659, 1084, 653, 1127, 841));
                cd.polygons.add(poly(1127, 841, 1084, 653, 1217, 860));
                cd.createBorder();
                continents.add(cd);
            }
            {
                ContinentData cd = new ContinentData("???");
                cd.polygons.add(poly(1003, 1035, 982, 992, 1024, 933, 1074, 940, 1043, 995));
                cd.polygons.add(poly(985, 931, 987, 890, 1013, 885, 1024, 933));
                cd.polygons.add(poly(1013, 885, 1074, 940, 1024, 933));
                cd.polygons.add(poly(1119, 1123, 1054, 1065, 1071, 1011, 1130, 928, 1214, 987, 1190, 1100));
                cd.polygons.add(poly(1043, 995, 1074, 940, 1081, 950, 1071, 1011));
                cd.polygons.add(poly(1144, 887, 1214, 987, 1130, 928));
                cd.polygons.add(poly(1118, 911, 1081, 950, 1074, 940));
                cd.createBorder();
                continents.add(cd);
            }
            {
                ContinentData cd = new ContinentData("???");
                cd.polygons.add(poly(1643, 713, 1640, 767, 1528, 786, 1586, 725));
                cd.polygons.add(poly(1739, 1037, 1715, 1124, 1643, 1164, 1629, 1089));
                cd.polygons.add(poly(1678, 1029, 1630, 1048, 1273, 921, 1673, 977));
                cd.polygons.add(poly(1197, 1098, 1218, 981, 1273, 921, 1629, 1089, 1643, 1164, 1367, 1169));
                cd.polygons.add(poly(1197, 948, 1273, 921, 1218, 981));
                cd.polygons.add(poly(1469, 695, 1488, 755, 1394, 811));
                cd.polygons.add(poly(1640, 767, 1676, 826, 1697, 909, 1673, 977, 1273, 921, 1394, 811));
                cd.polygons.add(poly(1488, 755, 1528, 786, 1394, 811));
                cd.polygons.add(poly(1335, 820, 1394, 811, 1273, 921));
                cd.polygons.add(poly(1630, 1048, 1629, 1089, 1273, 921));
                cd.createBorder();
                continents.add(cd);
            }
            {
                ContinentData cd = new ContinentData("???");
                cd.polygons.add(poly(1635, 370, 1728, 387, 1776, 536, 1656, 467));
                cd.polygons.add(poly(1757, 324, 1824, 377, 1802, 488, 1776, 536, 1728, 387));
                cd.polygons.add(poly(1833, 540, 1802, 488, 1824, 377, 1852, 424));
                cd.createBorder();
                continents.add(cd);
            }
            {
                ContinentData cd = new ContinentData("???");
                cd.polygons.add(poly(783, 184, 749, 110, 820, 107));
                cd.polygons.add(poly(1598, 101, 1547, 138, 1363, 155, 1303, 142, 1464, 23, 1551, 56));
                cd.polygons.add(poly(508, 50, 581, 27, 885, 88, 820, 107, 749, 110, 598, 95));
                cd.polygons.add(poly(1233, 168, 885, 88, 1464, 23, 1303, 142));
                cd.polygons.add(poly(1026, 149, 885, 88, 1122, 145));
                cd.polygons.add(poly(1464, 23, 885, 88, 581, 27));
                cd.createBorder();
                continents.add(cd);
            }
            {
                ContinentData cd = new ContinentData("???");
                cd.polygons.add(poly(811, 1169, 841, 1145, 942, 1118, 985, 1200, 929, 1210, 849, 1200));
                cd.polygons.add(poly(835, 1053, 872, 1089, 841, 1145, 820, 1087));
                cd.polygons.add(poly(872, 1089, 942, 1118, 841, 1145));
                cd.createBorder();
                continents.add(cd);
            }
        }
        return continents;
    }

    private static Polygon poly (float... vertices) {
//        float[] cw = new float[vertices.length];
//        for (int i = 0, n = vertices.length; i < n; i+=2) {
//            cw[ n - i - 2] = vertices[i];
//            cw[ n - i - 1] = vertices[i + 1];
//        }
        Polygon polygon = new Polygon(vertices);
        // we want to match it to weird src image
        float ax = 1280f/2058f;
        float ay = 720f/1262f;
        polygon.setScale(NukeGame.INV_SCALE * ay, NukeGame.INV_SCALE * ay);

        return polygon;
    }

    public static class ContinentData {
        public String name;
        public Array<Polygon> polygons = new Array<>();
        public Array<Polygon> borders = new Array<>();
        protected Rectangle bounds = null;

        public ContinentData (String name) {
            this.name = name;
        }

        void createBorder () {
            // TODO a nice border would be cool i guess, but effort
        }

        public Rectangle getBoundingRectangle () {
            if (bounds != null) {
                return bounds;
            }
            // we have a bunch of polygons per continent, got to merge their bounding boxes
            bounds = new Rectangle();
            if (polygons.size >= 1) {
                bounds.set(polygons.get(0).getBoundingRectangle());
            }
            for (int i = 1; i < polygons.size; i++) {
                bounds.merge(polygons.get(i).getBoundingRectangle());
            }

            return bounds;
        }
    }
}
