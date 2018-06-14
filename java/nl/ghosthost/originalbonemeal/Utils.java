package nl.ghosthost.originalbonemeal;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.Tree;

import java.util.Random;

public class Utils {
    public static void getBlockType(Player player, Block block) {
        player.sendMessage(ChatColor.YELLOW + "This block is a " + block.getType());
    }

    public static boolean growCrop(Block block) {
        Crops crop = (Crops) block.getState().getData();

        if (crop.getState() != CropState.RIPE) {
            block.setData(CropState.RIPE.getData());

            return true;
        }

        return false;
    }

    public static boolean growNetherWart(Block block) {
        if (block.getData() != 3) {
            block.setData((byte) 3);

            return true;
        }

        return false;
    }

    public static boolean growTree(Block block) {
        Byte data = block.getData();
        int id = block.getTypeId();
        Tree tree = new Tree(id, data);
        TreeSpecies species = tree.getSpecies();

        TreeType type = null;

        switch (species) {
            case BIRCH:
                type = (new Random().nextDouble() <= OriginalBonemeal.mega_tree_chance) ? TreeType.TALL_BIRCH : TreeType.BIRCH;
                break;
            case ACACIA:
                type = TreeType.ACACIA;
                break;
            case DARK_OAK:
                type = TreeType.DARK_OAK;
                break;
            case JUNGLE:
                return generateLargeTree(block, TreeType.SMALL_JUNGLE, TreeType.JUNGLE);
            case REDWOOD:
                return generateLargeTree(block, (new Random().nextDouble() < OriginalBonemeal.mega_tree_chance) ? TreeType.TALL_REDWOOD : TreeType.REDWOOD, TreeType.MEGA_REDWOOD);
            case GENERIC:
                type = (new Random().nextDouble() < OriginalBonemeal.mega_tree_chance) ? TreeType.BIG_TREE : TreeType.TREE;
                break;
            default:
                return false;
        }

        block.setType(Material.AIR);
        boolean grewTree = block.getWorld().generateTree(block.getLocation(), type);

        if (!grewTree) block.setTypeIdAndData(id, data, true);

        return grewTree;
    }

    public static boolean growCocoa(Block block) {
        CocoaPlant cPlant = (CocoaPlant) block.getState().getData();

        if (cPlant.getSize() != CocoaPlant.CocoaPlantSize.LARGE) {
            cPlant.setSize(CocoaPlant.CocoaPlantSize.LARGE);
            block.setTypeIdAndData(cPlant.getItemTypeId(), cPlant.getData(), true);
            return true;
        }

        return false;
    }

    public static boolean growFromStem(Block block) {
        if (block.getData() != 7) {
            block.setData(CropState.RIPE.getData());


            return true;
        }
        else return placePumpkinOrMelon(block);
    }

    private static boolean generateLargeTree(Block block, TreeType typeSmall, TreeType typeLarge) {
        int typeid = block.getTypeId();
        byte data = block.getData();

        Block[] group = getFourGroup(block);
        Block grow_location = (group == null) ? block : getNorthWestBlock(group[0], group[1], group[2], group[3]);
        TreeType type = (group == null) ? typeSmall : typeLarge;

        if (group == null) block.setType(Material.AIR);
        else for (Block mBlock : group) mBlock.setType(Material.AIR);

        if (!grow_location.getWorld().generateTree(grow_location.getLocation(), type)) {
            if (group == null) block.setTypeIdAndData(typeid, data, true);
            else for (Block mBlock : group) mBlock.setTypeIdAndData(typeid, data, true);

            return false;
        }

        return true;
    }

    private static Block[] getFourGroup(Block block) {
        Block[] blocks = {block, null, null, null};

        World world = block.getWorld();
        int x = block.getX(), y = block.getY(), z = block.getZ();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (j == 0 || i == 0) continue;
                blocks[1] = world.getBlockAt(x - i, y, z);
                blocks[2] = world.getBlockAt(x - i, y, z - j);
                blocks[3] = world.getBlockAt(x, y, z - j);

                boolean pass = true;
                for (Block mBlock : blocks) {
                    if (!(mBlock.getData() == blocks[0].getData())) {
                        pass = false;
                        break;
                    }
                }

                if (pass) return blocks;
            }
        }

        return null;
    }

    private static Block getNorthWestBlock(Block b1, Block b2, Block b3, Block b4) {
        Block grow_location = b1.getRelative(BlockFace.NORTH).equals(b2) ? b2
                : b1.getRelative(BlockFace.NORTH).equals(b3) ? b3
                : b1.getRelative(BlockFace.NORTH).equals(b4) ? b4
                : b1;
        grow_location = grow_location.getRelative(BlockFace.WEST).equals(b2) ? b2
                : grow_location.getRelative(BlockFace.WEST).equals(b3) ? b3
                : grow_location.getRelative(BlockFace.WEST).equals(b4) ? b4
                : grow_location;

        return grow_location;
    }

    private static boolean placePumpkinOrMelon(Block block) {
        World world = block.getWorld();
        BlockFace faces[] = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        Material block_type = block.getType().equals(Material.PUMPKIN_STEM) ? Material.PUMPKIN : Material.MELON_BLOCK;
        for (BlockFace face : faces) if (block.getRelative(face).getType().equals(block_type)) return false;
        for (BlockFace face : faces) {
            Block mblock = block.getRelative(face);

            if (mblock.getType().equals(Material.AIR)) {
                if (!(mblock.getRelative(BlockFace.DOWN).getType().equals(Material.DIRT)
                        || (mblock.getRelative(BlockFace.DOWN).getType().equals(Material.SOIL))
                        || (mblock.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS)))) continue;

                mblock.setType((block.getType().equals(Material.PUMPKIN_STEM) ? Material.PUMPKIN : Material.MELON_BLOCK));
                return true;
            }
        }

        return false;
    }
}
