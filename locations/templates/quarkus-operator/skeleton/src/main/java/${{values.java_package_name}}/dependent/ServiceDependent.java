package ${{values.java_package_name}}.dependent;

import ${{values.java_package_name}}.QuarkusApplication;
import org.jboss.logging.Logger;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;

public class ServiceDependent extends QuarkusApplicationDependent<Service> {

  private static final Logger LOG = Logger.getLogger(ServiceDependent.class);

  public ServiceDependent() {
    super(Service.class);
  }

  @Override
  public Service desired(QuarkusApplication quarkusApp, Context context) {

    final var name = quarkusApp.getMetadata().getName();
    final var namespace = quarkusApp.getMetadata().getNamespace();
    final var labels = getLabels(quarkusApp);
    LOG.debugf("Creating Service: %s in namespace: %s", name, namespace);
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(name)
        .withNamespace(namespace)
        .withLabels(labels)
        .endMetadata()
        .withNewSpec()
        .addNewPort()
        .withName("http")
        .withPort(8080)
        .withNewTargetPort().withValue(8080).endTargetPort()
        .endPort()
        .withSelector(labels)
        .withType("ClusterIP")
        .endSpec()
        .build();
  }
}
