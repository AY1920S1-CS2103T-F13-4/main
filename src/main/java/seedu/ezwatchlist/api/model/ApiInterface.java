package seedu.ezwatchlist.api.model;

import java.util.List;
import seedu.ezwatchlist.model.show.Movie;
import seedu.ezwatchlist.model.show.TvShow;

/**
 * Interface to retrieve information from online API.
 * Methods used here will return the information for movie
 */
public interface ApiInterface {
    public List<Movie> getMovieByName(String name);

    public List<TvShow> getTvShowByName(String name);
}