package ${{values.java_package_name}}.dependent;

import ${{values.java_package_name}}.QuarkusApplication;

import io.fabric8.openshift.api.model.ImageStream;
import io.javaoperatorsdk.operator.api.reconciler.ResourceIDMatcherDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;

public class BuilderImageStreamDiscriminator extends ResourceIDMatcherDiscriminator<ImageStream, QuarkusApplication> {

  public BuilderImageStreamDiscriminator() {
    super(p -> new ResourceID(BuilderImageStreamDependent.BUILDER_IMAGE_NAME, p.getMetadata().getNamespace()));
  }
}
