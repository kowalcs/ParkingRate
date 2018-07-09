package pl.touk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import pl.touk.model.CustomerType;
import pl.touk.model.Driver;
import pl.touk.model.ParkingRate;
import pl.touk.repository.DriverRepository;
import pl.touk.repository.ParkingRateRepository;
import pl.touk.service.dto.NewDriverConverter;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@DependsOn({"parkingRateInitialValues"})
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingRateService {

    private final DriverRepository driverRepository;
    private final ParkingRateRepository parkingRateRepository;

    private ParkingRate regular;
    private ParkingRate vip;
    private Double payment = 0.0;

    @PostConstruct
    public void init() {
        List<ParkingRate> rates = parkingRateRepository.findAll();
        rates.forEach(rate -> {
            if (rate.getCustomerType() == CustomerType.VIP) {
                vip = rate;
            } else if (rate.getCustomerType() == CustomerType.REGULAR) {
                regular = rate;
            } else {
                throw new IllegalStateException("Inconsistent configuration");
            }
        });
    }

    public Double calculateFee(Driver driver) {
        int hour = driver.getParkDuration();
        driver.setVip(isVip(driver.getRegistrationNumber()));
        if (driver.getVip()) {
            payment = calculate(hour, vip);
        } else {
            payment = calculate(hour, regular);
        }
        return payment;
    }

    private Double calculate(int hour, ParkingRate configuration) {
        if (hour <= 1) {
            payment = configuration.getFirstHour();
        } else if (hour <= 2) {
            payment = configuration.getFirstHour() + configuration.getSecondHour();
        } else {
            payment = configuration.getFirstHour() + configuration.getSecondHour();
            double prevHour = configuration.getSecondHour();
            for (int i = 2; i < hour; i++) {
                prevHour = prevHour * configuration.getFactor();
                payment = payment + prevHour;
            }
        }
        return payment;
    }

    public boolean isVip(String registrationNumber) {
        return parkingRateRepository.isVip(registrationNumber);
    }

    public boolean checkStartMeterForDriver(String registrationNumber) {
        log.debug("Check driver by " + registrationNumber);
        return driverRepository.findDriver(registrationNumber)
                .filter(driver -> driver.getStartParkTime() != null && driver.getEndParkTime() == null)
                .map(driver -> true)
                .orElse(false);
    }

    public Double allDayEarnings(String date) throws ParseException {
        try {
            final Double[] earnings = {0.0};
            driverRepository.getAllForDay(new SimpleDateFormat("dd-MM-yyyy").parse(date))
                    .stream()
                    .filter(d -> d.getPayment() != null)
                    .forEach(d -> earnings[0] += d.getPayment());
            return earnings[0];
        } catch (ParseException ex) {
            throw new ParseException("Incorrect date: '" + date + "' format. Correct: [dd-MM-yyyy]", ex.getErrorOffset());
        }
    }
}