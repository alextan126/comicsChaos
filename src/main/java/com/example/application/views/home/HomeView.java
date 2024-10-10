package com.example.application.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import ai.peoplecode.OpenAIConversation;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.TextField;

@PageTitle("Home")
@Menu(icon = "line-awesome/svg/pencil-ruler-solid.svg", order = 0)
@Route(value = "")
@RouteAlias(value = "")

public class HomeView extends VerticalLayout {

    private OpenAIConversation p1 = new OpenAIConversation("demo", "gpt-4o-mini");
    private OpenAIConversation p2 = new OpenAIConversation("demo", "gpt-4o-mini");
    private OpenAIConversation judge = new OpenAIConversation("demo", "gpt-4o-mini");

    private TextField askText;
    private Paragraph replyText;
    class MyClickListener
            implements ComponentEventListener<ClickEvent<Button>> {
        int count = 0;

        @Override
        public void onComponentEvent(ClickEvent<Button> event) {
            //event.getSource()
            //        .setText("You have clicked me " + (++count) + " times");
            String reply= p1.askQuestion("You are Plato", askText.getValue());
            replyText.setText(reply);
        }
    }


    public HomeView() {
        // Main layout containing two players
        HorizontalLayout mainLayout = new HorizontalLayout();

        // Create the left player's layout
        VerticalLayout leftPlayerLayout = createPlayerLayout("Left Player");

        // Create the right player's layout
        VerticalLayout rightPlayerLayout = createPlayerLayout("Right Player");

        // Add both player layouts to the main layout
        mainLayout.add(leftPlayerLayout, rightPlayerLayout);

        // Add the main layout to the view
        add(mainLayout);
    }

    private VerticalLayout createPlayerLayout(String playerLabel) {
        int maxHealth; // Max health points
        int currentHealth;


        VerticalLayout playerLayout = new VerticalLayout();


        //DONE add event listener, if a player name in inputed, gpt will determin the ability score and heath bar
        //DONE now the player stats are intergrated to PlayerStatsView
        // Player Name and Satistic
        TextField playerName = new TextField(playerLabel + " Name");
        playerName.setPlaceholder("Enter name");


        // Player Image
        Image playerImage = new Image("testImage/testImage.jpg", playerLabel + " Image");

        PlayerStatsView playerStatsView = new PlayerStatsView(playerLabel, judge, playerName, playerImage);

        //TODO unable to update player image  in PlayerStatsView



        //TODO history the user prompt and system response should be placed here with visibility of the latest prompt

        // Fight History
        Text fightHistory = new Text(playerLabel + " Fight History: This is a placeholder");

        //TODO Once user inputs a prompt, the move should be send to another bot, the other bot decides the dmg and deduction of health bar

        // User Prompt
        TextField userPrompt = new TextField(playerLabel + " User Prompt");
        userPrompt.setPlaceholder("Enter command...");

        // Add all components to the player layout
        playerLayout.add(playerName, playerStatsView, playerImage, fightHistory, userPrompt);

        return playerLayout;
    }
}




