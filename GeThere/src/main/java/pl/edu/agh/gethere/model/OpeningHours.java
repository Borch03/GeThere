package pl.edu.agh.gethere.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Dominik on 13.01.2017.
 */

@SuppressWarnings("serial")
public class OpeningHours implements Serializable {

    private Date openingHour;
    private Date closingHour;

    public OpeningHours(Date openingHour, Date closingHour) {
        this.openingHour = openingHour;
        this.closingHour = closingHour;
    }

    public Date getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(Date openingHour) {
        this.openingHour = openingHour;
    }

    public Date getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(Date closingHour) {
        this.closingHour = closingHour;
    }
}
