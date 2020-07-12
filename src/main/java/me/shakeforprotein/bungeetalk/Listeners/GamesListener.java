package me.shakeforprotein.bungeetalk.Listeners;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import me.shakeforprotein.bungeetalk.Manager.GameManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class GamesListener implements Listener {

    private BungeeTalk pl;

    public GamesListener(BungeeTalk main){
        this.pl = main;
        System.out.println("Registered Game Listener");
    }

    @EventHandler
    public void onChat(ChatEvent e){
        boolean postMessage = true;
        if(e.getSender() instanceof ProxiedPlayer && e.getMessage().length() > 0) {
            for (GameManager gameManager : pl.registeredGames) {
                if(gameManager.checkGame((ProxiedPlayer) e.getSender(), e.getMessage())){
                    postMessage = false;
                }
            }
        }
        if(!postMessage){
            if(e.getSender() instanceof ProxiedPlayer){
                pl.getUuidCache().set("Players." +((ProxiedPlayer) e.getSender()).getUniqueId().toString(), ((ProxiedPlayer) e.getSender()).getName());
            }
            e.setCancelled(true);
        }
    }
}
