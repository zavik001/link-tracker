package backend.academy.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import backend.academy.bot.util.LinkValidator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LinkValidatorTest {

    @Test
    void shouldValidateCorrectGitHubLinks() {
        var validLinks = Stream.of(
                "https://github.com/user/repo",
                "https://github.com/user/repo/issues/123",
                "https://github.com/user/repo/pull/42",
                "https://github.com/user/repo/discussions/7",
                "https://github.com/user/repo/actions",
                "https://github.com/user/repo/wiki",
                "https://github.com/user/repo/tags",
                "https://github.com/user/repo/branches/main");

        assertAll(validLinks.map(
                link -> () -> assertThat(LinkValidator.isValidLink(link)).isTrue()));
    }

    @Test
    void shouldInvalidateIncorrectGitHubLinks() {
        // Arrange
        var invalidLinks = Stream.of(
                "https://github.com/user",
                "https://github.com/user/repo/unknown/123",
                "https://gitlab.com/user/repo",
                "https://github.com/user_repo/issues/123");

        // Act & Assert
        assertAll(invalidLinks.map(
                link -> () -> assertThat(LinkValidator.isValidLink(link)).isFalse()));
    }

    @Test
    void shouldValidateCorrectStackExchangeLinks() {
        // Arrange
        var validLinks = Stream.of(
                "https://superuser.stackexchange.com/questions/6789",
                "https://askubuntu.stackexchange.com/users/1234",
                "https://math.stackexchange.com/tags/algebra");

        // Act & Assert
        assertAll(validLinks.map(
                link -> () -> assertThat(LinkValidator.isValidLink(link)).isTrue()));
    }

    @Test
    void shouldInvalidateIncorrectStackExchangeLinks() {
        // Arrange
        var invalidLinks = Stream.of(
                "https://randomsite.com/questions/6789",
                "https://notstackexchange.com/tags/java",
                "https://stackoverflow.co/questions/999");

        // Act & Assert
        assertAll(invalidLinks.map(
                link -> () -> assertThat(LinkValidator.isValidLink(link)).isFalse()));
    }
}
