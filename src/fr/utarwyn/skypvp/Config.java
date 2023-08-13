package fr.utarwyn.skypvp;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Config {

    public static Location spawn = new Location(Bukkit.getWorld("world"), 0.5, 115.5, 0.5, -90, 1);
    public static int tpCooldown = 10; // In seconds

    public static int deathY = 60;

    public static String playersTable = "skypvp_players";

    public static Integer padDelay     = 4; // In seconds
    public static Integer padBonusTime = 30; // In seconds

}
