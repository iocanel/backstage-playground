package ${{values.java_package_name}}.condition;

import static ${{values.java_package_name}}.Util.version;

import ${{values.java_package_name}}.QuarkusApplication;
import org.jboss.logging.Logger;

import io.fabric8.openshift.api.model.ImageStream;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class OutputImageStreamReadyCondition implements Condition<ImageStream, QuarkusApplication> {

  private static final Logger LOG = Logger.getLogger(OutputImageStreamReadyCondition.class);

  @Override
  public boolean isMet(DependentResource<ImageStream, QuarkusApplication> dependentResource, QuarkusApplication primary,
      Context<QuarkusApplication> context) {
    var ready = false;
    var name = primary.getMetadata().getName();

    try {
      var coordinates = primary.getSpec().getCoordinates();
      var version = version(coordinates);
      var imageStream = dependentResource.getSecondaryResource(primary, context);
      ready = imageStream.map(i -> i.getStatus() != null &&
          i.getStatus().getTags() != null &&
          i.getStatus().getTags().stream().anyMatch(t -> t.getTag().equals(version)))
          .orElse(false);
      LOG.debugf("Output image stream %s is ready: %s", name, ready);
    } catch (Exception e) {
      LOG.warnf("Output image stream %s is ready: %s, due to error:", name, ready, e.getMessage());
    }
    return ready;
  }
}
