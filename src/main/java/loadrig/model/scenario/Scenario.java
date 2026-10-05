package loadrig.model.scenario;

import java.util.List;
import loadrig.model.Role;
import loadrig.model.profile.ScenarioName;
import loadrig.model.step.Step;

/**
 * One scenario of the closed list: a class with a fixed skeleton (DR-3). A scenario holds
 * business order and business logic and nothing else - everything a step owes is taken by the
 * step kit's signatures - and states its own step weights, so the mix a plan implies is
 * computed from the scenarios themselves and held against the profile.
 */
public interface Scenario {

    ScenarioName name();

    /** The role whose sessions the iteration takes up and sets down. */
    Role sessionRole();

    /** The actor as the step names say it: what the capture calls this scenario's acts. */
    String actorLabel();

    /** The steps of one iteration, in business order, each weighted in the scenario's mix. */
    List<Step> iteration();
}
