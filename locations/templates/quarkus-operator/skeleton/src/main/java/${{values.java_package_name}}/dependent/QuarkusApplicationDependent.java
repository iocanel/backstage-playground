package ${{values.java_package_name}}.dependent;

import java.util.Map;

import ${{values.java_package_name}}.QuarkusApplication;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class QuarkusApplicationDependent<T extends HasMetadata> extends CRUDKubernetesDependentResource<T, QuarkusApplication>
    implements Matcher<T, QuarkusApplication> {

  static final String APP_LABEL = "app.kubernetes.io/name";

  public QuarkusApplicationDependent(Class<T> resourceType) {
    super(resourceType);
  }

  ObjectMeta createMetadata(QuarkusApplication resource, Map<String, String> labels) {
    final var metadata = resource.getMetadata();
    return new ObjectMetaBuilder()
        .withName(metadata.getName())
        .withNamespace(metadata.getNamespace())
        .withLabels(labels)
        .build();
  }

  public Map<String, String> getLabels(QuarkusApplication resource) {
    return Map.of(APP_LABEL, resource.getMetadata().getName());
  }
}
