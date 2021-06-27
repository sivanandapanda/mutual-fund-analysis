package com.example.application.views.dashboard;

import com.example.application.service.MutualFundUIService;
import com.example.application.views.MainLayout;
import com.example.common.model.Dashboard;
import com.example.common.model.TenorEnum;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.common.model.TenorEnum.*;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
public class DashboardView extends Div {

    public DashboardView(@Autowired MutualFundUIService service) {
        addClassNames("dashboard-view", "flex", "flex-col", "h-full");

        // Configure Grid
        var grid = createAndConfigureGrid();
        grid.addClassName("dashboard-grid");
        grid.setItems(service.loadDashboard());
        add(grid);
    }

    public static Grid<Dashboard> createAndConfigureGrid() {
        Grid<Dashboard> grid = new Grid<>(Dashboard.class, false);
        grid.addColumn(dashboard -> {
            var statistics = dashboard.getMutualFundStatistics();
            return statistics == null || statistics.getMutualFundMeta() == null ? "" : statistics.getMutualFundMeta().getFundHouse();
        }).setHeader("Fund House");

        grid.addColumn(dashboard -> {
            var statistics = dashboard.getMutualFundStatistics();
            return statistics == null || statistics.getMutualFundMeta() == null ? "" : statistics.getMutualFundMeta().getSchemeName();
        }).setHeader("Scheme Name");

        grid.addColumn(dashboard -> {
            var statistics = dashboard.getMutualFundStatistics();
            return statistics == null || statistics.getMutualFundMeta() == null ? "" : statistics.getMutualFundMeta().getSchemeCategory();
        }).setHeader("Category");

        grid.addColumn(dashboard -> {
            var statistics = dashboard.getMutualFundStatistics();
            return statistics == null ? "" : statistics.getPercentageIncrease() + "%";
        }).setHeader("Last 5y % increase");

        addNavToGrid(grid, ONED, "1d");
        addNavToGrid(grid, THREED, "3d");
        addNavToGrid(grid, ONEW, "1w");
        addNavToGrid(grid, TWOW, "2w");
        addNavToGrid(grid, ONEM, "1m");
        addNavToGrid(grid, TWOM, "2m");
        addNavToGrid(grid, SIXM, "6m");
        addNavToGrid(grid, ONEY, "1y");
        addNavToGrid(grid, TWOY, "2y");
        addNavToGrid(grid, FIVEY, "5y");
        addNavToGrid(grid, INCEPTION, "0");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setSizeFull();
        grid.setHeightFull();
        grid.setWidthFull();

        return grid;
    }

    private static void addNavToGrid(Grid<Dashboard> grid, TenorEnum tenor, String header) {
        grid.addColumn(dashboard -> {
            var find = dashboard.getMutualFundStatistics().getStatistics().stream().filter(s -> s.getTenor() == tenor).findAny();
            return find.isEmpty() ? "" : find.get().getNav().doubleValue() + " (" + find.get().getDate().format(BASIC_ISO_DATE) + ")";
        }).setHeader(header);
    }
}
