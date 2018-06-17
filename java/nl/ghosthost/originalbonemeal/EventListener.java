package nl.ghosthost.originalbonemeal;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
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
            Block block = e.getBlock();
            Material blockType = block.getType();
            final Inventory blockInventory;

            if (blockType == Material.DISPENSER) {
                Dispenser dispenser = (Dispenser) block.getState();
                blockInventory = dispenser.getInventory();
            }
            else if (blockType == Material.DROPPER){
                Dropper dropper = (Dropper) block.getState();
                blockInventory = dropper.getInventory();
            }
            else return;

            ItemStack[] items = blockInventory.getStorageContents();
            int StacksInInv = 0;
            int TotalDropperItems = 1;

            /* Get the amount of stacks in the dispenser/dropper
            *  Also the total item amount in case of a dropper as
            *  it only uses 1 item at a time from a stack
            */
            for (ItemStack itemObj : items) {
                if (itemObj != null && itemObj.getAmount() > 0) {
                    StacksInInv++;
                    TotalDropperItems += itemObj.getAmount();
                }
            }

            if ((blockType == Material.DROPPER && TotalDropperItems > 1) || (blockType == Material.DISPENSER && item.getAmount() > 1)) {
                final ItemStack newStack = item;
                newStack.setAmount(1);

                // Need to wait 1 tick to remove the second last item from the dropper
                if (blockType == Material.DROPPER && TotalDropperItems == 2) {
                    getServer().getScheduler().scheduleSyncDelayedTask(bonemealPlugin, new Runnable() {

                        public void run() {
                            blockInventory.removeItem(newStack);
                        }

                    }, 1);
                }
                else {
                    blockInventory.removeItem(newStack);
                }
            }
            // Need to wait 1 tick to remove the last item
            else if ((blockType == Material.DROPPER && TotalDropperItems == 1) || (blockType == Material.DISPENSER && StacksInInv == 1 && item.getAmount() == 1)) {
                getServer().getScheduler().scheduleSyncDelayedTask(bonemealPlugin, new Runnable() {

                    public void run() {
                        blockInventory.clear();
                    }

                }, 1);
            }
        }
    }
}
