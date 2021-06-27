package com.example.application.views.explore;

import com.example.application.service.MutualFundUIService;
import com.example.application.views.MainLayout;
import com.example.common.model.Dashboard;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static com.example.application.views.dashboard.DashboardView.createAndConfigureGrid;

@Route(value = "explore", layout = MainLayout.class)
@PageTitle("Explore")
public class ExploreView extends HorizontalLayout {

    public ExploreView(@Autowired MutualFundUIService service) {
        addClassName("explore-view");
        TextField searchString = new TextField("Your explore string");
        TextField sampleSize = new TextField("Sample Size");
        Button explore = new Button("Explore");
        Button clear = new Button("Clear");
        setVerticalComponentAlignment(Alignment.END, searchString, sampleSize, explore, clear);

        var grid = createAndConfigureGrid();
        grid.addClassName("explore-result-grid");
        grid.addComponentColumn(this::createWatchButton).setHeader("Actions");

        explore.addClickListener(e -> {
            Notification.show("Exploring " + searchString.getValue() + "...");
            var searchResult = service.explore(searchString.getValue(), sampleSize.getValue());
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

        add(searchString, sampleSize, explore, clear);
    }

    private Button createWatchButton(Dashboard item) {
        return new Button("Watch", clickEvent -> getElement().executeJs("return window.localStorage.getItem($0)", "fav-mf-schemecodes")
                .then(scriptResult -> {
                    var existingSchemeCodes = scriptResult.asString();
                    String jsonString;
                    if(existingSchemeCodes == null || existingSchemeCodes.contains("null")) {
                        jsonString = String.valueOf(item.getMutualFundStatistics().getMutualFundMeta().getSchemeCode());
                    } else {
                        jsonString = existingSchemeCodes + "," + item.getMutualFundStatistics().getMutualFundMeta().getSchemeCode();
                    }
                    getElement().executeJs("window.localStorage.setItem($0, $1)", "fav-mf-schemecodes", jsonString);
                }));
    }
}
