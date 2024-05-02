package ${{values.java_package_name}};

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.fabric8.openshift.api.model.Build;
import io.quarkus.container.image.openshift.deployment.BuildStatus;

public final class Util {

  private static final Logger LOG = Logger.getLogger(Util.class);

  private static final String ILLEGAL_COORDINATES_FORMAT = "Invalid coordinates: %s. Please use <groupId>:<artifactId>:<version> format!";
  private static final String DEFAULT_REPOSITORY_URL = "https://repo1.maven.org/maven2";

  private Util() {
    //Utility class
  }

  //
  // Maven Coordinates Utilities
  //
  public static String groupId(String coordinates) {
    try {
      return coordinates.split(":")[0];
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(String.format(ILLEGAL_COORDINATES_FORMAT, coordinates));
    }
  }

  public static String artifactId(String coordinates) {
    try {
      return coordinates.split(":")[1];
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(String.format(ILLEGAL_COORDINATES_FORMAT, coordinates));
    }
  }

  public static String version(String coordinates) {
    try {
      return coordinates.split(":")[2];
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(String.format(ILLEGAL_COORDINATES_FORMAT, coordinates));
    }
  }

  public static Optional<String> classifier(String coordinates) {
    String[] parts = coordinates.split(":");
    if (parts.length >= 5) {
      return Optional.of(parts[4]);
    }
    return Optional.empty();
  }

  public static String type(String coordinates) {
    String[] parts = coordinates.split(":");
    if (parts.length > 3) {
      return parts[3];
    }
    return "jar";
  }

  public static String artifactFileName(String coordinates) {
    return artifactId(coordinates) + "-" + version(coordinates) + classifier(coordinates).map(c -> "-" + c).orElse("") + "."
        + type(coordinates);
  }

  //
  //
  //
  public static Path download(String coordinates) {
    return download(Optional.empty(), coordinates);
  }

  public static Path download(Optional<String> repositoryUrl, String coordinates) {
    try {
      String groupId = groupId(coordinates);
      String artifactId = artifactId(coordinates);
      String version = version(coordinates);
      Optional<String> classifier = classifier(coordinates);
      String type = type(coordinates);
      String fileName = artifactFileName(coordinates);
      Path destination = Files.createTempDirectory("maven-artifact").resolve(fileName);
      download(repositoryUrl, groupId, artifactId, classifier, version, type, destination);
      LOG.infof("Downloaded artifact at: %s", destination.toAbsolutePath());
      return destination;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void download(Optional<String> repositoryUrl, String groupId, String artifactId, Optional<String> classifier,
      String version, String type, Path destination) {
    String artifactPath = groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version
        + classifier.map(c -> "-" + c).orElse("") + "." + type;
    String baseUrl = repositoryUrl.orElse(DEFAULT_REPOSITORY_URL);
    String downloadUrl = baseUrl.endsWith("/") ? baseUrl + artifactPath : baseUrl + "/" + artifactPath;
    LOG.infof("Downloading: %s", downloadUrl);

    try {
      URL url = new URL(downloadUrl);
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

      try (InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
          FileOutputStream outputStream = new FileOutputStream(destination.toFile())) {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      } finally {
        httpURLConnection.disconnect();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  //
  // OpenShift Build utilities
  //

  public static boolean isNew(Build build) {
    return build != null && build.getStatus() != null
        && BuildStatus.New.name().equalsIgnoreCase(build.getStatus().getPhase());
  }

  public static boolean isPending(Build build) {
    return build != null && build.getStatus() != null
        && BuildStatus.Pending.name().equalsIgnoreCase(build.getStatus().getPhase());
  }

  public static boolean isRunning(Build build) {
    return build != null && build.getStatus() != null
        && BuildStatus.Running.name().equalsIgnoreCase(build.getStatus().getPhase());
  }

  public static boolean isComplete(Build build) {
    return build != null && build.getStatus() != null
        && BuildStatus.Complete.name().equalsIgnoreCase(build.getStatus().getPhase());
  }

  public static boolean isFailed(Build build) {
    return build != null && build.getStatus() != null
        && BuildStatus.Failed.name().equalsIgnoreCase(build.getStatus().getPhase());
  }

  public static boolean isError(Build build) {
    return build != null && build.getStatus() != null
        && BuildStatus.Error.name().equalsIgnoreCase(build.getStatus().getPhase());
  }

  public static boolean isCancelled(Build build) {
    return build != null && build.getStatus() != null
        && BuildStatus.Cancelled.name().equalsIgnoreCase(build.getStatus().getPhase());
  }
}
