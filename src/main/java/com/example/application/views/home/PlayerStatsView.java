package com.example.application.views.home;

import ai.peoplecode.OpenAIConversation;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.awt.*;

public class PlayerStatsView extends VerticalLayout {

    private ProgressBar healthBar;
    private Text healthText;
    private TextField abilityScore;
    private int maxHealth;
    private int currentHealth;
    private com.vaadin.flow.component.html.Image playerImage;  // Add an Image component


    public PlayerStatsView(String playerLabel, OpenAIConversation judge, TextField playerName, com.vaadin.flow.component.html.Image playerImage) {
        this.playerImage = playerImage;
        // Ability Score TextField
        abilityScore = new TextField("Ability Score");

        // Button to set player stats
        Button setButton = new Button("Pick this player");

        setButton.addClickListener(event -> {
            setButton.setEnabled(false);
            playerName.setReadOnly(true);
            Notification.show("Once the player is picked, you cannot change.", 3000, Notification.Position.MIDDLE);

            // Fetch ability score and health points from the judge
            String abilityScoreFromJudge = judge.askQuestion("You are a fair judge to determine the ability score of a comics character. " +
                    "Give me a integer of his ability score based on his fighting power compared to other comics characters. Ranging from 0-100." +
                    "No explanation.", playerName.getValue());
            String healthPointsFromJudge = judge.askQuestion("You are a fair judge to determine the ability score of a comics character. " +
                    "Give me a integer of his ability score based on his endurance and resistance compared to other comics characters. Ranging from 0-10000." +
                    "No explanation.", playerName.getValue());

            abilityScore.setValue(Integer.toString(Integer.parseInt(abilityScoreFromJudge)));
            abilityScore.setReadOnly(true);

            maxHealth = Integer.parseInt(Integer.toString(Integer.parseInt(healthPointsFromJudge)));
            currentHealth = maxHealth;

            // Initialize the health bar and text
            healthBar = new ProgressBar(0, maxHealth, currentHealth);
            healthBar.setWidth("200px");

            // Update the health text
            healthText.setText(currentHealth + " / " + maxHealth);

            // Dynamically update the player image based on playerName or other criteria
            updatePlayerImage(playerName.getValue());

            add(abilityScore, setButton, healthBar, healthText);  // Make sure to add healthBar and healthText here

        });

        // Initialize the health text
        healthText = new Text("Health: N/A");

        // Add components to the layout
        add(abilityScore, setButton, healthText);
    }

    // Method to update health based on damage
    public void updateHealth(int damage) {
        currentHealth = Math.max(currentHealth - damage, 0); // Ensure health doesn't go below 0
        healthBar.setValue((double) currentHealth);
        healthText.setText(currentHealth + " / " + maxHealth);
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    private void updatePlayerImage(String playerName) {
        // Logic to set image based on playerName or other criteria
        // For simplicity, using the player name to set a corresponding image file
        switch (playerName.toLowerCase()) {
            case "spiderman":
                playerImage.setSrc("images/spiderman.png");
                break;
            case "batman":
                playerImage.setSrc("images/batman.png");
                break;
            default:
                playerImage.setSrc("images/default.png");  // Default image if no specific match
                break;
        }
    }
}
