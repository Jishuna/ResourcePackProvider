package me.jishuna.resourcepackprovider;

import java.util.Objects;

/**
 * Represents a SemanticVersion <br>
 * See <a href=https://semver.org/>https://semver.org/</a>
 */
final class SemanticVersion {

    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Create a new semantic version.
     *
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     */
    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Creates a SemanticVersion from the provided string.
     *
     * @param version the version string, must be in the format major.minor.patch
     * @return a SemanticVersion for the provided string
     * @throws IllegalArgumentException if the string is in an invalid format
     */
    public static SemanticVersion fromString(String version) {
        version = version.replaceAll("[^\\d.]", "");
        String[] parts = version.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException("String must be in the format major.minor.patch");
        }

        SemanticVersion semVersion = null;
        try {
            semVersion = new SemanticVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("String must be in the format major.minor.patch");
        }
        return semVersion;
    }

    /**
     * Checks if this SemanticVersion is newer than the provided version.
     *
     * @param other the version to compare with
     * @return true if newer, false otherwise
     */
    public boolean isNewerThan(SemanticVersion other) {
        if (this.major != other.major) {
            return this.major > other.major;
        }
        if (this.minor != other.minor) {
            return this.minor > other.minor;
        }
        if (this.patch != other.patch) {
            return this.patch > other.patch;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.major, this.minor, this.patch);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SemanticVersion other)) {
            return false;
        }
        return this.major == other.major && this.minor == other.minor && this.patch == other.patch;
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor + "." + this.patch;
    }
}
