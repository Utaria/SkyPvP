package fr.utarwyn.skypvp.tasks;

import fr.utarwyn.skypvp.SkyPvp;
import fr.utarwyn.skypvp.managers.PadsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PadsTask implements Runnable {

    public PadsTask(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SkyPvp.getInstance(), this, 0, 20L);
    }


    @Override
    public void run(){
        // Update all pads
        for(PadsManager.Pad pad : PadsManager.getPads())
            pad.update();

        // Check if a player is on a pad & add it
        for(Player player : Bukkit.getOnlinePlayers()){
            PadsManager.Pad pad = PadsManager.getPadOnPlayer(player);

            if(pad != null && !pad.playerIsOn(player)) pad.addPlayer(player);
        }
    }

}
