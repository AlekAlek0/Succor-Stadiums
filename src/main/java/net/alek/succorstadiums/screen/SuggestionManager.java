package net.alek.succorstadiums.screen;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SuggestionManager {
    private final EditBox editBox;
    private final List<String> allSuggestions;
    private List<String> filteredSuggestions = new ArrayList<>();
    private int selectedSuggestion = 0;
    private int suggestionScrollOffset = 0;
    private final int maxVisibleSuggestions;
    private final int dropdownYOffset;
    private final boolean isCommaSeparated;

    public SuggestionManager(EditBox editBox, Registry<?> registry, int maxVisibleSuggestions, int dropdownYOffset, boolean isCommaSeparated) {
        this.editBox = editBox;
        this.allSuggestions = registry.keySet().stream().map(Identifier::toString).sorted().collect(Collectors.toList());
        this.maxVisibleSuggestions = maxVisibleSuggestions;
        this.dropdownYOffset = dropdownYOffset;
        this.isCommaSeparated = isCommaSeparated;
    }

    public void filterSuggestions(String text) {
        selectedSuggestion = 0;
        suggestionScrollOffset = 0;
        String textToFilter = text;
        
        if (isCommaSeparated) {
            int lastComma = text.lastIndexOf(',');
            if (lastComma != -1) {
                textToFilter = text.substring(lastComma + 1).trim();
            }
        }

        if (textToFilter.isEmpty()) {
            filteredSuggestions = new ArrayList<>();
            editBox.setSuggestion(null);
        } else {
            String finalTextToFilter = textToFilter.toLowerCase();
            filteredSuggestions = allSuggestions.stream()
                    .filter(s -> s.contains(finalTextToFilter))
                    .collect(Collectors.toList());

            if (!filteredSuggestions.isEmpty()) {
                String first = filteredSuggestions.get(0);
                editBox.setSuggestion(first.startsWith(textToFilter) ? first.substring(textToFilter.length()) : null);
            } else {
                editBox.setSuggestion(null);
            }
        }
    }

    public boolean hasSuggestions() {
        return !filteredSuggestions.isEmpty();
    }

    public void applySuggestion() {
        if (selectedSuggestion >= 0 && selectedSuggestion < filteredSuggestions.size()) {
            String suggestion = filteredSuggestions.get(selectedSuggestion);
            if (isCommaSeparated) {
                String currentText = editBox.getValue();
                int lastComma = currentText.lastIndexOf(',');
                String prefix = "";
                if (lastComma != -1) {
                    prefix = currentText.substring(0, lastComma + 1).trim();
                }
                editBox.setValue(prefix + (prefix.isEmpty() || currentText.endsWith(",") ? "" : ", ") + suggestion);
            } else {
                editBox.setValue(suggestion);
            }
            editBox.setSuggestion(null);
            filteredSuggestions = new ArrayList<>();
            selectedSuggestion = 0;
            suggestionScrollOffset = 0;
        }
    }

    public void updateSuggestionSuffix() {
        if (!filteredSuggestions.isEmpty()) {
            String selected = filteredSuggestions.get(selectedSuggestion);
            String typed = editBox.getValue();
            String textToCompare = typed;

            if (isCommaSeparated) {
                int lastComma = typed.lastIndexOf(',');
                if (lastComma != -1) {
                    textToCompare = typed.substring(lastComma + 1).trim();
                }
            }

            editBox.setSuggestion(selected.startsWith(textToCompare) ? selected.substring(textToCompare.length()) : null);
        }
    }

    public void scrollSuggestions(double vertical) {
        suggestionScrollOffset = (int) Math.max(0,
                Math.min(suggestionScrollOffset - vertical,
                        filteredSuggestions.size() - maxVisibleSuggestions));
    }

    public void selectNextSuggestion() {
        selectedSuggestion = (selectedSuggestion + 1) % filteredSuggestions.size();
        if (selectedSuggestion >= suggestionScrollOffset + maxVisibleSuggestions) {
            suggestionScrollOffset = selectedSuggestion - maxVisibleSuggestions + 1;
        }
        updateSuggestionSuffix();
    }

    public void selectPreviousSuggestion() {
        selectedSuggestion = (selectedSuggestion - 1 + filteredSuggestions.size()) % filteredSuggestions.size();
        if (selectedSuggestion < suggestionScrollOffset) {
            suggestionScrollOffset = selectedSuggestion;
        }
        updateSuggestionSuffix();
    }

    public EditBox getEditBox() {
        return editBox;
    }

    public int getDropdownX() {
        return editBox.getX();
    }

    public int getDropdownY() {
        return editBox.getY() + dropdownYOffset;
    }

    public int getDropdownWidth() {
        return editBox.getWidth();
    }

    public int getVisibleSuggestionsCount() {
        return Math.min(maxVisibleSuggestions, filteredSuggestions.size());
    }

    public String getSuggestion(int index) {
        return filteredSuggestions.get(index + suggestionScrollOffset);
    }

    public int getSelectedSuggestionIndex() {
        return selectedSuggestion;
    }

    public void setSelectedSuggestion(int visibleIndex) {
        this.selectedSuggestion = visibleIndex + suggestionScrollOffset;
        updateSuggestionSuffix();
    }

    public String getTypedText() {
        return editBox.getValue();
    }

    public int getSuggestionScrollOffset() {
        return suggestionScrollOffset;
    }

    public boolean isCommaSeparated() {
        return isCommaSeparated;
    }

    // Public getter for filteredSuggestions
    public List<String> getFilteredSuggestions() {
        return filteredSuggestions;
    }
}