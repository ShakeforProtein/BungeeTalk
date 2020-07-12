package me.shakeforprotein.bungeetalk.Commands;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import me.shakeforprotein.bungeetalk.Manager.GameManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandDisableChatGames extends Command {

    private BungeeTalk pl;

    public CommandDisableChatGames(BungeeTalk main) {
        super("disablechatgames");
        this.pl = main;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (pl.registeredGames.size() > 0) {
            GameManager gameManager = pl.registeredGames.get(0);
            gameManager.mute(commandSender);
        }
    }
}
