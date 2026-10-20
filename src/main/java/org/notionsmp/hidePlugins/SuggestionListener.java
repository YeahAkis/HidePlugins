package org.notionsmp.hidePlugins;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

final class SuggestionListener implements Listener {

    private final CommandFilter filter;

    SuggestionListener(CommandFilter filter) {
        this.filter = filter;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSuggestions(AsyncPlayerSendSuggestionsEvent event) {
        Player player = event.getPlayer();
        String buffer = event.getBuffer();
        if (!buffer.startsWith("/")) {
            return;
        }

        int space = buffer.indexOf(' ');
        if (space >= 0) {
            if (filter.isHidden(buffer.substring(1, space), player)) {
                event.setCancelled(true);
            }
            return;
        }

        Suggestions suggestions = event.getSuggestions();
        List<Suggestion> visible = new ArrayList<>(suggestions.getList());
        if (visible.removeIf(suggestion -> !filter.isVisible(suggestion.getText(), player))) {
            event.setSuggestions(new Suggestions(suggestions.getRange(), visible));
        }
    }
}
