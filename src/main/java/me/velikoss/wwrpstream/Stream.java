package me.velikoss.wwrpstream;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.Merchant;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.Color;
import java.util.Objects;
import java.util.Random;

public final class Stream extends JavaPlugin implements CommandExecutor, Listener {

    Random rnd = new Random();
    int timer;
    int afk = 600;
    Location afklocation;
    String name = "bebra????";
    public boolean STREAM = false;
    Player randomPlayer;
    Inventory lastinv;
    Player player;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("stream").setExecutor(this);
        getCommand("block").setExecutor(this);
        getCommand("unblock").setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equals("stream")){
            switch(args[0]){
                case "start":
                    STREAM = true;
                    timer = 6000;
                    if(args.length > 1){
                        player = Bukkit.getPlayer(args[1]);
                    }else{
                        player = (Player) sender;
                    }
                    assert player != null;
                    player.setGameMode(GameMode.SPECTATOR);
                    break;
                case "stop":
                    STREAM = false;
                    break;
                case "skip":
                    timer = 6000;
                    randomPlayer = (Player) Bukkit.getOnlinePlayers().toArray()[rnd.nextInt(0, Bukkit.getOnlinePlayers().size())];
                    while(randomPlayer.getGameMode().equals(GameMode.SPECTATOR) || randomPlayer.getName().equals(name) || randomPlayer.getPersistentDataContainer().has(new NamespacedKey(this, "blockme"), PersistentDataType.INTEGER)) randomPlayer = (Player) Bukkit.getOnlinePlayers().toArray()[rnd.nextInt(0, Bukkit.getOnlinePlayers().size())];
                    name = randomPlayer.getName();
                    getServer().sendMessage(Component.text(ChatColor.BOLD + "" + net.md_5.bungee.api.ChatColor.of("#7f6faf") + "[Fratch'o'Stream] " + ChatColor.RESET + "Стрим идёт от лица игрока " + randomPlayer.getName()));
                    break;
                default:
                    break;
            }
        }

        if(command.getName().equals("block")){
            if(!((Player) sender).getPersistentDataContainer().has(new NamespacedKey(this, "blockme"), PersistentDataType.INTEGER)){
                ((Player) sender).getPersistentDataContainer().set(new NamespacedKey(this, "blockme"), PersistentDataType.INTEGER, 0);
            }else{
                sender.sendMessage("Вы уже заблокировали возможность наблюдения за вами во время стрима!");
            }
        }
        if(command.getName().equals("unblock")){
            if(((Player) sender).getPersistentDataContainer().has(new NamespacedKey(this, "blockme"), PersistentDataType.INTEGER)){
                ((Player) sender).getPersistentDataContainer().remove(new NamespacedKey(this, "blockme"));
            }else{
                sender.sendMessage("Вы уже разблокировали возможность наблюдения за вами во время стрима!");
            }
        }
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if(Bukkit.getOnlinePlayers().size() == 2 && STREAM){
            timer = 6000;
            randomPlayer = (Player) Bukkit.getOnlinePlayers().toArray()[rnd.nextInt(0, Bukkit.getOnlinePlayers().size())];
            while(randomPlayer.getGameMode().equals(GameMode.SPECTATOR) || randomPlayer.getName().equals(name)) randomPlayer = (Player) Bukkit.getOnlinePlayers().toArray()[rnd.nextInt(0, Bukkit.getOnlinePlayers().size())];
            name = randomPlayer.getName();
            getServer().sendMessage(Component.text(ChatColor.BOLD + "" + net.md_5.bungee.api.ChatColor.of("#7f6faf") + "[Fratch'o'Stream] " + ChatColor.RESET + "Стрим идёт от лица игрока " + randomPlayer.getName()));
            player.resetTitle();
        }
    }

    @EventHandler
    private void moveItem(InventoryInteractEvent event){
        Player p = (Player) event.getWhoClicked();
        if (p == randomPlayer) {
            player.getOpenInventory().setCursor(p.getOpenInventory().getCursor());
            player.getInventory().setContents(randomPlayer.getInventory().getContents());
        }
    }

    @EventHandler
    private void moveItem2(InventoryDragEvent event) {
        Player p = (Player) event.getWhoClicked();
        if (p == randomPlayer) {
            player.getOpenInventory().setCursor(event.getCursor());
            player.getInventory().setContents(randomPlayer.getInventory().getContents());
        }
    }

    @EventHandler
    private void stream(ServerTickStartEvent event) {
        if(player == null) STREAM = false;
        if(afk<0){
            if(afklocation.distance(randomPlayer.getLocation()) > 10) {
                afklocation = randomPlayer.getLocation();
                afk = 600;
            }
            else {
                randomplayer();
            }
        }
        if(STREAM){
            if(randomPlayer == null || !randomPlayer.isOnline() || timer<0){
                randomplayer();
            }
            player.sendActionBar(randomPlayer.getName() + " | " + (timer/20)/60 + ":" + ((timer/20)%60<10?("0"+((timer/20)%60)):(timer/20)%60));
            player.setSpectatorTarget(randomPlayer);
            if(randomPlayer.getWorld() != player.getWorld()) {
                afklocation = randomPlayer.getLocation();
                player.teleport(randomPlayer);
                player.setSpectatorTarget(randomPlayer);
            }
            if(player.getLocation().distance(randomPlayer.getLocation())>10){
                player.teleport(randomPlayer.getLocation());
                player.setSpectatorTarget(randomPlayer);
            }
            --afk;
            --timer;
            if(lastinv != randomPlayer.getOpenInventory().getTopInventory()){
                if(randomPlayer.getOpenInventory().getTopInventory().getType().equals(InventoryType.CRAFTING)) player.closeInventory();
                else if(randomPlayer.getOpenInventory().getType().equals(InventoryType.MERCHANT)) player.openMerchant((Merchant) randomPlayer.getOpenInventory(), false);
                else player.getOpenInventory().setCursor(Objects.requireNonNull(randomPlayer.getInventory().getHolder()).getItemOnCursor());
                player.getInventory().setContents(randomPlayer.getInventory().getContents());
                lastinv = randomPlayer.getOpenInventory().getTopInventory();
                afk = 600;
            }
        }
    }

    private void randomplayer() {
        randomPlayer = (Player) Bukkit.getOnlinePlayers().toArray()[rnd.nextInt(0, Bukkit.getOnlinePlayers().size())];
        while(Bukkit.getOnlinePlayers().size() > 1 && (randomPlayer.getGameMode().equals(GameMode.SPECTATOR) || randomPlayer.getName().equals(name) || randomPlayer.getPersistentDataContainer().has(new NamespacedKey(this, "blockme"), PersistentDataType.INTEGER))) randomPlayer = (Player) Bukkit.getOnlinePlayers().toArray()[rnd.nextInt(0, Bukkit.getOnlinePlayers().size())];
        name = randomPlayer.getName();
        if(Bukkit.getOnlinePlayers().size() == 1){
            player.sendTitle("Пока на сервере никого нет","Ждём людей на нашем сервере! https://discord.gg/NtVEGsj",0,99999999, 0);
        }else{
            player.resetTitle();
            timer = 6000;
            afk = 600;
            afklocation = randomPlayer.getLocation();
            getServer().sendMessage(Component.text(ChatColor.BOLD + "" + net.md_5.bungee.api.ChatColor.of("#7f6faf") + "[Fratch'o'Stream] " + ChatColor.RESET + "Стрим идёт от лица игрока " + randomPlayer.getName()));
        }
    }
}
