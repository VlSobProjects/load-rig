package loadrig.model.profile;

import java.util.EnumMap;
import java.util.Map;

/**
 * The service levels a campaign is judged by: one 95th-percentile figure per band and one hard
 * ceiling over everything the injector measures.
 *
 * <p>They are chosen, not measured. On this demonstration stand they are one of the four
 * calibration levers, and that freedom carries two conditions the shape of this type enforces:
 * they are fixed before a campaign, so they live in a file of their own rather than inside a
 * profile - a morning and an evening variant judged by different figures compare nothing - and
 * they are recorded with every capture, which is why the whole record goes into the run's
 * load-profile description.
 *
 * <p>The percentile itself is not a lever and is not configurable: the specification's table is
 * a table of 95th percentiles, and a run that quietly judged itself at the median would leave a
 * capture nobody could re-read. The figures are the lever; the statistic is the criterion's
 * shape.
 */
public record ServiceLevels(Map<ServiceLevelBand, Integer> percentile95Millis,
        int hardCeilingMillis) {

    /** The percentile every band's figure is stated at, and the one a run measures. */
    public static final int PERCENTILE = 95;

    public ServiceLevels {
        if (hardCeilingMillis <= 0) {
            throw new IllegalArgumentException("the hard ceiling must be positive, and it is "
                    + hardCeilingMillis + " ms");
        }
        percentile95Millis = figureOfEveryBand(percentile95Millis, hardCeilingMillis);
    }

    /** The figure this band is held to, in milliseconds. */
    public int levelOf(ServiceLevelBand band) {
        return percentile95Millis.get(band);
    }

    private static Map<ServiceLevelBand, Integer> figureOfEveryBand(
            Map<ServiceLevelBand, Integer> stated, int hardCeilingMillis) {
        EnumMap<ServiceLevelBand, Integer> copy = new EnumMap<>(ServiceLevelBand.class);
        for (ServiceLevelBand band : ServiceLevelBand.values()) {
            Integer millis = stated.get(band);
            if (millis == null) {
                throw new IllegalArgumentException("the service levels state no figure for "
                        + band.key() + "; a band nobody stated is a band nothing is judged by,"
                        + " and every act the rig performs belongs to one");
            }
            if (millis <= 0) {
                throw new IllegalArgumentException("the figure for " + band.key()
                        + " must be positive, and it is " + millis + " ms");
            }
            if (millis > hardCeilingMillis) {
                throw new IllegalArgumentException("the figure for " + band.key() + " is "
                        + millis + " ms, above the hard ceiling of " + hardCeilingMillis
                        + " ms; a band a run may pass while breaching the ceiling judges nothing");
            }
            copy.put(band, millis);
        }
        return Map.copyOf(copy);
    }
}
