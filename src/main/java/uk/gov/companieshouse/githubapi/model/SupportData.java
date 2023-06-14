package uk.gov.companieshouse.githubapi.model;

public class SupportData {

    private String cycle;
    private String supportedJavaVersions;
    private String releaseDate;
    private String eol;
    private String extendedSupport;
    private String latest;
    private String latestReleaseDate;
    private String lts;

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getSupportedJavaVersions() {
        return supportedJavaVersions;
    }

    public void setSupportedJavaVersions(String supportedJavaVersions) {
        this.supportedJavaVersions = supportedJavaVersions;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getEol() {
        return eol;
    }

    public void setEol(String eol) {
        this.eol = eol;
    }

    public String getExtendedSupport() {
        return extendedSupport;
    }

    public void setExtendedSupport(String extendedSupport) {
        this.extendedSupport = extendedSupport;
    }

    public String getLatest() {
        return latest;
    }

    public void setLatest(String latest) {
        this.latest = latest;
    }

    public String getLatestReleaseDate() {
        return latestReleaseDate;
    }

    public void setLatestReleaseDate(String latestReleaseDate) {
        this.latestReleaseDate = latestReleaseDate;
    }

    public String getLts() {
        return lts;
    }

    public void setLts(String lts) {
        this.lts = lts;
    }

    @Override
    public String toString() {
        return "SupportData{"
                + "cycle='"
                + cycle
                + '\'' 
                + ", supportedJavaVersions='"
                + supportedJavaVersions
                + '\''
                + ", releaseDate='"
                + releaseDate
                + '\''
                + ", eol='"
                + eol
                + '\''
                + ", extendedSupport='"
                + extendedSupport
                + '\''
                + ", latest='"
                + latest
                + '\''
                + ", latestReleaseDate='"
                + latestReleaseDate
                + '\''
                + ", lts='"
                + lts
                + '\''
                + '}';
    }
}
