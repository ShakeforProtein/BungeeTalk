package me.shakeforprotein.bungeetalk.Commands;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandToggleAnnouncer extends Command {

    private BungeeTalk pl;

    public CommandToggleAnnouncer(BungeeTalk main) {
        super("toggeleannouncer");
        this.pl = main;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        pl.announcer.mute(commandSender);
    }
}
