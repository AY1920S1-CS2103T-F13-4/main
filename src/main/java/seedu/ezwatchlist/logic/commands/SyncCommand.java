package seedu.ezwatchlist.logic.commands;

import seedu.ezwatchlist.logic.commands.exceptions.CommandException;
import seedu.ezwatchlist.model.Model;
import seedu.ezwatchlist.model.show.Movie;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static seedu.ezwatchlist.logic.parser.CliSyntax.PREFIX_NAME;

public class SyncCommand extends Command {

    public static final String COMMAND_WORD = "sync";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sync a database from IMDB to the watchlist. "
            + PREFIX_NAME + "NAME "
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "Joker ";

    public static final String MESSAGE_SUCCESS = "Sync movie: %1$s";
    public static final String MESSAGE_DUPLICATE_SHOW = "?"; //"This show already exists in the watchlist";

    private List<Movie> toSync;

    public SyncCommand(List<Movie> toSync){
        requireNonNull(toSync);
        this.toSync = toSync;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        /*if (model.hasShow(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_SHOW);
        }*/

        model.SyncMovie(toSync);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toSync));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof SyncCommand // instanceof handles nulls
                && toSync.equals(((SyncCommand) other).toSync));
    }

}
