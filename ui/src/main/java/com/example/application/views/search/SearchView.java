package com.example.application.views.search;

import com.example.application.service.MutualFundUIService;
import com.example.application.views.MainLayout;
import com.example.common.model.SearchableMutualFund;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@Route(value = "search", layout = MainLayout.class)
@PageTitle("Search")
public class SearchView extends HorizontalLayout {

    public SearchView(@Autowired MutualFundUIService service) {
        addClassName("search-view");
        TextField searchString = new TextField("Your search string");
        Button search = new Button("Search");
        Button clear = new Button("Clear");
        setVerticalComponentAlignment(Alignment.END, searchString, clear);

        var grid = new Grid<>(SearchableMutualFund.class, false);
        grid.addClassName("search-result-grid");
        grid.setColumns("schemeName");
        grid.addComponentColumn(this::createWatchButton).setHeader("Actions");

        search.addClickListener(e -> {
            Notification.show("Searching " + searchString.getValue() + "...");
            var searchResult = service.search(searchString.getValue());
            if(!searchResult.isEmpty()) {
                grid.setItems(searchResult);
                grid.getColumns().forEach(col -> col.setAutoWidth(true));
                add(grid);
            } else {
                add(new Paragraph("No results!"));
            }
        });

        clear.addClickListener(e -> {
            grid.setItems(Collections.emptyList());
            remove(grid);
        });

        add(searchString, search, clear);
    }

    private Button createWatchButton(SearchableMutualFund item) {
        return new Button("Watch", clickEvent -> getElement().executeJs("return window.localStorage.getItem($0)", "fav-mf-schemecodes")
            .then(scriptResult -> {
                var existingSchemeCodes = scriptResult.asString();
                String jsonString;
                if(existingSchemeCodes == null || existingSchemeCodes.contains("null")) {
                    jsonString = String.valueOf(item.getSchemeCode());
                } else {
                    jsonString = existingSchemeCodes + "," + item.getSchemeCode();
                }
                getElement().executeJs("window.localStorage.setItem($0, $1)", "fav-mf-schemecodes", jsonString);
            }));
    }

}
