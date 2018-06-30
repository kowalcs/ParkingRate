package pl.touk.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ParkingRate {

    private Long id;
    private CustomerType customerType;
    private Double firstHour;
    private Double secondHour;
    private Double factor;
    private CurrencyType currencyType;

}
