package com.mukul.pro.mickeybot.welcome;

import com.microsoft.bot.builder.*;
import com.microsoft.bot.schema.*;
import com.mukul.pro.mickeybot.state.MyUserState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Component
public class WelcomeUser extends ActivityHandler {
    // Messages sent to the user.
    @Value("${user.welcome.text}")
    private String WELCOMEMESSAGE;

    @Value("${user.info.text}")
    private String INFOMESSAGE;

    @Value("${user.locale.text}")
    private String LOCALEMESSAGE;

    @Value("${user.pattern.text}")
    private String PATTERNMESSAGE;

    private static final String FIRST_WELCOME_ONE =
            "You are seeing this message because this was your first message ever to this bot.";

    private static final String FIRST_WELCOME_TWO =
            "It is a good practice to welcome the user and provide personal greeting. For example: Welcome %s";

    private UserState userState;

    @Autowired
    public WelcomeUser(UserState userState) {
        this.userState = userState;
    }

    /**
     * Normal onTurn processing, with saving of state after each turn.
     *
     * @param turnContext The context object for this turn. Provides information
     *                    about the incoming activity, and other data needed to
     *                    process the activity.
     * @return A future task.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        return super.onTurn(turnContext)
                .thenCompose(saveResult -> userState.saveChanges(turnContext));
    }

    /**
     * This will prompt for a user name, after which it will send info about the
     * conversation. After sending information, the cycle restarts.
     *
     * @param turnContext The context object for this turn.
     * @return A future task.
     */
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        // Get state data from UserState.
        StatePropertyAccessor<MyUserState> stateAccessor =
                userState.createProperty("MyUserState");
        CompletableFuture<MyUserState> stateFuture =
                stateAccessor.get(turnContext, MyUserState::new);

        return stateFuture.thenApply(thisUserState -> {
            if (!thisUserState.isDidBotWelcomeUser()) {
                thisUserState.setDidBotWelcomeUser(true);

                String userName = turnContext.getActivity().getFrom().getName();
                return turnContext
                        .sendActivities(
                                MessageFactory.text(FIRST_WELCOME_ONE),
                                MessageFactory.text(String.format(FIRST_WELCOME_TWO, userName))
                        );
            } else {
                String text = turnContext.getActivity().getText().toLowerCase();
                switch (text) {
                    case "hello":
                    case "hi":
                        return turnContext.sendActivities(MessageFactory.text("You said " + text));

                    case "intro":
                    case "help":
                        return sendIntroCard(turnContext);

                    default:
                        return turnContext.sendActivity(WELCOMEMESSAGE);
                }
            }
        })
                // make the return value happy.
                .thenApply(resourceResponse -> null);
    }

    private CompletableFuture<ResourceResponse> sendIntroCard(TurnContext turnContext) {
        HeroCard card = new HeroCard() {{
            setTitle("Welcome to Bot Framework!");
            setText(
                    "Welcome to Welcome Users bot sample! This Introduction card "
                            + "is a great way to introduce your Bot to the user and suggest "
                            + "some things to get them started. We use this opportunity to "
                            + "recommend a few next steps for learning more creating and deploying bots."
            );
        }};

        card.setImages(Collections.singletonList(new CardImage() {
            {
                setUrl("https://aka.ms/bf-welcome-card-image");
            }
        }));

        card.setButtons(Arrays.asList(
                new CardAction() {{
                    setType(ActionTypes.OPEN_URL);
                    setTitle("Get an overview");
                    setText("Get an overview");
                    setDisplayText("Get an overview");
                    setValue(
                            "https://docs.microsoft.com/en-us/azure/bot-service/?view=azure-bot-service-4.0"
                    );
                }},
                new CardAction() {{
                    setType(ActionTypes.OPEN_URL);
                    setTitle("Ask a question");
                    setText("Ask a question");
                    setDisplayText("Ask a question");
                    setValue("https://stackoverflow.com/questions/tagged/botframework");
                }},
                new CardAction() {{
                    setType(ActionTypes.OPEN_URL);
                    setTitle("Learn how to deploy");
                    setText("Learn how to deploy");
                    setDisplayText("Learn how to deploy");
                    setValue(
                            "https://docs.microsoft.com/en-us/azure/bot-service/bot-builder-howto-deploy-azure?view=azure-bot-service-4.0"
                    );
                }})
        );

        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }
}
