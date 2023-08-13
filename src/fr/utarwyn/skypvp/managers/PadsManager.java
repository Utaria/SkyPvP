package fr.utarwyn.skypvp.managers;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle;
import com.utaria.utariaapi.managers.AdminModeManager;
import com.utaria.utariaapi.managers.ParticleManager;
import com.utaria.utariaapi.utils.MathUtils;
import com.utaria.utariaapi.utils.PlayerUtils;
import fr.utarwyn.skypvp.Config;
import fr.utarwyn.skypvp.SkyPvp;
import fr.utarwyn.skypvp.SkyPvpPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PadsManager {

    private static List<Pad> pads = new ArrayList<>();


    public static Pad createPadAt(Location location, PotionEffect effect){
        if(existsPadAt(location)) return getPadAt(location);
        Pad pad = new Pad(location, effect);

        pads.add(pad);
        return pad;
    }
    public static boolean existsPadAt(Location location){
        return (getPadAt(location) != null);
    }
    public static Pad getPadAt(Location location){
        for(Pad pad : getPads()){
            if(pad.getPosition().equals(location))
                return pad;
        }

        return null;
    }
    public static List<Pad> getPads(){
        return pads;
    }

    public static Pad getPadOnPlayer(Player player){
        for(Pad pad : getPads()){
            if(pad.isAtGoodCoords(player)) return pad;
        }

        return null;
    }


    public static class Pad{
        private List<UUID> playersOnIt = new ArrayList<>();

        private Location location;
        private PotionEffectType effectType;
        private Integer effectAmplifier;
        private Integer state = 0;
        private Integer gives = 0;

        private UUID lastBeneficiary;


        public Pad(Location location, PotionEffect effect){
            this.location   = location;
            this.effectType = effect.getType();
            this.effectAmplifier = effect.getAmplifier();
        }


        public Location getPosition(){
            return this.location;
        }
        public boolean isUsed(){
            return this.playersOnIt.size() == 0;
        }
        public Player getBeneficiary(){
            return (this.playersOnIt.size() > 0 && Bukkit.getPlayer(this.playersOnIt.get(0)) != null) ? Bukkit.getPlayer(this.playersOnIt.get(0)) : null;
        }
        public boolean playerIsOn(Player player){
            return this.playersOnIt.contains(player.getUniqueId());
        }
        public Integer getState(){
            return this.state;
        }
        public List<UUID> getPlayersOnIt(){
            return this.playersOnIt;
        }

        public boolean isAtGoodCoords(Player player){
            if(this.location == null) return false;
            Location location = player.getLocation();
            boolean valideY   = MathUtils.inRange(location.getBlockY(), this.location.getBlockY(), this.location.getBlockY() + 3);

            return (location.getBlockX() == this.location.getBlockX() && location.getBlockZ() == this.location.getBlockZ() && valideY);
        }
        public void addPlayer(Player player){
            UUID uuid = player.getUniqueId();

            // Don't add player if he is in AdminMode
            if(AdminModeManager.hasAdminMode(player)) return;

            // Send welcome message & sound
            PlayerUtils.sendMessage(player, "Vous entrez sur une plateforme de §9" + this.getFrenchName() + ".");
            if(this.getPlayersOnIt().size() == 0) player.playSound(getPosition(), Sound.CAT_MEOW, 1f, 1f);

            if(this.playersOnIt.contains(uuid)) this.playersOnIt.remove(uuid);
            this.playersOnIt.add(uuid);
        }
        public void removePlayer(Player player){
            UUID uuid = player.getUniqueId();

            if(this.playersOnIt.contains(uuid))
                this.playersOnIt.remove(uuid);
        }

        public void update(){
            // Update effects (not very important but cool)
            this.updateEffects();

            // Remove in the cache all players out of the pad
            Iterator<UUID> iter = this.playersOnIt.iterator();
            while(iter.hasNext()){
                UUID uuid = iter.next();

                if(Bukkit.getPlayer(uuid) == null || !this.isAtGoodCoords(Bukkit.getPlayer(uuid)))
                    iter.remove();
            }

            // Pause pad & reset it if there are two players on pad (or nobody)
            if(this.getPlayersOnIt().size() > 1 || this.getBeneficiary() == null){
                this.state = 0;
                this.gives = 0;
                return;
            }

            // Reset pad if there is another player (or nobody)
            if(this.lastBeneficiary != null && !this.lastBeneficiary.equals(this.getBeneficiary().getUniqueId())){
                this.state = 0;
                this.gives = 0;
            }

            // Give bonus on good state
            if(this.state.equals(Config.padDelay - 1)){
                this.giveBonus();

                this.gives++;
                this.state = 0;
                this.lastBeneficiary = this.getBeneficiary().getUniqueId();

                return;
            }else{
                // Send sound
                ParticleManager.displayParticleAt(ParticleManager.ParticleEffect.SPELL, getPosition().clone().add(.5, .5, .5), 120, .1f, .5f, .5f, .5f);
                this.getBeneficiary().getWorld().playSound(getPosition(), Sound.NOTE_BASS, 1f, 1f);
            }

            // Update pad state
            this.lastBeneficiary = this.getBeneficiary().getUniqueId();
            this.state++;
        }
        private void updateEffects(){
            if(this.getPlayersOnIt().size() == 0)
                ParticleManager.displayParticleAt(ParticleManager.ParticleEffect.VILLAGER_HAPPY, getPosition().clone().add(.5, 1, .5), 4, 0, .4f, .4f, .4f);
            ParticleManager.displayParticleCircleAt(ParticleManager.ParticleEffect.FIREWORKS_SPARK, getPosition().clone().add(.5, 1, .5), 20, 0f, 6, 300);
        }
        private void giveBonus(){
            if(this.effectType == null) return;
            Player beneficiary = this.getBeneficiary();

            // Get current effect duration & remove it from player
            int baseDuration = 0;
            for(PotionEffect potion : beneficiary.getActivePotionEffects()){
                if(potion.getType().equals(this.effectType)){
                    baseDuration = potion.getDuration();
                    beneficiary.removePotionEffect(potion.getType());
                }
            }

            // Add the effect with the new calculated duration to the player
            beneficiary.addPotionEffect(new PotionEffect(this.effectType, baseDuration + Config.padBonusTime * 20, this.effectAmplifier));

            // Give a half point to the player
            SkyPvpPlayer.get(beneficiary).addPoints(0.5);

            // Play particle animation
            for(int i = 0; i < 150; i++)
                ParticleManager.displayParticleAt(ParticleManager.ParticleEffect.VILLAGER_HAPPY, getPosition().clone().add(.5, 5 + i, .5), 10, 0, .4f, .4f, .4f, 200);

            // Send message & sound to player
            PlayerUtils.sendMessage(beneficiary, "Plateforme : §6+ " + Config.padBonusTime + "s§7 de " + this.getFrenchName() + ". §e(+ 0.5 point)");
            beneficiary.getWorld().playSound(getPosition(), Sound.LEVEL_UP, 1f, 1f);
        }

        private String getFrenchName(){
            switch(this.effectType.getName()){
                default: return "Vide";
                case "JUMP": return "§aSaut";
                case "SPEED": return "§dVitesse";
                case "DAMAGE_RESISTANCE": return "§8Résistance";
                case "FIRE_RESISTANCE": return "§6Résistance au feu";
            }
        }
        public String toString(){
            return "{Pad (players=" + Arrays.toString(this.playersOnIt.toArray()) + " state=" + this.state + " gives= " + this.gives + ")}";
        }

    }

}
