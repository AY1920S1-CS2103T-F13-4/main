package seedu.ezwatchlist.logic.parser;

import seedu.ezwatchlist.api.exceptions.OnlineConnectionException;
import seedu.ezwatchlist.commons.core.Messages;
import seedu.ezwatchlist.logic.commands.AddCommand;
import seedu.ezwatchlist.logic.commands.SyncCommand;
import seedu.ezwatchlist.logic.parser.exceptions.ParseException;
import seedu.ezwatchlist.model.actor.Actor;
import seedu.ezwatchlist.model.show.*;

import seedu.ezwatchlist.api.*;

import java.util.Set;
import java.util.stream.Stream;

import static seedu.ezwatchlist.logic.parser.CliSyntax.*;
import static seedu.ezwatchlist.logic.parser.CliSyntax.PREFIX_ACTOR;

public class SyncCommandParser implements Parser<SyncCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an SyncCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    @Override
    public SyncCommand parse(String args) throws ParseException, OnlineConnectionException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, SyncCommand.MESSAGE_USAGE));
        }

        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());

        return new SyncCommand(new ApiMain().getMovieByName(name.showName));
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
