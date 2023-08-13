package fr.utarwyn.skypvp.tasks;

import fr.utarwyn.skypvp.Config;
import fr.utarwyn.skypvp.SkyPvp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DeathHelpTask implements Runnable {

    public DeathHelpTask(){
        Bukkit.getScheduler().runTaskTimer(SkyPvp.getInstance(), this, 0, 4L);
    }

    @Override
    public void run(){
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.getLocation().getY() > Config.deathY) continue;

            if(player.getHealth() >= 2){
                player.setHealth(player.getHealth() - 2);
            }
        }
    }

}
