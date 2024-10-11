package com.example.application.views.home;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class ChatView extends VerticalLayout {
    private VerticalLayout chatHistory;

    public ChatView() {
        // Initialize the chat history layout
        chatHistory = new VerticalLayout();
        chatHistory.setWidth("100%");
        chatHistory.setHeight("400px");
        chatHistory.getStyle().set("overflow-y", "auto");  // Make it scrollable


        // Add components to the layout
        add(chatHistory);
    }

    // Method to add a new message to the chat history
    public void addMessage(String sender, String message) {
        Paragraph chatMessage = new Paragraph(sender + ": " + message);
        chatHistory.add(chatMessage);  // Dynamically add the message to the layout
        chatHistory.getElement().callJsFunction("scrollTop", chatHistory.getElement().getProperty("scrollHeight"));  // Auto-scroll to the bottom
    }
}

