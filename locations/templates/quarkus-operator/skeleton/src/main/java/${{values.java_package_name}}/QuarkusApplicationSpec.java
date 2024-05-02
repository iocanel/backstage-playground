package ${{values.java_package_name}};

import java.util.Map;

public class QuarkusApplicationSpec {
  private String coordinates;
  private String repositoryUrl;
  private Map<String, String> env;

  public QuarkusApplicationSpec() {
  }

  public QuarkusApplicationSpec(String coordinates, String repositoryUrl, Map<String, String> env) {
    this.coordinates = coordinates;
    this.repositoryUrl = repositoryUrl;
    this.env = env;
  }

  public String getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(String coordinates) {
    this.coordinates = coordinates;
  }

  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }

  public Map<String, String> getEnv() {
    return env;
  }

  public void setEnv(Map<String, String> env) {
    this.env = env;
  }

}
