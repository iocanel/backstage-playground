package ${{values.java_package_name}}.condition;

import static ${{values.java_package_name}}.dependent.BuilderImageStreamDependent.BUILDER_IMAGE_NAME;
import static ${{values.java_package_name}}.dependent.BuilderImageStreamDependent.BUILDER_IMAGE_TAG;

import ${{values.java_package_name}}.QuarkusApplication;
import org.jboss.logging.Logger;

import io.fabric8.openshift.api.model.ImageStream;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class BuilderImageStreamReadyCondition implements Condition<ImageStream, QuarkusApplication> {

  private static final Logger LOG = Logger.getLogger(BuilderImageStreamReadyCondition.class);

  @Override
  public boolean isMet(DependentResource<ImageStream, QuarkusApplication> dependentResource, QuarkusApplication primary,
      Context<QuarkusApplication> context) {
    var ready = false;
    var name = BUILDER_IMAGE_NAME;
    try {
      var imageStream = dependentResource.getSecondaryResource(primary, context);
      ready = imageStream.map(i -> i.getStatus() != null &&
          i.getStatus().getTags() != null &&
          i.getStatus().getTags().stream().anyMatch(t -> t.getTag().equals(BUILDER_IMAGE_TAG)))
          .orElse(false);

      LOG.debugf("Builder image stream %s is ready: %s", name, ready);
    } catch (Exception e) {
      LOG.warnf("Builder image stream %s is ready: %s, due to error:", name, ready, e.getMessage());
    }
    return ready;
  }
}
