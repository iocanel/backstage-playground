package ${{values.java_package_name}}.dependent;

import ${{values.java_package_name}}.QuarkusApplication;

import io.fabric8.openshift.api.model.ImageStream;
import io.javaoperatorsdk.operator.api.reconciler.ResourceIDMatcherDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;

public class OutputImageStreamDiscriminator extends ResourceIDMatcherDiscriminator<ImageStream, QuarkusApplication> {

  public OutputImageStreamDiscriminator() {
    super(p -> new ResourceID(p.getMetadata().getName(), p.getMetadata().getNamespace()));
  }
}
