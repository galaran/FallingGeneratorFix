package mccity.plugins.fallinggeneratorfix;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FallingGeneratorFix extends JavaPlugin implements Listener {

    private final Map<Block, Long> markedPistons = new HashMap<Block, Long>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (checkMarked(event)) return;

        Block piston = event.getBlock();

        List<Block> movingBlocks = event.getBlocks();
        for (Block curBlock : movingBlocks) {
            Material curMat = curBlock.getType();
            if (curMat == Material.SAND || curMat == Material.GRAVEL) {
                markedPistons.put(piston, piston.getWorld().getFullTime());
                return;
            } else if (curMat == Material.DRAGON_EGG) {
            	event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        checkMarked(event);
    }

    private boolean checkMarked(BlockPistonEvent event) {
        Block piston = event.getBlock();
        Long markTick = markedPistons.get(piston);
        if (markTick != null) {
            if (piston.getWorld().getFullTime() > markTick) {
                markedPistons.remove(piston);
            } else {
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }
}
