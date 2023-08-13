package fr.utarwyn.skypvp;

import com.utaria.utariaapi.managers.FlagManager;
import com.utaria.utariaapi.managers.FlagManager.Flag;
import com.utaria.utariaapi.managers.NPCManager;
import com.utaria.utariaapi.managers.TutorialManager;
import com.utaria.utariaapi.managers.ZoneManager;
import com.utaria.utariaapi.utils.PlayerUtils;
import fr.utarwyn.skypvp.commands.SpawnCommand;
import fr.utarwyn.skypvp.listeners.PlayerListener;
import fr.utarwyn.skypvp.listeners.SignListener;
import fr.utarwyn.skypvp.managers.PadsManager;
import fr.utarwyn.skypvp.tasks.DeathHelpTask;
import fr.utarwyn.skypvp.tasks.PadsTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;

public class SkyPvp extends JavaPlugin {

    private static SkyPvp instance;

    public void onEnable(){
        instance = this;

        paramFlags();

        // Commands
        getCommand("spawn").setExecutor(new SpawnCommand());

        // Listeners
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // Tasks
        new DeathHelpTask();
        new PadsTask();
    }
    public void onDisable(){

    }



    private void paramFlags(){
        // World Flags
        FlagManager.setFlag(FlagManager.FlagType.WEATHER, FlagManager.FlagValue.SUN);
        FlagManager.setFlag(FlagManager.FlagType.ANIMAL_SPAWNING, FlagManager.FlagValue.DENY);
        FlagManager.setFlag(FlagManager.FlagType.MOB_SPAWNING, FlagManager.FlagValue.DENY);
        FlagManager.setFlag(FlagManager.FlagType.ICE_FORM, FlagManager.FlagValue.DENY);
        FlagManager.setFlag(FlagManager.FlagType.ICE_MELT, FlagManager.FlagValue.DENY);
        FlagManager.setFlag(FlagManager.FlagType.SNOW_MELT, FlagManager.FlagValue.DENY);

        FlagManager.setFlag(FlagManager.FlagType.ANIMAL_DAMAGE, FlagManager.FlagValue.DENY);

        // Player Flags
        FlagManager.setFlag(FlagManager.FlagType.BLOCK_BREAK, FlagManager.FlagValue.DENY);
        FlagManager.setFlag(FlagManager.FlagType.BLOCK_PLACE, FlagManager.FlagValue.DENY);
        FlagManager.setFlag(FlagManager.FlagType.AUTO_RESPAWN, FlagManager.FlagValue.ALLOW);

        World defaultWorld = Bukkit.getWorld("world");
        Flag pvpFlag = new Flag(FlagManager.FlagType.PVP, FlagManager.FlagValue.DENY);

        ZoneManager.createSquareZone(
                new Location(defaultWorld, 12.0, 0, -12.0),
                new Location(defaultWorld, -12.0, 256, 12.0),
                Collections.singletonList(pvpFlag)
        );

        setupTutorial();

        createPads();
        createNPCs();
    }
    private void createPads(){
        PadsManager.createPadAt(new Location(getServer().getWorld("world"), 41, 105, -38), new PotionEffect(PotionEffectType.JUMP, 0, 0));
        PadsManager.createPadAt(new Location(getServer().getWorld("world"), 45, 134, 4), new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 0, 1));
        PadsManager.createPadAt(new Location(getServer().getWorld("world"), -60, 113, -29), new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 0, 1));
    }
    private void createNPCs(){
        final NPCManager.NPC npc = NPCManager.spawnNPCAt(new Location(Config.spawn.getWorld(), 7.5, 113, 7.5, 135, 2), "tuto", "God");

        npc.onClick(new Runnable() {
            @Override
            public void run() {
                Player player = npc.getLastPlayerWhoClicked();
                TutorialManager.startTutorialFor("SkyPvp", player);
            }
        });
    }
    private void setupTutorial(){
        TutorialManager.Tutorial tuto = TutorialManager.createTutorial("SkyPvp");

        tuto.newStep().delay(9000).location(new Location(getServer().getWorld("world"), 50, 140, -41, 52, 25)).lines(Arrays.asList(
                "§aBienvenue sur le serveur §a§lSkyPvp§r§a d'§b§lUtaria§r§a !",
                " ",
                "§eLe §6SkyPvp§e est un jeu original dans lequel vous devez",
                "§ecombattre les autres joueurs pour devenir le meilleur."
        ));
        tuto.newStep().delay(8000).location(new Location(getServer().getWorld("world"), 18, 112, 33, 118, 2)).lines(Arrays.asList(
                "§aDes parcours variés vous attendent !",
                " ",
                "§eSautez de plateforme en plateforme et",
                "§eévitez de tomber dans le vide !"
        ));
        tuto.newStep().delay(9000).location(new Location(getServer().getWorld("world"), -18, 120.8, -67.5, 90, 0)).lines(Arrays.asList(
                "§eRécupérez des armures, des armes et des §9§litems spéciaux",
                "§een cliquant sur les panneaux §5§l[Item]",
                "§ecachés un peu partout sur la carte !"
        ));
        tuto.newStep().delay(9000).location(new Location(getServer().getWorld("world"), -64.1, 123.1, -34.4, -39, 56)).lines(Arrays.asList(
                "§eDes plateformes spéciales vous permettent",
                "§ede gagner des bonus !",
                "§2Restez sur la vitre de couleur et",
                "§2vous obtiendrez des effets §a§lpositifs§2 !"
        ));
        tuto.newStep().delay(8000).location(new Location(getServer().getWorld("world"), -33.3, 132.5, 26.1, -134, 32)).lines(Arrays.asList(
                "§e§lBonne chance !",
                " ",
                "§dDevenez le meilleur, et amusez-vous bien",
                "§ddans notre mode de jeu §6§lSkyPvp§r§d !"
        ));

        tuto.whenFinished(new TutorialManager.PlayerFinishedCallback() {

            @Override
            public void run(Player player, TutorialManager.Tutorial tutorial) {
                if(TutorialManager.isFirstFinished(tutorial.getName(), player)){
                    SkyPvpPlayer.get(player).addPoints(50);
                    PlayerUtils.sendMessage(player, "Vous avez gagné §6+50 points§7 en finissant le tutoriel.");
                }
            }

        });
    }



    public static SkyPvp getInstance(){
        return instance;
    }

}
