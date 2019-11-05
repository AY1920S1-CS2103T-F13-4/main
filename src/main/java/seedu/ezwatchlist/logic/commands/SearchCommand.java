package seedu.ezwatchlist.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.ezwatchlist.api.exceptions.OnlineConnectionException;
import seedu.ezwatchlist.api.model.ApiInterface;
import seedu.ezwatchlist.api.model.ApiManager;
import seedu.ezwatchlist.commons.core.Messages;
import seedu.ezwatchlist.logic.commands.exceptions.CommandException;
import seedu.ezwatchlist.model.Model;
import seedu.ezwatchlist.model.actor.Actor;
import seedu.ezwatchlist.model.show.Genre;
import seedu.ezwatchlist.model.show.Movie;
import seedu.ezwatchlist.model.show.Name;
import seedu.ezwatchlist.model.show.Show;
import seedu.ezwatchlist.model.show.TvShow;

/**
 * Finds and lists all shows in watchlist whose name contains any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class SearchCommand extends Command {
    public static final String COMMAND_WORD = "search";
    public static final String MESSAGE_USAGE = "No result found.\n"
            + COMMAND_WORD + ": Searches for shows whose names contain any of the given keywords from the watchlist, "
            + "watched list and online.\n"
            + "- by name: search n/SHOW_NAME… [g/GENRE]… [a/ACTOR_NAME]… [i/IS_INTERNAL] [t/TYPE] [w/IS_WATCH]\n"
            + "- by genre: search g/GENRE… [n/SHOW_NAME]… [a/ACTOR_NAME]… [i/IS_INTERNAL] [t/TYPE] [w/IS_WATCH]\n"
            + "- by actor (from watchlist): search a/ACTOR_NAME…\u200B [n/SHOW_NAME]… [g/GENRE]… [t/TYPE] "
            + "[w/IS_WATCH]\n";
    private static final String MESSAGE_INVALID_IS_INTERNAL_COMMAND =
            "Invalid input. i/[OPTION] where OPTION is either true/yes or false/no";
    private static final String MESSAGE_INVALID_TYPE_COMMAND =
            "Invalid input. t/[TYPE] where TYPE is either movie or tv";

    private static final String EMPTY_STRING = "";
    private static final String INPUT_TRUE = "true";
    private static final String INPUT_YES = "yes";
    private static final String INPUT_FALSE = "false";
    private static final String INPUT_NO = "no";
    private static final String INPUT_MOVIE = "movie";
    private static final String INPUT_TV = "tv";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ACTOR = "actor";
    private static final String KEY_GENRE = "genre";
    private static final String KEY_IS_WATCHED = "is_watched";
    private static final String KEY_IS_INTERNAL = "is_internal";

    private ApiInterface onlineSearch;
    private List<String> nameList;
    private List<String> typeList;
    private List<String> actorList;
    private List<String> genreList;
    private List<String> isWatchedList;
    private List<String> isInternalList;

    private List<Show> searchResult = new ArrayList<>();

    public SearchCommand(HashMap<String, List<String>> searchShowsHashMap) {
        nameList = searchShowsHashMap.get(KEY_NAME);
        typeList = searchShowsHashMap.get(KEY_TYPE);
        actorList = searchShowsHashMap.get(KEY_ACTOR); // unable to search online
        genreList = searchShowsHashMap.get(KEY_GENRE); // unable to search at all
        isWatchedList = searchShowsHashMap.get(KEY_IS_WATCHED);
        isInternalList = searchShowsHashMap.get(KEY_IS_INTERNAL);
        try {
            onlineSearch = new ApiManager();
        } catch (OnlineConnectionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            /*if (!nameList.isEmpty() && !actorList.isEmpty()) {
                searchByName(model);
                searchByActor(model);
                // add shows with the show name and also shows with the actors involved
                // ensure no duplicates, currently might have duplicate
                // for now, do not allow users to search for both name and actor at the same time
            } */
            if (!nameList.isEmpty()) {
                searchByName(model);
            }
            if (!actorList.isEmpty()) {
                searchByActor(model);
            }
            if (!genreList.isEmpty()) {
                searchByGenre(model);
            }
            if (searchResult.isEmpty()) { // User only input search
                throw new CommandException(MESSAGE_USAGE);
            }
            /*else { // if has no name and actor to be searched
                //searchByType();
                //searchByIsWatched();
                //searchByIsInternal();
            }*/

            model.updateSearchResultList(searchResult);
            return new CommandResult(String.format(Messages.MESSAGE_SHOWS_LISTED_OVERVIEW,
                    model.getSearchResultList().size()));
        } catch (OnlineConnectionException e) {
            return null;
        }
    }

    /**
     * Search for shows by name.
     * @param model Model used.
     * @throws CommandException If command exception occurred.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void searchByName(Model model) throws CommandException, OnlineConnectionException {
        if (requestedSearchFromInternal()) {
            for (String showName : nameList) {
                addShowFromWatchListIfSameNameAs(showName, model);
            }
        } else if (requestedSearchFromOnline()) {
            for (String showName : nameList) {
                addShowFromOnlineIfSameNameAs(showName);
            }
        } else if (requestedIsInternal()) {
            throw new CommandException(MESSAGE_INVALID_IS_INTERNAL_COMMAND);
        } else { // there's no restriction on where to search from
            for (String showName : nameList) {
                addShowFromWatchListIfSameNameAs(showName, model);
                addShowFromOnlineIfSameNameAs(showName);
            }
        }
    }

    /**
     * Search for shows by actor.
     * @param model Model used.
     * @throws CommandException If command exception occurred.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void searchByActor(Model model) throws CommandException, OnlineConnectionException {
        Set<Actor> actorSet = new HashSet<Actor>();
        for (String actorName : actorList) {
            Actor actor = new Actor(actorName);
            actorSet.add(actor);
        }

        if (requestedSearchFromInternal()) {
            addShowFromWatchListIfHasActor(actorSet, model);
        } else if (requestedSearchFromOnline()) {
            //addShowFromOnlineIfHasActor(actorSet); // unable to search online for now
        } else if (requestedIsInternal()) {
            throw new CommandException(MESSAGE_INVALID_IS_INTERNAL_COMMAND);
        } else { // there's no restriction on where to search from
            addShowFromWatchListIfHasActor(actorSet, model);
            // addShowFromOnlineIfHasActor(actorSet); // unable to search online for now
        }
    }

    /**
     * Search for shows by genre.
     * @param model Model used.
     * @throws CommandException If command exception occurred.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void searchByGenre(Model model) throws CommandException, OnlineConnectionException {
        Set<Genre> genreSet = new HashSet<Genre>();
        for (String genreName : genreList) {
            Genre genre = new Genre(genreName);
            genreSet.add(genre);
        }

        if (requestedSearchFromInternal()) {
            addShowFromWatchListIfIsGenre(genreSet, model);
        } else if (requestedSearchFromOnline()) {
            addShowFromOnlineIfIsGenre(genreSet); //unable to search for online tv
        } else if (requestedIsInternal()) {
            throw new CommandException(MESSAGE_INVALID_IS_INTERNAL_COMMAND);
        } else { // there's no restriction on where to search from
            addShowFromWatchListIfIsGenre(genreSet, model);
            addShowFromOnlineIfIsGenre(genreSet); //unable to search for online tv
        }
    }

    /**
     * Adds show from list if it has the same name as in {@code showName}.
     * @param showName name of the given show.
     * @param model current model of the program.
     */
    private void addShowFromWatchListIfSameNameAs(String showName, Model model) {
        if (!showName.equals(EMPTY_STRING)) {
            List<Show> filteredShowList = model.getShowIfHasName(new Name(showName));
            addShowToSearchResult(filteredShowList);
        }
    }

    /**
     * Adds show from list if it has any actor in {@code actorSet}.
     * @param actorSet Set of actors to be searched for.
     * @param model Model used.
     */
    private void addShowFromWatchListIfHasActor(Set<Actor> actorSet, Model model) {
        if (!actorSet.isEmpty()) {
            List<Show> filteredShowList = model.getShowIfHasActor(actorSet);
            addShowToSearchResult(filteredShowList);
        }
    }

    /**
     * Adds show from list if it has any genre in {@code genreSet}.
     * @param genreSet Set of actors to be searched for.
     * @param model Model used.
     */
    private void addShowFromWatchListIfIsGenre(Set<Genre> genreSet, Model model) {
        if (!genreSet.isEmpty()) {
            List<Show> filteredShowList = model.getShowIfIsGenre(genreSet);
            addShowToSearchResult(filteredShowList);
        }
    }

    /**
     * Add show to search result.
     * @param showList List of shows to be added.
     */
    private void addShowToSearchResult(List<Show> showList) {
        for (Show show : showList) {
            if (requestedSearchFromWatched() && !show.isWatched().getIsWatchedBoolean()) {
                continue; // skip if request to be watched but show is not watched
            } else if (requestedSearchFromWatchList() && show.isWatched().getIsWatchedBoolean()) {
                continue; // skip if requested to be in watchlist but show is watched
            } else if (requestedSearchForMovie() && !show.getType().equals("Movie")) {
                continue; // skip if requested search for movie but show is tv
            } else if (requestedSearchForTv() && !show.getType().equals("Tv Show")) {
                continue; // skip if requested search for tv but show is movie
            }
            searchResult.add(show);
        }
    }

    /**
     * Add shows, both movies and tv series, searched by name from online to the search result list.
     * @param showName Name of the show to be searched.
     * @throws OnlineConnectionException If online exception occurred.
     * @throws CommandException If command exception occurred.
     */
    private void addShowFromOnlineIfSameNameAs(String showName) throws OnlineConnectionException, CommandException {
        if (!requestedIsWatched() && !showName.equals(EMPTY_STRING)) {
            if (requestedSearchForMovie()) {
                addOnlineMovieSearchedByNameToResult(showName);
            } else if (requestedSearchForTv()) {
                addOnlineTvSearchedByNameToResult(showName);
            } else if (requestedType()) {
                throw new CommandException(MESSAGE_INVALID_TYPE_COMMAND);
            } else {
                addOnlineMovieSearchedByNameToResult(showName);
                addOnlineTvSearchedByNameToResult(showName);
            }
        }
    }

    /**
     * Add movies, searched by name from online to the search result list.
     * @param showName Name of the show to be searched.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void addOnlineMovieSearchedByNameToResult(String showName) throws OnlineConnectionException {
        List<Movie> movies = onlineSearch.getMovieByName(showName);
        for (Movie movie : movies) {
            searchResult.add(movie);
        }
    }

    /**
     * Add tv series, searched by name from online to the search result list.
     * @param showName Name of the show to be searched.
     * @throws OnlineConnectionException If online exception occurred.
     */
    private void addOnlineTvSearchedByNameToResult(String showName) throws OnlineConnectionException {
        List<TvShow> tvShows = onlineSearch.getTvShowByName(showName);
        for (TvShow tvShow : tvShows) {
            searchResult.add(tvShow);
        }
    }

    /**
     * Add show from online if has actors, taking into account if user makes any other requests.
     * @param actorSet The set of actors to be searched.
     * @throws OnlineConnectionException If online exception occurred.
     * @throws CommandException If command exception occurred.
     */
    private void addShowFromOnlineIfHasActor(Set<Actor> actorSet) throws OnlineConnectionException, CommandException {
        if (!actorSet.isEmpty() && !requestedIsWatched()) { // would not check online if requested to be is watched
            if (requestedSearchForMovie()) {
                //addOnlineMovieSearchedByNameToResult(showName);
            } else if (requestedSearchForTv()) {
                //addOnlineTvSearchedByNameToResult(showName);
            } else if (requestedType()) {
                throw new CommandException(MESSAGE_INVALID_TYPE_COMMAND);
            } else {
                //addOnlineMovieSearchedByNameToResult(showName);
                //addOnlineTvSearchedByNameToResult(showName);
            }
        }
    }

    /**
     * Returns a list of Tv Shows from the API search method.
     *
     * @param genreSet the set of genres that the user wants to search.
     * @throws OnlineConnectionException when not connected to the internet.
     */
    private void addShowFromOnlineIfIsGenre(Set<Genre> genreSet) throws OnlineConnectionException {
        if (!genreSet.isEmpty()) {
            List<Movie> movies = onlineSearch.getMovieByGenre(genreSet);
            for (Movie movie : movies) {
                searchResult.add(movie);
            }
            //List<TvShow> tvShows = onlineSearch.getTvShowByGenre(genreSet); // unable to search for tv show
        }
    }

    /**
     * Return true if user requests to search for tv series or movies only.
     * @return True if user requests to search for tv series or movies only.
     */
    private boolean requestedType() {
        return typeList.size() != 2;
    }

    /**
     * Return true if user requests to search for movies only.
     * @return True if user requests to search for movies only.
     */
    private boolean requestedSearchForMovie() {
        return requestedType() && (typeList.get(0).equals(INPUT_MOVIE));
    }

    /**
     * Return true if user requests to search for tv series only.
     * @return True if user requests to search for tv series only.
     */
    private boolean requestedSearchForTv() {
        return requestedType() && (typeList.get(0).equals(INPUT_TV));
    }

    /**
     * Return true if user requests to search from watch or watched list.
     * @return True if user requests to search from watch or watched list.
     */
    private boolean requestedIsWatched() {
        return !isWatchedList.isEmpty();
    }

    /**
     * Return true if user requests to search from watched list.
     * @return True if user requests to search from watched list.
     */
    private boolean requestedSearchFromWatched() {
        return requestedIsWatched()
                && (isWatchedList.get(0).equals(INPUT_TRUE) || isWatchedList.get(0).equals(INPUT_YES));
    }

    /**
     * Return true if user requests to search from watch list.
     * @return True if user requests to search from watch list.
     */
    private boolean requestedSearchFromWatchList() {
        return requestedIsWatched()
                && (isWatchedList.get(0).equals(INPUT_FALSE) || isWatchedList.get(0).equals(INPUT_NO));
    }

    /**
     * Return true if user requests to search from internal or online.
     * @return True if user requests to search from internal or online.
     */
    private boolean requestedIsInternal() {
        return !isInternalList.isEmpty();
    }

    /**
     * Return true if user requests to search from internal.
     * @return True if user requests to search from internal.
     */
    private boolean requestedSearchFromInternal() {
        return requestedIsInternal()
                && (isInternalList.get(0).equals(INPUT_TRUE) || isInternalList.get(0).equals(INPUT_YES));
    }

    /**
     * Return true if user requests to search from online.
     * @return True if user requests to search from online.
     */
    private boolean requestedSearchFromOnline() {
        return requestedIsInternal()
                && (isInternalList.get(0).equals(INPUT_FALSE) || isInternalList.get(0).equals(INPUT_NO));
    }

    /**
     * Return the list of search results.
     * @return List of search results.
     */
    public List<Show> getSearchResult() {
        return searchResult;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof SearchCommand // instanceof handles nulls
                && nameList.equals(((SearchCommand) other).nameList)
                && typeList.equals(((SearchCommand) other).typeList)
                && actorList.equals(((SearchCommand) other).actorList)
                && isWatchedList.equals(((SearchCommand) other).isWatchedList)
                && isInternalList.equals(((SearchCommand) other).isInternalList));
    }
}
