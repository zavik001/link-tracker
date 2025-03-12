package backend.academy.bot.util;

import java.util.regex.Pattern;

public class LinkValidator {
    // GitHub: supports repositories, issues, pull requests, discussions, releases, actions, wiki, tags, branches
    private static final Pattern GITHUB_PATTERN = Pattern.compile(
            "https://github\\.com/[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+(?:/(?:issues|pull|discussions|releases)/\\d+|/(?:actions|wiki|tags|branches)(?:/[a-zA-Z0-9_.-]+)?)?",
            Pattern.CASE_INSENSITIVE);

    // Stack Overflow: supports questions, answers, tags, and users
    private static final Pattern STACK_OVERFLOW_PATTERN =
            Pattern.compile("https://stackoverflow\\.com/(questions|users|tags)/\\d+(/.*)?");

    // Stack Exchange: general support for all Stack Exchange network sites (SuperUser, AskUbuntu, etc.)
    private static final Pattern STACK_EXCHANGE_PATTERN = Pattern.compile(
            "https://[^/]+\\.stackexchange\\.com/(questions|users)/\\d+(/.*)?|https://[^/]+\\.stackexchange\\.com/tags/[^/]+");

    public static boolean isValidLink(String url) {
        return GITHUB_PATTERN.matcher(url).matches()
                || STACK_OVERFLOW_PATTERN.matcher(url).matches()
                || STACK_EXCHANGE_PATTERN.matcher(url).matches();
    }
}
