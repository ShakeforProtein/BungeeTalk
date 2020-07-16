package me.shakeforprotein.bungeetalk.Listeners;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static me.shakeforprotein.bungeetalk.Enums.ProtocolVersion.matchVersion;

public class LaunchListener implements Listener {

    private BungeeTalk pl;
    private boolean tasksRunning = false;

    public LaunchListener(BungeeTalk main) {
        this.pl = main;
    }
    private HashMap<ProxiedPlayer, ServerInfo> transferHash = new HashMap<>();

    @EventHandler
    public void ChatEvent(ChatEvent e) {
        if (!tasksRunning) {
            System.out.println("Delayed tasks not initiated");
            tasksRunning = true;
            pl.startTasks();
        }
    }

    @EventHandler
    public void PlayerJoin(LoginEvent e) {
        if (e.getConnection().isOnlineMode()) {
            for(ProxiedPlayer p : pl.getProxy().getPlayers()){
                p.sendMessage(ChatMessageType.CHAT, getJoinMessage(e.getConnection().getName(), e.getConnection().getUniqueId(), e.getConnection().getVersion()));
            }
            pl.getUuidCacheFull().set("Players." + e.getConnection().getUniqueId().toString(), e.getConnection().getName());
        } else{
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerLeave(PlayerDisconnectEvent e){
        String playerName = e.getPlayer().getDisplayName();
        UUID uuid = e.getPlayer().getUniqueId();
        for(ProxiedPlayer p : pl.getProxy().getPlayers()){
            p.sendMessage(ChatMessageType.CHAT, getLeaveMessage(playerName, uuid));
        }
    }

    @EventHandler
    public void serverTransferEvent(ServerConnectedEvent e){
        transferHash.put(e.getPlayer(), e.getServer().getInfo());
        pl.getProxy().getScheduler().schedule(pl, () -> {
            if(transferHash.keySet().contains(e.getPlayer())){
                transferHash.remove(e.getPlayer());
            }
        }, 2, TimeUnit.SECONDS);
    }

    @EventHandler
    public void serverDisconnect(ServerDisconnectEvent e){
        ProxiedPlayer p = e.getPlayer();
        ServerInfo serverInfo = e.getTarget();
        if(transferHash.containsKey(e.getPlayer())){
            pl.getProxy().broadcast(getTransferMessage(p.getDisplayName(), serverInfo.getName(), transferHash.get(e.getPlayer()).getName()));
        }
    }


    private TextComponent getJoinMessage(String playerName, UUID uuid, int version){
        TextComponent output = new TextComponent("");
        String inputString;
        inputString = pl.getConfig().getStringList("General.Messages.JoinMessages").get(ThreadLocalRandom.current().nextInt(0, pl.getConfig().getStringList("General.Messages.JoinMessages").size()));
        inputString = inputString.replace("%playerName%",playerName);
        inputString = inputString.replace("%uuid%", uuid.toString());
        inputString = inputString.replace("%version%",  matchVersion(version) + "");
        output.setText(ChatColor.translateAlternateColorCodes('&', inputString));
        return output;
    }

    private TextComponent getLeaveMessage(String playerName, UUID uuid){
        TextComponent output = new TextComponent("");
        String inputString;
        inputString = pl.getConfig().getStringList("General.Messages.LeaveMessages").get(ThreadLocalRandom.current().nextInt(0, pl.getConfig().getStringList("General.Messages.JoinMessages").size()));
        inputString = inputString.replace("%playerName%",playerName);
        inputString = inputString.replace("%uuid%", uuid.toString());
        output.setText(ChatColor.translateAlternateColorCodes('&', inputString));
        return output;
    }

    private TextComponent getTransferMessage(String playerName, String fromServer, String toServer){
        TextComponent output = new TextComponent("");
        String inputString;
        inputString = pl.getConfig().getStringList("General.Messages.ServerTransferMessages").get(ThreadLocalRandom.current().nextInt(0, pl.getConfig().getStringList("General.Messages.JoinMessages").size()));
        inputString = inputString.replace("%playerName%",playerName);
        inputString = inputString.replace("%fromServer%", fromServer);
        inputString = inputString.replace("%toServer%", toServer);
        output.setText(ChatColor.translateAlternateColorCodes('&', inputString));
        return output;
    }


}
