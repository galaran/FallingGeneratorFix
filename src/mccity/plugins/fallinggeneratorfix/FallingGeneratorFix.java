package mccity.plugins.fallinggeneratorfix;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FallingGeneratorFix extends JavaPlugin implements Listener {

    /** Piston Block -> Mark (last non-canceled extend) tick */
    private final Map<Block, Long> markedPistons = new HashMap<Block, Long>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (checkMark(event)) return;
        if (checkDragonEgg(event, true)) return;

        Block piston = event.getBlock();
        List<Block> movingBlocks = event.getBlocks();
        for (Block curBlock : movingBlocks) {
            Material curMat = curBlock.getType();
            if (curMat == Material.SAND || curMat == Material.GRAVEL) {
                markedPistons.put(piston, piston.getWorld().getFullTime());
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (checkMark(event)) return;
        checkDragonEgg(event, false);
    }

    /**
     * @return piston event was canceled
     */
    private boolean checkMark(BlockPistonEvent event) {
        Block piston = event.getBlock();
        Long markTick = markedPistons.get(piston);
        if (markTick != null) {
            if (piston.getWorld().getFullTime() > markTick + 2) {
                markedPistons.remove(piston);
            } else {
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    /**
     * @return piston event was canceled
     */
    private boolean checkDragonEgg(BlockPistonEvent event, boolean isExtend) {
        Block pistonBlock = event.getBlock();
        BlockFace direction = isExtend ? BlockFace.UP : BlockFace.DOWN;
        if (event.getDirection() == direction && pistonBlock.getType() == Material.PISTON_STICKY_BASE) {
            // check for dragon egg block up to piston block
            // [piston_base] - [air] - [solid_block] - [dragon_egg] duplicator design
            if (pistonBlock.getRelative(0, 3, 0).getType() == Material.DRAGON_EGG) {
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }
}
