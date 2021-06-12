package com.example.worker.service;

import com.example.common.model.Move;
import com.example.common.model.MutualFund;
import com.example.common.model.NavPerDate;
import com.example.common.model.TenorEnum;
import com.example.mutualfund.grpc.MutualFundStatisticsGrpc;
import com.example.mutualfund.grpc.NavStatisticsGrpc;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
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

    MutualFundStatisticsGrpc getStatistics(MutualFund mutualFund) {
        List<NavPerDate> sortedNavList = mutualFund.getNavPerDates()
                .stream()
                .sorted(Comparator.comparing(NavPerDate::getDate).reversed())
                .collect(Collectors.toList());


        var statistics = Stream.of(getXDayNav(sortedNavList, sortedNavList.size(), -1, INCEPTION),
                getXDayNav(sortedNavList, 1305, 1566, FIVEY), getXDayNav(sortedNavList, 528, 794, TWOY),
                getXDayNav(sortedNavList, 264, 528, ONEY), getXDayNav(sortedNavList, 132, 264, SIXM),
                getXDayNav(sortedNavList, 44, 132, TWOM), getXDayNav(sortedNavList, 22, 44, ONEM),
                getXDayNav(sortedNavList, 10, 22, TWOW), getXDayNav(sortedNavList, 5, 10, ONEW),
                getXDayNav(sortedNavList, 3, 7, THREED), getXDayNav(sortedNavList, 0, 3, ONED))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return MutualFundStatisticsGrpc.newBuilder()
                .addAllStatistics(statistics)
                .setMeta(mutualFund.getMeta().convertToGrpcModel())
                .setPercentageIncrease(navChangeIn5Years(statistics, mutualFund.getMeta().getSchemeName()))
                .build();
    }

    private NavStatisticsGrpc getXDayNav(List<NavPerDate> sortedNavList, int point, int pointToCompare, TenorEnum tenor) {
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

        return NavStatisticsGrpc.newBuilder()
                .setDate(navOnDate.getDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                .setDays(point)
                .setNav(navOnDate.getNav().doubleValue())
                .setTenorValue(tenor.getTenorValue())
                .setMoveValue(move.getMoveValue())
                .build();
    }

    private double navChangeIn5Years(List<NavStatisticsGrpc> statistics, String schemeName) {
        double percentageIncrease = 0d;
        try {
            if (Objects.nonNull(statistics)) {

                Optional<NavStatisticsGrpc> fiveY = statistics.stream().filter(s -> s.getTenorValue() == FIVEY.getTenorValue()).findAny();
                Optional<NavStatisticsGrpc> oneD = statistics.stream()
                        .filter(s -> /*s.getDate().isAfter(LocalDate.now().minusDays(30)) &&*/ s.getTenorValue() == TenorEnum.ONED.getTenorValue()).findAny(); //TODO check this

                if (oneD.isPresent()) {
                    if (fiveY.isPresent() && fiveY.get().getNav() != 0d) {
                        BigDecimal increase = BigDecimal.valueOf(oneD.get().getNav()).subtract(BigDecimal.valueOf(fiveY.get().getNav()));
                        percentageIncrease = increase.divide(BigDecimal.valueOf(fiveY.get().getNav()), 2, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100")).doubleValue();
                    } else {
                        Optional<NavStatisticsGrpc> inception = statistics.stream().filter(s -> s.getTenorValue() == INCEPTION.getTenorValue()).findAny();

                        if (inception.isPresent() && inception.get().getNav() != 0d) {
                            BigDecimal increase = BigDecimal.valueOf(oneD.get().getNav()).subtract(BigDecimal.valueOf(inception.get().getNav()));
                            percentageIncrease = increase.divide(BigDecimal.valueOf(inception.get().getNav()), 2, RoundingMode.DOWN)
                                    .multiply(new BigDecimal("100")).doubleValue();
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
