package org.chainoptim.desktop.shared.features.location.model;

import lombok.Getter;

@Getter
public class Location {

    private Integer id;
    private String address;
    private String city;
    private String state;
    private String country;
    private Double latitude;
    private Double longitude;
    private String zipCode;
    private Integer organizationId;

    public String getFormattedLocation() {
        String formattedCity = (city != null && !city.isEmpty()) ? city : "";
        String formattedState = (state != null && !state.isEmpty()) ? ", " + state : "";
        String formattedCountry = (country != null && !country.isEmpty()) ? ", " + country : "";

        return formattedCity + formattedState + formattedCountry;
    }

}
