package com.example.application.views.home;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import ai.peoplecode.OpenAIConversation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PageTitle("Home")
@Route(value = "")
@RouteAlias(value = "")
@CssImport("./styles/shared-styles.css")
public class HomeView extends VerticalLayout {

    private final OpenAIConversation p1AI = new OpenAIConversation("demo", "gpt-4o-mini");
    private final OpenAIConversation p2AI = new OpenAIConversation("demo", "gpt-4o-mini");
    private final OpenAIConversation judgeAI = new OpenAIConversation("demo", "gpt-4o-mini");
    private PlayerStatsView fighterOneStatsView;
    private PlayerStatsView fighterTwoStatsView;
    private final ChatView fightHistory;
    private boolean isFighterOneTurn = true;

    private Button[] fighterOneSuggestAttackButtons;
    private Button[] fighterTwoSuggestAttackButtons;
    private boolean fighterOneReady = false;
    private boolean fighterTwoReady = false;

    public HomeView() {
        H1 header = new H1("AI Battle Arena");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        fightHistory = new ChatView();
        fightHistory.setWidth("65%");
        fightHistory.setHeight("300px");
        fightHistory.getStyle().set("border-radius", "10px");
        fightHistory.addClassName("fight-history");

        VerticalLayout fighterOneLayout = createFighterLayout("Fighter One", p1AI);
        VerticalLayout fighterTwoLayout = createFighterLayout("Fighter Two", p2AI);

        mainLayout.add(fighterOneLayout, fighterTwoLayout);
        add(header, mainLayout, fightHistory);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);
    }

    private VerticalLayout createFighterLayout(String fighterLabel, OpenAIConversation fighterAI) {
        VerticalLayout fighterLayout = new VerticalLayout();
        fighterLayout.addClassName("fighter-layout");
        fighterLayout.setWidth("45%");
        fighterLayout.setAlignItems(Alignment.CENTER);

        TextField fighterNameField = new TextField(fighterLabel + " Name");
        fighterNameField.setPlaceholder("Enter character name");
        fighterNameField.setWidthFull();

        Image fighterImage = new Image("images/default.png", "");
        fighterImage.setWidth("200px");
        fighterImage.setHeight("200px");
        fighterImage.addClassName("fighter-image");

        PlayerStatsView fighterStatsView = new PlayerStatsView(judgeAI, fighterNameField);

        if (fighterLabel.equals("Fighter One")) {
            fighterOneStatsView = fighterStatsView;
        } else {
            fighterTwoStatsView = fighterStatsView;
        }

        // Create three attack buttons
        Button[] suggestAttackButtons = new Button[3];
        for (int i = 0; i < 3; i++) {
            suggestAttackButtons[i] = new Button("Attack " + (i + 1));
            suggestAttackButtons[i].addClassName("suggest-attack-button");
            suggestAttackButtons[i].setWidthFull();
            suggestAttackButtons[i].setEnabled(false); // Initially disabled
        }

        fighterStatsView.setSuggestAttackButtons(suggestAttackButtons);

        // Store suggestAttackButtons references
        if (fighterLabel.equals("Fighter One")) {
            fighterOneSuggestAttackButtons = suggestAttackButtons;
        } else {
            fighterTwoSuggestAttackButtons = suggestAttackButtons;
        }

        fighterNameField.addValueChangeListener(event -> {
            String fighterName = event.getValue();
            if (fighterName != null && !fighterName.isEmpty()) {
                updateFighterImage(fighterImage, fighterName);
            }
        });

        // Set PlayerReadyListener to handle readiness and attack suggestions
        fighterStatsView.setPlayerReadyListener(() -> {
            if (fighterLabel.equals("Fighter One")) {
                fighterOneReady = true;
                updateSuggestedAttacks(fighterOneSuggestAttackButtons, p1AI, fighterOneStatsView.getFighterName());
            } else {
                fighterTwoReady = true;
                updateSuggestedAttacks(fighterTwoSuggestAttackButtons, p2AI, fighterTwoStatsView.getFighterName());
            }

            // Enable the correct suggest attack buttons when both players are ready
            if (fighterOneReady && fighterTwoReady) {
                if (isFighterOneTurn) {
                    enableAttackButtons(fighterOneSuggestAttackButtons, true);
                } else {
                    enableAttackButtons(fighterTwoSuggestAttackButtons, true);
                }
            }
        });

        // Add click listeners to each attack button
        for (Button attackButton : suggestAttackButtons) {
            attackButton.addClickListener(event -> {
                handleAttackButtonClick(fighterLabel, fighterNameField, fighterAI, attackButton);
            });
        }

        // Change to VerticalLayout for stacking buttons vertically
        VerticalLayout attackButtonsLayout = new VerticalLayout(suggestAttackButtons);
        attackButtonsLayout.setWidthFull();
        attackButtonsLayout.setSpacing(true);

        fighterLayout.add(fighterNameField, fighterImage, fighterStatsView, attackButtonsLayout);
        fighterLayout.setSpacing(true);
        fighterLayout.setPadding(true);

        return fighterLayout;
    }

    private void handleAttackButtonClick(String fighterLabel, TextField fighterNameField, OpenAIConversation fighterAI, Button attackButton) {
        if ((fighterLabel.equals("Fighter One") && isFighterOneTurn) || (fighterLabel.equals("Fighter Two") && !isFighterOneTurn)) {
            String fighterName = fighterNameField.getValue();
            if (fighterName == null || fighterName.isEmpty()) {
                Notification.show("Please enter your character's name.");
                return;
            }

            PlayerStatsView opponentStatsView = getOpponentStatsView(fighterLabel);
            if (opponentStatsView == null) {
                Notification.show("Opponent not found.");
                return;
            }

            String command = attackButton.getText();
            if (command == null || command.isEmpty()) {
                Notification.show("No suggested attack available.");
                return;
            }

            fightHistory.addMessage(fighterName, command);

            String moveSuggestion = fighterAI.askQuestion(
                    "I am " + fighterName + ". My friend wants me to " + command,
                    "What is my reasonable move based on the comics?"
            );
            fightHistory.addMessage(fighterName + " AI", moveSuggestion);

            String damageQuery = "You are a fair judge to determine the damage of a move based on the character's ability score. Give me an integer of this move's damage, ranging from 0 - 5000 based on my ability score. No explanation.";
            int abilityScore = getPlayerStatsView(fighterLabel).getAbilityScore();
            String damageStr = judgeAI.askQuestion(damageQuery + " Ability Score: " + abilityScore, moveSuggestion);
            int damage = extractIntegerFromResponse(damageStr);

            opponentStatsView.applyDamage(damage);
            fightHistory.addMessage("Judge", fighterName + " has done " + damage + " points of damage to the opponent.");
            triggerFightAnimation(opponentStatsView);

            if (opponentStatsView.getCurrentHealth() <= 0) {
                fightHistory.addMessage("Game Over", fighterName + " wins!");
                disableAllAttackButtons();
                showGameOverDialog(fighterName + " wins!");
            } else {
                // Switch turns
                isFighterOneTurn = !isFighterOneTurn;

                // Enable the correct attack buttons
                if (isFighterOneTurn) {
                    enableAttackButtons(fighterOneSuggestAttackButtons, true);
                    enableAttackButtons(fighterTwoSuggestAttackButtons, false);
                } else {
                    enableAttackButtons(fighterOneSuggestAttackButtons, false);
                    enableAttackButtons(fighterTwoSuggestAttackButtons, true);
                }
            }
        } else {
            Notification.show("It's not your turn!");
        }
    }

    private void updateSuggestedAttacks(Button[] suggestAttackButtons, OpenAIConversation fighterAI, String fighterName) {
        String suggestionResponse = fighterAI.askQuestion(
                "You are a comic and manga expert. And you only respond with the answer to the question no other text, no numbers either ie do not do 1. 2. 3.",
                "Please list three typical moves or attacks that a character named " + fighterName + " might perform in a battle. List them as separate lines."
        );

        String[] suggestions = suggestionResponse.split("\\n");
        for (int i = 0; i < suggestAttackButtons.length && i < suggestions.length; i++) {
            suggestAttackButtons[i].setText(suggestions[i].trim());
        }
    }

    private void updateFighterImage(Image fighterImage, String fighterName) {
        String[] fileExtensions = {".jpg"};
        for (String extension : fileExtensions) {
            String imageUrl = "images/fighters/" + fighterName + extension;
            try {
                fighterImage.setSrc(imageUrl);
                return;
            } catch (Exception ignored) {
            }
        }
        fighterImage.setSrc("images/default.png");
    }

    private PlayerStatsView getOpponentStatsView(String fighterLabel) {
        return fighterLabel.equals("Fighter One") ? fighterTwoStatsView : fighterOneStatsView;
    }

    private PlayerStatsView getPlayerStatsView(String fighterLabel) {
        return fighterLabel.equals("Fighter One") ? fighterOneStatsView : fighterTwoStatsView;
    }

    private void triggerFightAnimation(PlayerStatsView opponentStatsView) {
        opponentStatsView.getElement().executeJs(
                "this.classList.remove('shake');" +
                        "void this.offsetWidth;" +  // This forces reflow, allowing the animation to trigger again
                        "this.classList.add('shake');" +
                        "setTimeout(() => this.classList.remove('shake'), 500);"
        );
    }

    private int extractIntegerFromResponse(String response) {
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(response);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            return 0;
        }
    }

    private void enableAttackButtons(Button[] attackButtons, boolean enabled) {
        for (Button button : attackButtons) {
            button.setEnabled(enabled);
        }
    }

    private void disableAllAttackButtons() {
        enableAttackButtons(fighterOneSuggestAttackButtons, false);
        enableAttackButtons(fighterTwoSuggestAttackButtons, false);
    }

    private void showGameOverDialog(String message) {
        Dialog dialog = new Dialog();
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setAlignItems(Alignment.CENTER);
        dialogLayout.setSpacing(true);

        H2 dialogTitle = new H2("Game Over");
        Span messageLabel = new Span(message);
        Button restartButton = new Button("Restart", event -> {
            dialog.close();
            restartGame();
        });

        dialogLayout.add(dialogTitle, messageLabel, restartButton);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void restartGame() {
        getUI().ifPresent(ui -> ui.getPage().reload());
    }
}
