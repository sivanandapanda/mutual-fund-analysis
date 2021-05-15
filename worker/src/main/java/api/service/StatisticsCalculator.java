package api.service;

import com.example.common.model.*;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.common.model.TenorEnum.*;

@ApplicationScoped
public class StatisticsCalculator {

    @Inject
    Logger log;

    public MutualFundStatistics getStatistics(MutualFund mutualFund) {
        List<NavPerDate> sortedNavList = mutualFund.getNavPerDates()
                .stream()
                .sorted(Comparator.comparing(NavPerDate::getDate).reversed())
                .collect(Collectors.toList());

        Statistics statistics = new Statistics(
                        Stream.of(getXDayNav(sortedNavList, sortedNavList.size(), -1, INCEPTION),
                                getXDayNav(sortedNavList, 1305, 1566, FIVEY), getXDayNav(sortedNavList, 528, 794, TWOY),
                                getXDayNav(sortedNavList, 264, 528, ONEY), getXDayNav(sortedNavList, 132, 264, SIXM),
                                getXDayNav(sortedNavList, 44, 132, TWOM), getXDayNav(sortedNavList, 22, 44, ONEM),
                                getXDayNav(sortedNavList, 10, 22, TWOW), getXDayNav(sortedNavList, 5, 10, ONEW),
                                getXDayNav(sortedNavList, 3, 7, THREED), getXDayNav(sortedNavList, 0, 3, ONED))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));

        return new MutualFundStatistics(mutualFund.getMeta(), statistics, navChangeIn5Years(statistics, mutualFund.getMeta().getSchemeName()));
    }

    private NavStatistics getXDayNav(List<NavPerDate> sortedNavList, int point, int pointToCompare, TenorEnum tenor) {
        if (sortedNavList.size() < point) {
            return null;
        }

        NavPerDate navOnDate = point == 0 ? sortedNavList.get(0) : sortedNavList.get(point - 1);

        if (sortedNavList.size() < pointToCompare) {
            pointToCompare = -1;
        }

        BigDecimal navToCompare = pointToCompare == -1 ? navOnDate.getNav() : sortedNavList.get(pointToCompare - 1).getNav();

        Move move;
        if (navOnDate.getNav().compareTo(navToCompare) > 0) {
            move = Move.UP;
        } else if (navOnDate.getNav().compareTo(navToCompare) < 0) {
            move = Move.DOWN;
        } else {
            move = Move.NO_CHANGE;
        }
        return new NavStatistics(point, navOnDate.getDate(), navOnDate.getNav(), tenor, move);
    }

    private double navChangeIn5Years(Statistics statistics, String schemeName) {
        double percentageIncrease = 0d;
        try {
            if (Objects.nonNull(statistics)) {

                Optional<NavStatistics> fiveY = statistics.getStatisticsList().stream().filter(s -> s.getTenor() == TenorEnum.FIVEY).findAny();
                Optional<NavStatistics> oneD = statistics.getStatisticsList().stream()
                        .filter(s -> s.getDate().isAfter(LocalDate.now().minusDays(30)) && s.getTenor() == TenorEnum.ONED).findAny();

                if (oneD.isPresent()) {
                    if (fiveY.isPresent() && fiveY.get().getNav().doubleValue() != 0d) {
                        BigDecimal increase = oneD.get().getNav().subtract(fiveY.get().getNav());
                        percentageIncrease = increase.divide(fiveY.get().getNav(), 2, RoundingMode.DOWN).multiply(new BigDecimal("100")).doubleValue();
                    } else {
                        Optional<NavStatistics> inception = statistics.getStatisticsList().stream().filter(s -> s.getTenor() == INCEPTION).findAny();

                        if (inception.isPresent() && inception.get().getNav().doubleValue() != 0d) {
                            BigDecimal increase = oneD.get().getNav().subtract(inception.get().getNav());
                            percentageIncrease = increase.divide(inception.get().getNav(), 2, RoundingMode.DOWN).multiply(new BigDecimal("100")).doubleValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed for " + schemeName ,e);
        }
        return percentageIncrease;
    }
}
