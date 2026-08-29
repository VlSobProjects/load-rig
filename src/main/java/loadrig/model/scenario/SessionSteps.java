package loadrig.model.scenario;

import static us.abstracta.jmeter.javadsl.JmeterDsl.ifController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;

import java.util.ArrayList;
import java.util.List;
import loadrig.model.Correlation;
import loadrig.model.Role;
import loadrig.model.SutSurface;
import loadrig.model.step.ContentExpectation;
import loadrig.model.step.StepKit;
import loadrig.registry.RegistryStarvedException;
import loadrig.registry.SessionRegistry;
import loadrig.registry.TransportContext;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.property.JMeterProperty;
import us.abstracta.jmeter.javadsl.core.controllers.DslIfController;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler;

/**
 * The session keeping of one scenario's iteration: the iteration takes up a session of the
 * scenario's role at its start, signs one in when none is free, and sets it down at its end
 * with the transport context attached - so a session outlives the iteration and passes between
 * threads, which is what decouples SUT sessions from virtual users.
 *
 * <p>The thread's own cookie store is only a scratch register: before the iteration's first
 * request the held session's context is loaded into it, after every request it is mirrored
 * back, and at the iteration's end the context returns to the session registry. Letting the
 * injector's store do the cookie work during the requests keeps redirect chains correct - the
 * sign-in answers with a fresh cookie mid-redirect - while no session state survives in any
 * thread.
 *
 * <p>The bookkeeping acts are samplers whose results are ignored: they must run inside the
 * iteration, but a capture that counted them would count acts no user performs. A bookkeeping
 * failure is not ignored - it lands in the log as the error it is.
 */
final class SessionSteps {

    /** The account whose session the iteration holds, read by every request that names itself. */
    static final String SESSION_USER_VARIABLE = "sessionUser";

    /** The token of the held session, scraped at sign-in and carried in its context since. */
    static final String SESSION_TOKEN_VARIABLE = "sessionToken";

    private static final String LEASE_SLOT = "sessionLease";
    private static final String COOKIE_JAR_SLOT = "sessionCookieJar";
    private static final String CONTEXT_LOADED_FLAG = "sessionContextLoaded";
    private static final String NEEDS_SIGN_IN_FLAG = "sessionNeedsSignIn";

    private final StepKit kit;
    private final ScenarioWiring wiring;
    private final Role role;
    private final String actorLabel;

    SessionSteps(StepKit kit, ScenarioWiring wiring, Role role, String actorLabel) {
        this.kit = kit;
        this.wiring = wiring;
        this.role = role;
        this.actorLabel = actorLabel;
    }

    /**
     * Loads the held session's cookies into the thread's store before the iteration's first
     * request. Scoped to the whole thread group; requests after the first find the store
     * already loaded and leave it alone, so what the requests accumulate is never reverted.
     */
    DslJsr223PreProcessor contextIntoThread() {
        return jsr223PreProcessor(s -> {
            if (!(s.sampler instanceof HTTPSamplerBase http)
                    || Boolean.parseBoolean(s.vars.get(CONTEXT_LOADED_FLAG))) {
                return;
            }
            List<TransportContext.CookieFact> jar = cookieJar(s.vars.getObject(COOKIE_JAR_SLOT));
            if (jar == null) {
                return;
            }
            CookieManager store = http.getCookieManager();
            store.clear();
            for (TransportContext.CookieFact fact : jar) {
                store.add(new Cookie(fact.name(), fact.value(), fact.domain(), fact.path(),
                        fact.secure(), fact.expires()));
            }
            s.vars.put(CONTEXT_LOADED_FLAG, "true");
        });
    }

    /**
     * Mirrors the thread's cookie store back into the held session's jar after every request,
     * so the context set down at the iteration's end is the one the last answer produced.
     */
    DslJsr223PostProcessor cookiesOutOfThread() {
        return jsr223PostProcessor(s -> {
            if (!(s.sampler instanceof HTTPSamplerBase http)) {
                return;
            }
            List<TransportContext.CookieFact> jar = cookieJar(s.vars.getObject(COOKIE_JAR_SLOT));
            if (jar == null) {
                return;
            }
            jar.clear();
            for (JMeterProperty property : http.getCookieManager().getCookies()) {
                Cookie cookie = (Cookie) property.getObjectValue();
                jar.add(new TransportContext.CookieFact(cookie.getName(), cookie.getValue(),
                        cookie.getDomain(), cookie.getPath(), cookie.getSecure(),
                        cookie.getExpires()));
            }
        });
    }

