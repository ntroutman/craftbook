package com.sk89q.craftbook.cart;

import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.VehiclesConfiguration;
import com.sk89q.craftbook.bukkit.VehiclesPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.EnumMap;
import java.util.Map;

public class MinecartManager {

    public MinecartManager(VehiclesPlugin plugin) {

        this.plugin = plugin;
        reloadConfiguration(plugin.getLocalConfiguration());
    }

    private final VehiclesPlugin plugin;
    private Map<Material, CartMechanism> mechanisms;

    public void reloadConfiguration(VehiclesConfiguration cfg) {

        mechanisms = new EnumMap<Material, CartMechanism>(Material.class);
        mechanisms.put(cfg.matBoostMax, new CartBooster(100));
        mechanisms.put(cfg.matBoost25x, new CartBooster(1.25));
        mechanisms.put(cfg.matSlow20x, new CartBooster(0.8));
        mechanisms.put(cfg.matSlow50x, new CartBooster(0.5));
        mechanisms.put(cfg.matReverse, new CartReverser());
        mechanisms.put(cfg.matSorter, new CartSorter());
        mechanisms.put(cfg.matStation, new CartStation());
        mechanisms.put(cfg.matEjector, new CartEjector());
        mechanisms.put(cfg.matDeposit, new CartDeposit());
        mechanisms.put(cfg.matTeleport, new CartTeleporter());
        mechanisms.put(cfg.matDispenser, new CartDispenser());
        mechanisms.put(cfg.matMessager, new CartMessager(plugin));
        for (Map.Entry<Material, CartMechanism> ent : mechanisms.entrySet())
            ent.getValue().setMaterial(ent.getKey());
    }

    public void impact(VehicleMoveEvent event) {

        try {
            CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getTo().getBlock());
            cmb.setFromBlock(event.getFrom().getBlock()); // WAI
            CartMechanism thingy = mechanisms.get(cmb.base.getType());
            if (thingy != null) {
                Location from = event.getFrom();
                Location to = event.getTo();
                boolean crossesBlockBoundary =
                        from.getBlockX() == to.getBlockX()
                                && from.getBlockY() == to.getBlockY()
                                && from.getBlockZ() == to.getBlockZ();
                thingy.impact((Minecart) event.getVehicle(), cmb, crossesBlockBoundary);
            }
        } catch (InvalidMechanismException ignored) {
            /* okay, so there's nothing interesting to see here.  carry on then, eh? */
        }
    }

    public void enter(VehicleEnterEvent event) {

        try {
            CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getVehicle().getLocation().getBlock());
            cmb.setFromBlock(event.getVehicle().getLocation().getBlock()); // WAI
            CartMechanism thingy = mechanisms.get(cmb.base.getType());
            if (thingy != null) {
                Location to = event.getVehicle().getLocation();
                Location from = event.getEntered().getLocation();
                boolean crossesBlockBoundary =
                        from.getBlockX() == to.getBlockX()
                                && from.getBlockY() == to.getBlockY()
                                && from.getBlockZ() == to.getBlockZ();
                thingy.enter((Minecart) event.getVehicle(), event.getEntered(), cmb, crossesBlockBoundary);
            }
        } catch (InvalidMechanismException ignored) {
            /* okay, so there's nothing interesting to see here.  carry on then, eh? */
        }
    }

    public void impact(BlockPhysicsEvent event) {

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedImpact(event));
    }

    /**
     * Bukkit reports redstone events before updating the status of the relevant
     * blocks... which had the rather odd effect of causing only input wires
     * from the north causing the responses intended. Scheduling the impact
     * check one tick later dodges the whole issue.
     */
    private class DelayedImpact implements Runnable {

        public DelayedImpact(BlockPhysicsEvent event) {

            huh = event.getBlock();
        }

        private final Block huh;

        @Override
        public void run() {

            try {
                CartMechanismBlocks cmb = CartMechanismBlocks.find(huh);
                CartMechanism thingy = mechanisms.get(cmb.base.getType());
                if (thingy != null)
                    thingy.impact(CartMechanism.getCart(cmb.rail), cmb, false);
            } catch (InvalidMechanismException ignored) {
                /* okay, so there's nothing interesting to see here.  carry on then, eh? */
            }
        }
    }
}
