package ${{values.java_package_name}};

import static io.javaoperatorsdk.operator.api.reconciler.Constants.WATCH_CURRENT_NAMESPACE;
import static ${{values.java_package_name}}.Util.download;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

import ${{values.java_package_name}}.condition.BuilderImageStreamReadyCondition;
import ${{values.java_package_name}}.condition.OutputImageStreamReadyCondition;
import ${{values.java_package_name}}.dependent.BuildConfigDependent;
import ${{values.java_package_name}}.dependent.BuilderImageStreamDependent;
import ${{values.java_package_name}}.dependent.DeploymentDependent;
import ${{values.java_package_name}}.dependent.OutputImageStreamDependent;
import ${{values.java_package_name}}.dependent.ServiceDependent;
import org.jboss.logging.Logger;

import io.dekorate.utils.Packaging;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.client.OpenShiftClient;
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import jakarta.inject.Inject;

@SuppressWarnings("unused")
@ControllerConfiguration(namespaces = WATCH_CURRENT_NAMESPACE, dependents = {
    @Dependent(name = "builder", type = BuilderImageStreamDependent.class, useEventSourceWithName = "imageStreamEventSource", readyPostcondition = BuilderImageStreamReadyCondition.class),
    @Dependent(name = "output", type = OutputImageStreamDependent.class, useEventSourceWithName = "imageStreamEventSource", readyPostcondition = OutputImageStreamReadyCondition.class),
    @Dependent(name = "deployment", type = DeploymentDependent.class, dependsOn = { "output" }),
    @Dependent(name = "buildConfig", type = BuildConfigDependent.class),
    @Dependent(name = "service", type = ServiceDependent.class)
})
public class QuarkusApplicationReconciler
    implements Reconciler<QuarkusApplication>, EventSourceInitializer<QuarkusApplication> {

  private static final Logger LOG = Logger.getLogger(QuarkusApplicationReconciler.class);

  @Inject
  KubernetesClient client;

  @Override
  public UpdateControl<QuarkusApplication> reconcile(QuarkusApplication quarkusApplication, Context<QuarkusApplication> context)
      throws Exception {
    var name = quarkusApplication.getMetadata().getName();
    var spec = quarkusApplication.getSpec();
    var status = quarkusApplication.getStatus();

    if (status != null && QuarkusApplicationStatus.State.CREATED == status.getState()) {
      return UpdateControl.noUpdate();
    }

    try {
      LOG.info("Reconciling QuarkusApplication");
      var openShiftClient = client.adapt(OpenShiftClient.class);
      var repositoryUrl = Optional.ofNullable(spec.getRepositoryUrl());
      Path downloadedJar = download(repositoryUrl, spec.getCoordinates());
      String contextRoot = null;
      File tar;
      try {
        File original = Packaging.packageFile(downloadedJar.getParent(), contextRoot, downloadedJar);
        //Let's rename the archive and give it a more descriptive name, as it may appear in the logs.
        tar = Files.createTempFile("quarkus-", "-openshift").toFile();
        Files.move(original.toPath(), tar.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (Exception e) {
        throw new RuntimeException("Error creating the openshift binary build archive.", e);
      }
      Build build = openShiftClient.buildConfigs().withName(name)
          .instantiateBinary()
          .withTimeoutInMillis(300000)
          .fromFile(tar);

      status = new QuarkusApplicationStatus();
      status.setState(QuarkusApplicationStatus.State.CREATED);
      status.setError(false);
      status.setMessage("Created");
    } catch (Exception e) {
      LOG.error(e.getMessage());
      status = new QuarkusApplicationStatus();
      status.setMessage("Error querying API: " + e.getMessage());
      status.setState(QuarkusApplicationStatus.State.ERROR);
      status.setError(true);
    }

    quarkusApplication.setStatus(status);
    return UpdateControl.updateStatus(quarkusApplication);
  }

  public Map<String, EventSource> prepareEventSources(EventSourceContext<QuarkusApplication> context) {
    return Map.of("imageStreamEventSource", new InformerEventSource<ImageStream, QuarkusApplication>(
        InformerConfiguration.from(ImageStream.class, context).build(), context));
  }
}
