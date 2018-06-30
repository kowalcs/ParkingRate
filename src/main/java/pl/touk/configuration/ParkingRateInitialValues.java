package pl.touk.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.touk.model.CurrencyType;
import pl.touk.model.CustomerType;
import pl.touk.model.ParkingRate;
import pl.touk.repository.ParkingRateRepository;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Slf4j
@Component("parkingRateInitialValues")
@RequiredArgsConstructor
public class ParkingRateInitialValues {

    private final ParkingRateRepository parkingRateRepository;

    @PostConstruct
    public void init() {
        parkingRateRepository.save(Arrays.asList(
                new ParkingRate(null, CustomerType.REGULAR, 1.0, 2.0, 1.5, CurrencyType.PLN),
                new ParkingRate(null, CustomerType.VIP, 0.0, 2.0, 1.2, CurrencyType.PLN))
        );
    }
}
