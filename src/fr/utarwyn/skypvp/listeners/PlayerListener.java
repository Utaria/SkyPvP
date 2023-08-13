package fr.utarwyn.skypvp.listeners;

import com.utaria.utariaapi.managers.AdminModeManager;
import com.utaria.utariaapi.utils.MathUtils;
import com.utaria.utariaapi.utils.PlayerUtils;
import com.utaria.utariaapi.utils.ScoreboardUtils;
import fr.utarwyn.skypvp.Config;
import fr.utarwyn.skypvp.SkyPvp;
import fr.utarwyn.skypvp.SkyPvpPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class PlayerListener implements Listener {

    public PlayerListener(){
        for(Player player : Bukkit.getOnlinePlayers())
            this.applyScoreboard(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        final Player player = e.getPlayer();

        e.setRespawnLocation(Config.spawn);

        if(!AdminModeManager.hasAdminMode(player)) {
            Bukkit.getScheduler().runTaskLater(SkyPvp.getInstance(), new Runnable() {
                @Override
                public void run() {
                    preparePlayer(player);
                }
            }, 20L);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        final Player player = e.getPlayer();

        if(!player.hasPlayedBefore()) {
            Bukkit.getScheduler().runTaskLater(SkyPvp.getInstance(), new Runnable() {
                @Override
                public void run() {
                    preparePlayer(player);
                }
            }, 20L);
        }

        // Scoreboard
        this.applyScoreboard(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        Player killer = player.getKiller();


        if(killer == null){
            SkyPvpPlayer.get(player).addDeath();
            PlayerUtils.sendErrorMessage(player, "Vous êtes mort.");
            return;
        }

        boolean shoted = (killer.getItemInHand() != null && killer.getItemInHand().getType().equals(Material.BOW));

        // Killer section
        String message     = (shoted) ? "Vous avez tué §6" + player.getName() + "§7 à l'arc." : "Vous avez tué §6" + player.getName() + "§7.";
        double pointsToAdd = MathUtils.round(MathUtils.getRandomDouble(5, 10), 2);

        SkyPvpPlayer.get(killer).addPoints(pointsToAdd);
        SkyPvpPlayer.get(killer).addKill();
        PlayerUtils.sendMessage(killer, message + "§e (+" + pointsToAdd + " points)");

        // Death player section
        SkyPvpPlayer.get(player).addDeath();
        PlayerUtils.sendErrorMessage(player, "Vous avez été tué par §6" + killer.getName() + "§c.");
    }



    private void preparePlayer(Player player){
        ItemStack axe = new ItemStack(Material.STONE_AXE, 1);
        ItemStack bow = new ItemStack(Material.BOW, 1);
        ItemStack arr = new ItemStack(Material.ARROW, 64);
        ItemStack app = new ItemStack(Material.APPLE, 32);

        player.getInventory().setItem(0, axe);
        player.getInventory().setItem(1, bow);
        player.getInventory().setItem(2, arr);
        player.getInventory().setItem(8, app);

        player.setGameMode(GameMode.ADVENTURE);
    }
    private void applyScoreboard(final Player player){
        Bukkit.getScheduler().runTaskLater(SkyPvp.getInstance(), new Runnable() {
            @Override
            public void run() {
                SkyPvpPlayer sPlayer = SkyPvpPlayer.get(player);

                ScoreboardUtils.sendScoreboard(player, " §r §r §r §f§lSKYPVP §r §r §r ", Arrays.asList("§a", "§a§lPoints:", "§1§e§l" + MathUtils.round(sPlayer.getPoints(), 2), "§b", "§a§lTués:", "§2§e§l" + sPlayer.getKills(), "§1", "§a§lMorts:", "§3§e§l" + sPlayer.getDeaths(), "§2", "§bmc.utaria.fr"));
            }
        }, 20L);
    }

}
