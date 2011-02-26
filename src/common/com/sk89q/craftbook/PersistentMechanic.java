package com.sk89q.craftbook;

import java.util.*;

import com.sk89q.craftbook.util.*;

/**
 * <p>
 * PersistentMechanic exist to keep internal state in situations where the
 * instantiation of the Mechanic may be relatively expensive. However, they are
 * in many cases not appropriate tools since limitations in the core of Bukkit
 * mean that it is not always possible to catch all events that a
 * PersistentMechanic may need in order to perform the caching of mechanism
 * state this class was designed to facilitate with verifiable correctness.
 * </p>
 * 
 * <p>
 * A Mechanic at minimum has one or more BlockVector which it wishes to receive
 * events from -- these are called "triggers". These BlockVector are also
 * registered with a MechanicManager when the Mechanic is loaded into that
 * MechanicManager.
 * </p>
 * 
 * <p>
 * A Mechanic may also have a set of BlockVector which it wishes to receive
 * events from because they may change the state of the mechanism, but they
 * aren't explicitly triggers -- these are called "defining". An example of
 * defining blocks are the vertical column of blocks in an elevator mechanism,
 * since the operation of the elevator depends on where there is open space in
 * blocks that are clearly not trigger blocks. Defining blocks are also
 * registered with a MechanicManager, but are distinct from trigger blocks in
 * that a single BlockVector may be considered defining to multiple mechanisms.
 * </p>
 * 
 * <p>
 * A Mechanic may alter or examine the contents of BlockVector other than those
 * that are explicitly trigger or definer BlockVector. These are the BlockVector
 * that the Mechanic "enhances", but there is no formal interface in which these
 * must be enumerated.
 * </p>
 * 
 * <p>
 * The requirement of providing correct service even when this Mechanic instance
 * is discarded and another reconstructed over the same blocks of the world laid
 * out for all Mechanic still applies to PersistentMechanic!
 * </p>
 * 
 * @author hash
 * 
 */
public abstract class PersistentMechanic extends Mechanic {
    /**
     * Construct the object.
     * 
     * @param triggers
     *            positions that can trigger this mechanic. These may never be
     *            revised.
     */
    protected PersistentMechanic(BlockWorldVector... triggers) {
        super();
        this.triggers = Collections.unmodifiableList(Arrays.asList(triggers));
    }
    
    /**
     * These BlockVector also get loaded into a HashMap in a MechanicManager
     * when the Mechanic is loaded. (The Mechanic still has to keep the list of
     * the triggerable BlockVectors in order to deregister from the HashMap
     * efficiently when unloaded.)
     */
    protected final List<BlockWorldVector> triggers;
    
    /**
     * Get the list of trigger positions that this mechanic uses. This list is
     * constant and cannot change during the run of the mechanic.
     */
    public final List<BlockWorldVector> getTriggerPositions() {
        return triggers;
    }

    /**
     * Get the list of positions that in some way define this mechanic -- this
     * mechanic must be informed of any changes to this positions in order to
     * maintain the validity of their cached state. The contents of this last
     * may change over time and the MechanicManager of this Mechanic must be
     * informed after every change in order to maintain system consistency.
     */
    public abstract List<BlockWorldVector> getDefiningPositions();
}