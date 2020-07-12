package me.shakeforprotein.bungeetalk.Manager;

import com.google.common.primitives.Chars;
import me.shakeforprotein.bungeetalk.BungeeTalk;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private BungeeTalk pl;
    private boolean enabled;
    private Configuration gameConfiguration;
    private Configuration muteFile;
    private String badge;
    private List<String> rewards;
    private List<String> wordList = new ArrayList<>();
    private String gameMode;
    private String winMessage;
    private String noWinnerMessage;
    private String answer = "";
    private String leaderBoardFile;
    private boolean gameIsRunning = false;
    private long gameStarted = 0;
    private long gameLength = System.currentTimeMillis();
    private String winner = "";
    private String word = "";
    private long startDelay;
    private long frequency;
    private ScheduledTask newRoundTimer;
    private ScheduledTask currentRoundTimer;


    public GameManager(BungeeTalk main, String gameMode) {
        this.pl = main;
        this.gameMode = gameMode;
        pl.saveDefaultResource(pl.getConfig().getString("Games." + gameMode + ".Filename"));
        this.gameConfiguration = pl.loadYaml(pl.getConfig().getString("Games." + gameMode + ".Filename"));
        this.enabled = pl.getConfig().getBoolean("Games." + gameMode + ".Settings.Enabled");
        this.badge = pl.getConfig().getString("Games." + gameMode + ".Messages.Badge");
        this.winMessage = pl.getConfig().getString("Games." + gameMode + ".Messages.Winner");
        this.noWinnerMessage = pl.getConfig().getString("Games." + gameMode + ".Messages.NoWinner");
        this.rewards = pl.getConfig().getStringList("Games." + gameMode + ".Rewards");
        this.leaderBoardFile = pl.getConfig().getString("Games." + gameMode + ".leaderBoardFile");
        this.frequency = pl.getConfig().getLong("Games." + gameMode + ".Settings.Frequency");
        this.startDelay = pl.getConfig().getLong("Games." + gameMode + ".Settings.StartOffset");
        this.muteFile = pl.loadYaml(pl.getConfig().getString("General.GamesMuteFile"));
        if (gameConfiguration != null && gameConfiguration.get("Phrases") != null) {
            this.wordList = gameConfiguration.getStringList("Phrases");
        } else {
            pl.getLogger().warning("Could not load word list for GameMode:" + gameMode);
        }
        System.out.println("Registered Manager for: " + gameMode);
    }

    public boolean checkGame(ProxiedPlayer sender, String message) {
        if (enabled && gameIsRunning) {
            if (message.equalsIgnoreCase(answer)) {
                winner = sender.getName();
                gameLength = System.currentTimeMillis() - gameStarted;
                pl.rewardPlayer(this, sender.getName());
                updateStatistic("Wins");
                updateStatistic("TopSpeed");
                announceWinner(this);
                gameIsRunning = false;
                pl.saveYaml(pl.getUuidCache(), "uuidCache.yml");
                return true;
            }
        }
        return false;
    }

    public String getGameMode() {
        return gameMode;
    }

    private String getBadge() {
        return badge;
    }

    public List<String> getRewards() {
        return this.rewards;
    }

    private String getAnswer() {
        return answer;
    }

    private void setAnswer(String answer) {
        this.answer = answer;
    }

    private boolean isGameRunning() {
        return gameIsRunning;
    }

    private void setGameRunning(boolean gameIsRunning) {
        this.gameIsRunning = gameIsRunning;
    }

    private void setGameStarted(long gameStarted) {
        this.gameStarted = gameStarted;
    }

    private long getGameLength() {
        return gameLength;
    }

    private String getWinner() {
        return winner;
    }

    private String getWord() {
        return word;
    }

    private void setWord(String word) {
        this.word = word;
    }

    private String getWinMessage() {
        return winMessage;
    }

    private String getNoWinnerMessage() {
        return noWinnerMessage;
    }

    private String getLeaderBoardFile() {
        return leaderBoardFile;
    }

    private List<String> getWordList() {
        return this.wordList;
    }

    public void setGameConfig(Configuration newConfig) {
        this.gameConfiguration = newConfig;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void start() {
        newRoundTimer = pl.getProxy().getScheduler().schedule(pl, () -> {
            if (enabled) {
                if (ProxyServer.getInstance().getPlayers().size() >= pl.getConfig().getInt("Games." + gameMode + ".Settings.MinimumPlayers")) {
                    runGame();
                }
            } else {
                newRoundTimer.cancel();
            }
        }, startDelay, frequency, TimeUnit.SECONDS);
    }

    private void stop() {
        if (newRoundTimer != null) {
            newRoundTimer.cancel();
        }
    }

    public void reload() {
        setGameConfig(pl.loadYaml(pl.getConfig().getString("Games." + gameMode + ".Filename")));
        this.badge = pl.getConfig().getString("Games." + gameMode + ".Messages.Badge");
        this.winMessage = pl.getConfig().getString("Games." + gameMode + ".Messages.Winner");
        this.noWinnerMessage = pl.getConfig().getString("Games." + gameMode + ".Messages.NoWinner");
        this.rewards = pl.getConfig().getStringList("Games." + gameMode + ".Rewards");
        this.leaderBoardFile = pl.getConfig().getString("Games." + gameMode + ".LeaderBoardFile");
        this.gameConfiguration = pl.loadYaml(pl.getConfig().getString("Games." + gameMode + ".Filename"));
        this.frequency = pl.getConfig().getLong("Games." + gameMode + ".Settings.Frequency");
        this.startDelay = pl.getConfig().getLong("Games." + gameMode + ".Settings.StartOffset");
        this.enabled = pl.getConfig().getBoolean("Games." + gameMode + ".Settings.Enabled");

        if (gameConfiguration != null && gameConfiguration.get("Phrases") != null) {
            this.wordList = gameConfiguration.getStringList("Phrases");
        }
        stop();
        if (enabled) {
            start();
        }
        pl.getLogger().info("Reloaded all values for manager: " + gameMode);
    }

    private void runGame() {
        int randomNumber;
        List<String> wordList = getWordList();
        randomNumber = ThreadLocalRandom.current().nextInt(0, wordList.size() - 1);
        setAnswer(wordList.get(randomNumber));

        if (gameMode.equalsIgnoreCase("Unscramble")) {
            setWord(shuffleWord(answer));
        } else if (gameMode.equalsIgnoreCase("SpeedTypist")) {
            setWord(answer);
        }

        setGameRunning(true);
        setGameStarted(System.currentTimeMillis());

        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if (!muteFile.getBoolean("Players." + p.getUniqueId().toString())) {
                p.sendMessage(ChatMessageType.CHAT, new ComponentBuilder(convertString(pl.getConfig().getString("Games." + gameMode + ".Messages.Badge") + pl.getConfig().getString("Games." + gameMode + ".Messages.GameStart"))).create());
            }
        }

        try {
            currentRoundTimer.cancel();
        } catch (NullPointerException ex) {//do nothing
        }
        currentRoundTimer = pl.getProxy().getScheduler().schedule(pl, () ->
        {
            if (isGameRunning()) {
                announceNoWinner(this);
                setGameRunning(false);
            }
            try {
                currentRoundTimer.cancel();
            } catch (NullPointerException ex) {//do nothing
            }
        }, pl.getConfig().getInt("Games." + gameMode + ".Settings.GameLength"), TimeUnit.SECONDS);
    }

    private String shuffleWord(String word) {
        List<Character> chars = Chars.asList(word.toCharArray());
        Collections.shuffle(chars);
        return new String(Chars.toArray(chars));
    }

    private void announceWinner(GameManager manager) {
        for (ProxiedPlayer player : pl.getProxy().getPlayers()) {
            if (muteFile.getBoolean("Players." + ( player).getUniqueId().toString())) {
                player.sendMessage(new ComponentBuilder(convertString("" + manager.getBadge() + " " + manager.getWinMessage())).create());
            }
        }
    }

    private void announceNoWinner(GameManager manager) {
        for (ProxiedPlayer player : pl.getProxy().getPlayers()) {
            if (muteFile.getBoolean("Players." + (player).getUniqueId().toString())) {
                player.sendMessage(new ComponentBuilder(convertString("" + manager.getBadge() + " " + manager.getNoWinnerMessage())).create());
            }
        }
    }

    private String convertString(String input) {
        return ChatColor.translateAlternateColorCodes('&', input.replace("%winner%", getWinner()).replace("%value%", (getGameLength() / 1000.0) + " Seconds").replace("%answer%", getAnswer()).replace("%word%", getWord()).replace("%gameLength%", pl.getConfig().getString("Games." + getGameLength() + ".Settings.GameLength")));
    }

    @SuppressWarnings("ConstantConditions")
    private void updateStatistic(String stat) {
        ProxiedPlayer player = pl.getProxy().getPlayer(getWinner());

        Configuration leaderBoard = pl.loadYaml(getLeaderBoardFile());
        try {
            if (stat.equalsIgnoreCase("TopSpeed")) {
                if (leaderBoard != null && leaderBoard.getLong(stat + "." + player.getUniqueId().toString()) == 0 || getGameLength() < leaderBoard.getLong(stat + "." + player.getUniqueId().toString())) {
                    leaderBoard.set(stat + "." + player.getUniqueId().toString(), getGameLength());
                }
            } else if (leaderBoard != null && stat.equalsIgnoreCase("Wins")) {
                leaderBoard.set(stat + "." + player.getUniqueId().toString(), leaderBoard.getLong(stat + "." + player.getUniqueId().toString()) + 1);
            }
            pl.saveYaml(leaderBoard, getLeaderBoardFile());
        } catch (NullPointerException ex) {
            pl.getLogger().info("Null pointer occurred when checking if stat exists.");
        }
    }

    @SuppressWarnings("deprecation")
    public void mute(CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            if (muteFile != null) {
                if (muteFile.getBoolean("Players." + ((ProxiedPlayer) sender).getUniqueId().toString())) {
                    muteFile.set("Players." + ((ProxiedPlayer) sender).getUniqueId().toString(), false);
                    for(GameManager gameManager : pl.registeredGames) {
                        sender.sendMessage(pl.badge + "You will no longer see " + gameManager.getGameMode() + " games.");
                    }
                } else {
                    muteFile.set("Players." + ((ProxiedPlayer) sender).getUniqueId().toString(), true);
                    for(GameManager gameManager : pl.registeredGames) {
                        sender.sendMessage(pl.badge + "You will now see " + gameManager.getGameMode() + " games.");
                    }
                }
                pl.saveYaml(muteFile, pl.getConfig().getString("General.GamesMuteFile"));
            }
        }
    }
}
