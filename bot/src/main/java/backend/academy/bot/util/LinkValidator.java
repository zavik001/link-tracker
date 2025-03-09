package backend.academy.bot.util;

import java.util.regex.Pattern;

public class LinkValidator {
    // GitHub: supports repositories, issues, pull requests, discussions, releases, actions, wiki, tags, branches
    private static final Pattern GITHUB_PATTERN = Pattern.compile(
            "https://github\\.com/[^/]+/[^/]+(?:/(issues|pull|discussions|releases|actions|wiki|tags|branches)/\\d+)?");

    // Stack Overflow: supports questions, answers, tags, and users
    private static final Pattern STACK_OVERFLOW_PATTERN =
            Pattern.compile("https://stackoverflow\\.com/(questions|users|tags)/\\d+(/.*)?");

    // Stack Exchange: general support for all Stack Exchange network sites (SuperUser, AskUbuntu, etc.)
    private static final Pattern STACK_EXCHANGE_PATTERN =
            Pattern.compile("https://[^/]+\\.stackexchange\\.com/(questions|users|tags)/\\d+(/.*)?");

    public static boolean isValidLink(String url) {
        return GITHUB_PATTERN.matcher(url).matches()
                || STACK_OVERFLOW_PATTERN.matcher(url).matches()
                || STACK_EXCHANGE_PATTERN.matcher(url).matches();
    }
}