    /**
     * Takes up a session for the iteration: a free signed-in session of the role when one
     * exists, otherwise an account to sign in with. A pool that cannot seat even a sign-in is a
     * configuration mistake, reported once and ending the thread rather than flooding the
     * capture with the rig's own failures.
     */
    DslJsr223Sampler takeUp() {
        return jsr223Sampler(actorLabel + " takes up a session", s -> {
            s.vars.put(CONTEXT_LOADED_FLAG, "false");
            s.vars.putObject(LEASE_SLOT, null);
            s.vars.putObject(COOKIE_JAR_SLOT, null);
            SessionRegistry.Lease lease;
            boolean signIn;
            try {
                try {
                    lease = wiring.sessions().acquire(role);
                    signIn = false;
                } catch (RegistryStarvedException noneSignedIn) {
                    lease = wiring.sessions().acquireToSignIn(role);
                    signIn = true;
                }
            } catch (RegistryStarvedException poolExhausted) {
                s.ctx.getThread().stop();
                throw poolExhausted;
            }
            TransportContext context =
                    signIn ? TransportContext.fresh() : wiring.sessions().contextOf(lease);
            s.vars.putObject(LEASE_SLOT, lease);
            s.vars.putObject(COOKIE_JAR_SLOT, new ArrayList<>(context.cookies()));
            s.vars.put(SESSION_USER_VARIABLE, lease.username());
            s.vars.put(SESSION_TOKEN_VARIABLE, context.pageToken());
            s.vars.put(NEEDS_SIGN_IN_FLAG, String.valueOf(signIn));
            s.sampleResult.setIgnore();
        });
    }

    /**
     * The sign-in a fresh session performs: the login page for the pre-session token, then the
     * credentials. Every pool account signs in with the pool password it was provisioned with.
     * A manager's landing screen offers the assignee options, so it is where the user directory
     * learns the identities the creations, the hand-outs and the reports name accounts by.
     */
    DslIfController signInWhenNeeded() {
        DslHttpSampler opensTheLoginPage = kit.pageRequest(
                actorLabel + " opens the login page", SutSurface.LOGIN,
                ContentExpectation.renders("the login page offers the sign-in form",
                        "name=\"" + SutSurface.CSRF_FIELD + "\""),
                Correlation.sessionToken(SESSION_TOKEN_VARIABLE))
                .children(kit.thinkTimer());
        DslHttpSampler signsIn = kit.pageRequest(actorLabel + " signs in", SutSurface.LOGIN,
                ContentExpectation.renders("the sign-in reached an authenticated screen",
                        SutSurface.AUTHENTICATED_MARK),
                Correlation.sessionToken(SESSION_TOKEN_VARIABLE))
                .method("POST")
                .param(SutSurface.USERNAME_FIELD, StepKit.variable(SESSION_USER_VARIABLE))
                .param(SutSurface.PASSWORD_FIELD, wiring.poolPassword())
                .param(SutSurface.CSRF_FIELD, StepKit.variable(SESSION_TOKEN_VARIABLE));
        if (role == Role.MANAGER) {
            signsIn.children(directoryHarvest());
        }
        return ifController(s -> Boolean.parseBoolean(s.vars.get(NEEDS_SIGN_IN_FLAG)),
                opensTheLoginPage, signsIn);
    }

    /**
     * Sets the session down: the context the iteration ends with returns to the registry and
     * the session stays signed in for its next holder. A context that carries no token is not a
     * session - the sign-in that should have produced it was refused - and is reported signed
     * out instead of being handed to the next step as if it worked.
     */
    DslJsr223Sampler setDown() {
        return jsr223Sampler(actorLabel + " sets the session down", s -> {
            SessionRegistry.Lease lease = (SessionRegistry.Lease) s.vars.getObject(LEASE_SLOT);
            if (lease != null) {
                s.vars.putObject(LEASE_SLOT, null);
                List<TransportContext.CookieFact> jar =
                        cookieJar(s.vars.getObject(COOKIE_JAR_SLOT));
                String token = s.vars.get(SESSION_TOKEN_VARIABLE);
                TransportContext context = new TransportContext(
                        jar == null ? List.of() : jar, token == null ? "" : token);
                if (context.carriesAToken()) {
                    wiring.sessions().attach(lease, context);
                    wiring.sessions().release(lease);
                } else {
                    wiring.sessions().signedOut(lease);
                }
            }
            s.sampleResult.setIgnore();
        });
    }

    private DslJsr223PostProcessor directoryHarvest() {
        List<String> pool = new ArrayList<>();
        pool.addAll(wiring.sessions().namesOf(Role.WORKER));
        pool.addAll(wiring.sessions().namesOf(Role.MANAGER));
        return jsr223PostProcessor(s -> {
            String answer = s.prevResponse();
            for (String username : pool) {
                Correlation.userIdentity("directory", username).read(answer)
                        .ifPresent(id -> wiring.directory().learned(username, id));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static List<TransportContext.CookieFact> cookieJar(Object slot) {
        return (List<TransportContext.CookieFact>) slot;
    }
}
