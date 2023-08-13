package fr.utarwyn.skypvp.commands;

import com.utaria.utariaapi.managers.AdminModeManager;
import com.utaria.utariaapi.managers.TaskManager;
import com.utaria.utariaapi.utils.PlayerUtils;
import com.utaria.utariaapi.utils.ScoreboardUtils;
import fr.utarwyn.skypvp.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        final Player player = (Player) sender;

        int cooldown = AdminModeManager.hasAdminMode(player) ? 0 : Config.tpCooldown;
        if(cooldown > 0) PlayerUtils.sendMessage(player, "Téléportation dans §6" + cooldown + " secondes§7.");

        Runnable task = new Runnable() {
            @Override
            public void run() {
                player.teleport(Config.spawn);
                PlayerUtils.sendSuccessMessage(player, "Vous avez été téléporté au spawn.");
            }
        };

        if(cooldown == 0){
            task.run();
            return true;
        }

        TaskManager.runTaskLaterWithoutMoving(player, cooldown * 20L, task, "Vous avez bougé, la téléportation a été annulée.");

        return true;
    }
}
