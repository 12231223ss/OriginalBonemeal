package nl.ghosthost.originalbonemeal;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Bukkit.getServer;

public class EventListener implements Listener {
    private OriginalBonemeal bonemealPlugin;
    private List<String> Crops;
    private List<String> Stems;

    public EventListener(OriginalBonemeal plugin) {
        this.bonemealPlugin = plugin;
        Crops = bonemealPlugin.getConfig().getStringList("crops");
        Stems = bonemealPlugin.getConfig().getStringList("stems");
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getItem() == null) return;

        Block clickedBlock = e.getClickedBlock();
        boolean usedBonemeal = false;

        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (!bonemealPlugin.use_permissions || e.getPlayer().isOp() || e.getPlayer().hasPermission("classicbonemeal.use"))) {
            ItemStack item = e.getItem();

            if (item != null && item.getType().equals(Material.BONE) && (e.getPlayer().isOp() || e.getPlayer().hasPermission("classicbonemeal.blocktype"))) Utils.getBlockType(e.getPlayer(), clickedBlock);

            if (item != null && item.getData().getData() == 15) {
                if (Crops.contains(clickedBlock.getType().toString().toLowerCase())) usedBonemeal = Utils.growCrop(clickedBlock);
                else if (Stems.contains(clickedBlock.getType().toString().toLowerCase())) usedBonemeal = Utils.growFromStem(clickedBlock);
                else if (clickedBlock.getType().equals(Material.SAPLING)) usedBonemeal = Utils.growTree(clickedBlock);
                else if (clickedBlock.getType().equals(Material.COCOA)) usedBonemeal = Utils.growCocoa(clickedBlock);
                else if (clickedBlock.getType().equals(Material.NETHER_WARTS)) usedBonemeal = Utils.growNetherWart(clickedBlock);
                else return;

                e.setCancelled(true);
            }

            if (usedBonemeal) {
                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    e.getItem().setAmount(e.getItem().getAmount() - 1);
                }
            }
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        if (e.getBlock() == null || e.getItem() == null) return;

        Block relative = e.getBlock().getRelative(((org.bukkit.material.Dispenser) e.getBlock().getState().getData()).getFacing());

        boolean usedBonemeal = false;
        ItemStack item = e.getItem();

        if (item != null && item.getData().getData() == 15) {
            if (Crops.contains(relative.getType().toString().toLowerCase())) usedBonemeal = Utils.growCrop(relative);
            else if (Stems.contains(relative.getType().toString().toLowerCase())) usedBonemeal = Utils.growFromStem(relative);
            else if (relative.getType().equals(Material.SAPLING)) usedBonemeal = Utils.growTree(relative);
            else if (relative.getType().equals(Material.COCOA)) usedBonemeal = Utils.growCocoa(relative);
            else if (relative.getType().equals(Material.NETHER_WARTS)) usedBonemeal = Utils.growNetherWart(relative);
            else return;

            e.setCancelled(true);
        }

        if (usedBonemeal) {
            Dispenser dispenser = (Dispenser) e.getBlock().getState();
            final Inventory dispenserInventory = dispenser.getInventory();

            ItemStack[] items = dispenserInventory.getStorageContents();
            int totalItems = 0;

            for (ItemStack itemObj : items) {
                if (itemObj != null && itemObj.getAmount() > 0) {
                    totalItems++;
                }
            }

            if (totalItems > 1 || (totalItems == 1 && item.getAmount() > 1)) {
                ItemStack newStack = item;
                newStack.setAmount(1);
                dispenserInventory.removeItem(newStack);
            }
            else if (totalItems == 1 && item.getAmount() == 1) {
                getServer().getScheduler().scheduleSyncDelayedTask(bonemealPlugin, new Runnable() {

                    public void run() {
                        dispenserInventory.clear();
                    }

                }, 1);
            }
        }
    }
}
