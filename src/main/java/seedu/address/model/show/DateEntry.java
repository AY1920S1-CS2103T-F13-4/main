package seedu.address.model.show;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateEntry {

    public final Date date;
    public final SimpleDateFormat formatter;



    public final String dateEntry;
    public static final String MESSAGE_CONSTRAINTS = "Dates can take any values, and it should not be blank";

    public DateEntry() {
        this.date = new Date();
        this.formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        dateEntry = formatter.format(date);
    }

    public String getDateEntry() {
        return dateEntry;
    }

    @Override
    public String toString() {
        return dateEntry;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DateEntry // instanceof handles nulls
                && dateEntry.equals(((DateEntry) other).dateEntry)); // state check
    }

    @Override
    public int hashCode() {
        return dateEntry.hashCode();
    }
}
