package backend.academy.bot.command;

import backend.academy.bot.client.LinkClient;
import backend.academy.bot.dto.TrackLinkRequest;
import backend.academy.bot.util.LinkValidator;
import com.pengrad.telegrambot.model.Update;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TrackCommand implements Command {
    private final LinkClient scrapperClient;
    private static final Map<Long, TrackState> userStates = new HashMap<>();

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "Start tracking a link (GitHub/Stack Overflow only)";
    }

    @Override
    public String handle(Update update) {
        long chatId = update.message().chat().id();
        String message = update.message().text();

        if (message.startsWith("/track")) {
            userStates.put(chatId, new TrackState(TrackStep.WAITING_URL));
            return "📌 Send the link you want to track";
        }

        TrackState state = userStates.get(chatId);
        if (state == null) {
            return "❌ Error! Start over by sending /track";
        }

        switch (state.step) {
            case WAITING_URL:
                if (!LinkValidator.isValidLink(message)) {
                    return "🚫 *Invalid link!* Only GitHub and Stack Overflow links are supported.";
                }
                state.link = message;
                state.step = TrackStep.WAITING_TAGS;
                return "🏷 Enter tags (separated by spaces, optional). If not needed, send -";

            case WAITING_TAGS:
                state.tags = message.equals("-") ? List.of() : List.of(message.split(" "));
                state.step = TrackStep.WAITING_FILTERS;
                return "🔍 Specify filters (separated by spaces, optional). If not needed, send -";

            case WAITING_FILTERS:
                state.filters = message.equals("-") ? List.of() : List.of(message.split(" "));
                userStates.remove(chatId);
                return scrapperClient.addLink(chatId, new TrackLinkRequest(state.link, state.tags, state.filters));
        }

        return "❌ Error! Start over by sending /track";
    }

    public boolean isTrackingCompleted(long chatId) {
        return !userStates.containsKey(chatId);
    }

    private static class TrackState {
        TrackStep step;
        String link;
        List<String> tags;
        List<String> filters;

        TrackState(TrackStep step) {
            this.step = step;
        }
    }

    private enum TrackStep {
        WAITING_URL,
        WAITING_TAGS,
        WAITING_FILTERS
    }
}
