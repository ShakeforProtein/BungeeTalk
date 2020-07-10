package me.shakeforprotein.bungeetalk.Listeners;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LaunchListener implements Listener {

    private BungeeTalk pl;
    private boolean tasksRunning = false;

    public LaunchListener(BungeeTalk main){
        this.pl = main;
    }

    @EventHandler
    public void ChatEvent(ChatEvent e){
        if(!tasksRunning){
            System.out.println("Delayed tasks not initiated");
            tasksRunning = true;
            pl.startRunnables();
        }
    }
}
