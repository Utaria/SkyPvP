package fr.utarwyn.skypvp;

import com.utaria.utariaapi.database.Database;
import com.utaria.utariaapi.database.DatabaseSet;
import com.utaria.utariaapi.utils.ScoreboardUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkyPvpPlayer {

    private static List<SkyPvpPlayer> skyPvpPlayers = new ArrayList<>();
    private Player player;

    private double points = 0.0;
    private int kills = 0;
    private int deaths = 0;

    private boolean exists;

    public SkyPvpPlayer(Player player){
        this.player = player;
        this.refreshData();
    }


    public Player getBukkitPlayer(){
        return this.player;
    }


    public double getPoints(){
        return this.points;
    }
    public int getKills(){
        return this.kills;
    }
    public int getDeaths(){
        return this.deaths;
    }

    public void addPoints(double points){
        this.points += points;

        ScoreboardUtils.getScoreboardOf(getBukkitPlayer()).animateLineTo(2, getPoints());
        this.saveData();
    }
    public void addKill(){
        kills++;

        ScoreboardUtils.getScoreboardOf(getBukkitPlayer()).setLine(5, "§2§e§l" + getKills());
        this.saveData();
    }
    public void addDeath(){
        deaths++;

        ScoreboardUtils.getScoreboardOf(getBukkitPlayer()).setLine(8, "§3§e§l" + getDeaths());
        this.saveData();
    }



    private void refreshData(){
        DatabaseSet set = new Database().findFirst(Config.playersTable, DatabaseSet.makeConditions("uuid", getBukkitPlayer().getUniqueId().toString()));

        if(set != null){
            points = set.getDouble("points");
            kills  = set.getInteger("kills");
            deaths = set.getInteger("deaths");

            exists = true;
        }
    }
    private void saveData(){
        Bukkit.getScheduler().runTaskAsynchronously(SkyPvp.getInstance(), new Runnable() {
            @Override
            public void run() {
                Map<String, String> conditions = null;

                if(exists)
                    conditions = DatabaseSet.makeConditions("uuid", getBukkitPlayer().getUniqueId().toString());

                new Database().save(Config.playersTable, DatabaseSet.makeFields(
                        "playername", getBukkitPlayer().getName(),
                        "uuid", getBukkitPlayer().getUniqueId().toString(),
                        "points", getPoints(),
                        "kills", getKills(),
                        "deaths", getDeaths()
                ), conditions);
            }
        });
    }


    public static SkyPvpPlayer get(Player player){
        for(SkyPvpPlayer sPlayer : skyPvpPlayers){
            if(sPlayer.getBukkitPlayer() != null && sPlayer.getBukkitPlayer().getUniqueId().equals(player.getUniqueId()))
                return sPlayer;
        }

        SkyPvpPlayer newSPlayer = new SkyPvpPlayer(player);
        skyPvpPlayers.add(newSPlayer);
        return newSPlayer;
    }

}
