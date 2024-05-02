package ${{values.java_package_name}}.dependent;

import ${{values.java_package_name}}.QuarkusApplication;
import org.jboss.logging.Logger;

import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(resourceDiscriminator = OutputImageStreamDiscriminator.class)
public class OutputImageStreamDependent extends QuarkusApplicationDependent<ImageStream> {

  private static final Logger LOG = Logger.getLogger(OutputImageStreamDependent.class);
  private static final String BUILDER_IMAGE_REPO = "registry.access.redhat.com/ubi8/openjdk-17";

  public OutputImageStreamDependent() {
    super(ImageStream.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ImageStream desired(QuarkusApplication quarkusApp, Context context) {
    final var name = quarkusApp.getMetadata().getName();
    final var namespace = quarkusApp.getMetadata().getNamespace();
    final var labels = getLabels(quarkusApp);

    LOG.debugf("Creating output ImageStream: %s in namespace: %s", name, namespace);
    return new ImageStreamBuilder()
        .withNewMetadata()
        .withName(name)
        .withNamespace(namespace)
        .withLabels(labels)
        .endMetadata()
        .withNewSpec()
        .withNewLookupPolicy().withLocal(true).endLookupPolicy()
        .endSpec()
        .build();
  }

}
