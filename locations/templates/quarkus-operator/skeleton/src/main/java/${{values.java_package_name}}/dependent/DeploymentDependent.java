package ${{values.java_package_name}}.dependent;

import static ${{values.java_package_name}}.Util.version;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ${{values.java_package_name}}.QuarkusApplication;
import org.jboss.logging.Logger;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;

public class DeploymentDependent extends QuarkusApplicationDependent<Deployment> {

  private static final Logger LOG = Logger.getLogger(DeploymentDependent.class);

  public DeploymentDependent() {
    super(Deployment.class);
  }

  public Deployment desired(QuarkusApplication quarkusApp, Context context) {
    final var name = quarkusApp.getMetadata().getName();
    final var namespace = quarkusApp.getMetadata().getName();
    final var labels = getLabels(quarkusApp);
    final var spec = quarkusApp.getSpec();
    final var coordinates = spec.getCoordinates();
    final var env = asList(spec.getEnv());
    final var version = version(coordinates);

    LOG.debugf("Creating Deployment: %s in namespace: %s", name, namespace);
    return new DeploymentBuilder()
        .withMetadata(createMetadata(quarkusApp, labels))
        .withNewSpec()
        .withNewSelector().withMatchLabels(labels).endSelector()
        .withNewTemplate()
        .withNewMetadata().withLabels(labels).endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName(name)
        .withImage(name + ":" + version)
        .addNewPort()
        .withName("http").withProtocol("TCP").withContainerPort(8080)
        .endPort()
        .withEnv(env)
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();
  }

  @Override
  public Result<Deployment> match(Deployment actual, QuarkusApplication primary, Context<QuarkusApplication> context) {
    final var desiredSpec = primary.getSpec();
    final var container = actual.getSpec().getTemplate().getSpec().getContainers().stream().findFirst();
    final var desiredImageRef = primary.getMetadata().getName() + ":" + version(desiredSpec.getCoordinates());
    final Result<Deployment> result = Result.nonComputed(container.map(c -> c.getImage().equals(desiredImageRef)
        && asList(desiredSpec.getEnv()).equals(c.getEnv())).orElse(false));
    return result;
  }

  private List<EnvVar> asList(Map<String, String> env) {
    if (env == null) {
      return Collections.emptyList();
    }
    return env.entrySet().stream().map(e -> new EnvVarBuilder().withName(e.getKey()).withValue(e.getValue()).build())
        .collect(Collectors.toList());
  }
}
