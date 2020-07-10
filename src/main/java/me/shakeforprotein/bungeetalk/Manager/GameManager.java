package me.shakeforprotein.bungeetalk.Manager;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private BungeeTalk pl;
    private Configuration gameConfiguration;
    private String badge;
    private List<String> rewards;
    private List<String> wordList = new ArrayList<>();
    private String gamemode;
    private String winMessage;
    private String noWinnerMessage;
    private String answer = "";
    private String leaderboardFile;
    private boolean gameIsRunning = false;
    private long gameStarted = 0;
    private long gameLength = System.currentTimeMillis();
    private String winner = "";
    private String word = "";


    public GameManager(BungeeTalk main, String gamemode){
        this.pl = main;
        this.gamemode = gamemode;
        this.badge = pl.getConfig().getString("Games." + gamemode + ".Messages.Badge");
        this.winMessage = pl.getConfig().getString("Games." + gamemode + ".Messages.Winner");
        this.noWinnerMessage = pl.getConfig().getString("Games." + gamemode + ".Messages.NoWinner");
        this.rewards = pl.getConfig().getStringList("Games." + gamemode + ".Rewards");
        this.leaderboardFile = pl.getConfig().getString("Games." + gamemode + ".LeaderboardFile");
        this.gameConfiguration = pl.loadYaml(pl.getConfig().getString("Games." +  gamemode + ".Filename"));
        if(gameConfiguration != null && gameConfiguration.get("Phrases") != null) {
            this.wordList = gameConfiguration.getStringList("Phrases");
        }
        System.out.println("Registered Manager for: " + gamemode);
    }

    public boolean checkGame(ProxiedPlayer sender, String message) {
        if (gameIsRunning) {
            if (message.equalsIgnoreCase(answer)) {
                winner = sender.getName();
                gameLength = System.currentTimeMillis() - gameStarted;
                pl.rewardPlayer(this, sender.getName());
                pl.updateStatistic(this, "Wins");
                pl.updateStatistic(this, "TopSpeed");
                pl.announceWinner(this);
                gameIsRunning = false;
                return true;
            }
        }
        return false;
    }

    public String getGamemode() {
        return gamemode;
    }

    public String getBadge() {
        return badge;
    }

    public List<String> getRewards(){
        return this.rewards;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isGameRunning() {
        return gameIsRunning;
    }

    public void setGameRunning(boolean gameIsRunning) {
        this.gameIsRunning = gameIsRunning;
    }

    public void setGameStarted(long gameStarted) {
        this.gameStarted = gameStarted;
    }

    public long getGameLength() {
        return gameLength;
    }

    public String getWinner() {
        return winner;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWinMessage() {
        return winMessage;
    }

    public String getNoWinnerMessage() {
        return noWinnerMessage;
    }

    public String getLeaderboardFile(){
        return leaderboardFile;
    }

    public List<String> getWordList(){
        return this.wordList;
    }

    public void setGameConfig(Configuration newConfig){
        this.gameConfiguration = newConfig;
    }

    public void reload(){
        this.badge = pl.getConfig().getString("Games." + gamemode + ".Messages.Badge");
        this.winMessage = pl.getConfig().getString("Games." + gamemode + ".Messages.Winner");
        this.noWinnerMessage = pl.getConfig().getString("Games." + gamemode + ".Messages.NoWinner");
        this.rewards = pl.getConfig().getStringList("Games." + gamemode + ".Rewards");
        this.leaderboardFile = pl.getConfig().getString("Games." + gamemode + ".LeaderboadrdFile");
        this.gameConfiguration = pl.loadYaml(pl.getConfig().getString("Games." +  gamemode + ".Filename"));
        if(gameConfiguration != null && gameConfiguration.get("Phrases") != null) {
            this.wordList = gameConfiguration.getStringList("Phrases");
        }
        System.out.println("Reloaded all values for manager: " + gamemode);
    }
}
