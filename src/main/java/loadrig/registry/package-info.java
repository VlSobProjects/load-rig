/**
 * The in-process coordination state shared by all virtual users of one injector JVM:
 * the session registry (SUT sessions decoupled from virtual users, picked by state) and the
 * task registry (task id, status, creator, assignee, hot-set membership) that lets a scenario
 * step find a task it may act on. Plain Java, unit-tested against the SUT's transition table;
 * deliberately no external store, so the capture host carries no extra container.
 */
package loadrig.registry;
