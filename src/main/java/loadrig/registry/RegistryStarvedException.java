package loadrig.registry;

/**
 * Thrown when a registry holds nothing that fits what a step asked for.
 *
 * <p>A starved registry is never papered over with a silent retry: the run states the reason and
 * stops, because a capture full of failures the rig itself caused has measured the script, not
 * the system. Whether a step may wait or reschedule is the profile's decision, made where the
 * step is scheduled - never here.
 */
public class RegistryStarvedException extends RuntimeException {

    public RegistryStarvedException(String message) {
        super(message);
    }
}
