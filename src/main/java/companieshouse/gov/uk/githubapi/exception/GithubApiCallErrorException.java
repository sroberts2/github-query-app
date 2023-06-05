package companieshouse.gov.uk.githubapi.exception;

public class GithubApiCallErrorException extends RuntimeException {

    public GithubApiCallErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
