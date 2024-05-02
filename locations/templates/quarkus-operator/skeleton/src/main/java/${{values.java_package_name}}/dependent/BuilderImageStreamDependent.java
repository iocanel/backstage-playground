package ${{values.java_package_name}}.dependent;

import ${{values.java_package_name}}.QuarkusApplication;
import org.jboss.logging.Logger;

import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(resourceDiscriminator = BuilderImageStreamDiscriminator.class)
public class BuilderImageStreamDependent extends QuarkusApplicationDependent<ImageStream> {

  private static final Logger LOG = Logger.getLogger(BuilderImageStreamDependent.class);
  public static final String BUILDER_IMAGE_NAME = "openjdk-17";
  public static final String BUILDER_IMAGE_TAG = "1.18";
  public static final String BUILDER_IMAGE_REPO = "registry.access.redhat.com/ubi8/openjdk-17";

  public BuilderImageStreamDependent() {
    super(ImageStream.class);
  }

  @Override
  public ImageStream desired(QuarkusApplication quarkusApp, Context context) {
    var name = BUILDER_IMAGE_NAME;
    var namespace = quarkusApp.getMetadata().getNamespace();

    LOG.debugf("Creating builder ImageStream: %s in namespace: %s", name, namespace);
    return new ImageStreamBuilder()
        .withNewMetadata()
        .withName(name)
        .withNamespace(namespace)
        .endMetadata()
        .withNewSpec()
        .withDockerImageRepository(BUILDER_IMAGE_REPO)
        .withNewLookupPolicy().withLocal(true).endLookupPolicy()
        .endSpec()
        .build();
  }
}
