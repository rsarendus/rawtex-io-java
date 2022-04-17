package ee.ristoseene.rawtex.io.core.common.test;

import org.junit.jupiter.api.Assertions;

public final class TestVersionUtils {

    public static int parseMajorFromVersionString(String versionString) {
        return Integer.parseInt(splitVersionString(versionString)[0]);
    }

    public static int parseMinorFromVersionString(String versionString) {
        return Integer.parseInt(splitVersionString(versionString)[1]);
    }

    private static String[] splitVersionString(String versionString) {
        String[] versionComponents = versionString.split("\\.");
        if (versionComponents.length != 2) {
            Assertions.fail("Invalid version string: " + versionString);
        }
        return versionComponents;
    }

    private TestVersionUtils() {}

}
