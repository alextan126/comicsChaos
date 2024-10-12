package com.example.application.views.home;

import ai.peoplecode.OpenAIConversation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerStatsView extends VerticalLayout {

    private ProgressBar healthBar;
    private final Span healthText;
    private final Span abilityScoreLabel;
    private int abilityScore;
    private int maxHealth;
    private int currentHealth;
    private Button[] suggestAttackButtons;
    private final OpenAIConversation judge;
    private final TextField playerNameField;

    private String fighterName;

    public int getAbilityScore() {
        return abilityScore;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public String getFighterName() {
        return this.fighterName;
    }

    // Define the listener interface
    public interface PlayerReadyListener {
        void onPlayerReady();
    }

    private PlayerReadyListener playerReadyListener;

    public void setPlayerReadyListener(PlayerReadyListener listener) {
        this.playerReadyListener = listener;
    }

    public PlayerStatsView(OpenAIConversation judge, TextField playerNameField) {
        this.judge = judge;
        this.playerNameField = playerNameField;

        // Initialize labels
        abilityScoreLabel = new Span("Ability Score: N/A");
        healthText = new Span("Health: N/A");

        // Apply styles to the labels for better UI
        abilityScoreLabel.getStyle().set("font-weight", "bold");
        healthText.getStyle().set("font-weight", "bold");

        // Button to set player stats and enable attack buttons
        Button setButton = new Button("Pick this player");
        setButton.addClassName("pick-button");

        setButton.addClickListener(event -> {
            setButton.setEnabled(false);
            playerNameField.setReadOnly(true);
            Notification.show("You have locked in your player!", 3000, Notification.Position.MIDDLE);
            initializeStats();
            if (playerReadyListener != null) {
                playerReadyListener.onPlayerReady();
            }
        });

        add(setButton);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);
    }

    public void initializeStats() {
        String playerName = playerNameField.getValue();
        this.fighterName = playerName;

        // Fetch ability score and health points from the judge
        String abilityScoreFromJudge = judge.askQuestion(
                "You are a fair judge to determine the ability score of a comics character. Give me an integer of his ability score based on his fighting power compared to other comics characters, ranging from 0-100. No explanation.",
                playerName
        );
        String healthPointsFromJudge = judge.askQuestion(
                "You are a fair judge to determine the health points of a comics character. Give me an integer of his health points based on his endurance and resistance compared to other comics characters, ranging from 0-10000. No explanation.",
                playerName
        );
        abilityScore = extractIntegerFromResponse(abilityScoreFromJudge);
        if (abilityScore == 0) {
            abilityScore = 50;
        }

        abilityScoreLabel.setText("Ability Score: " + abilityScore);
        maxHealth = extractIntegerFromResponse(healthPointsFromJudge);
        if (maxHealth == 0) {
            maxHealth = 5000;
        }

        currentHealth = maxHealth;
        healthBar = new ProgressBar(0, maxHealth, currentHealth);
        healthBar.setWidth("200px");
        healthBar.addClassName("health-bar");
        healthText.setText("Health: " + currentHealth + " / " + maxHealth);

        // Remove the set button and add the labels and progress bar
        remove(getComponentAt(0));
        add(abilityScoreLabel, healthBar, healthText);
        if (suggestAttackButtons != null) {
            for (Button button : suggestAttackButtons) {
                button.setEnabled(true);
            }
        }
    }

    public void setSuggestAttackButtons(Button[] suggestAttackButtons) {
        this.suggestAttackButtons = suggestAttackButtons;
    }

    // Method to update health based on damage
    public void applyDamage(int damage) {
        currentHealth = Math.max(currentHealth - damage, 0);
        healthBar.setValue(currentHealth);
        healthText.setText("Health: " + currentHealth + " / " + maxHealth);
    }

    // Method to extract integers from AI responses
    private int extractIntegerFromResponse(String response) {
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(response);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            return 0;
        }
    }
}
