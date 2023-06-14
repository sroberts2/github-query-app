package uk.gov.companieshouse.githubapi.exception;

public class PoolyFormattedDependenciesFileException extends RuntimeException {
    

    public PoolyFormattedDependenciesFileException(final String message) {
        super(message);
    }

    public PoolyFormattedDependenciesFileException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
