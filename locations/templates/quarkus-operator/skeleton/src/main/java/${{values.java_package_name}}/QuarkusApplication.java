package ${{values.java_package_name}};

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("${{values.apiGroup}}")
@Version("${{values.apiVersion}}")
@ShortNames("qapps")
public class QuarkusApplication extends CustomResource<QuarkusApplicationSpec, QuarkusApplicationStatus> implements Namespaced {

  public static final String GROUP = "${{values.apiGroup}}";
  public static final String VERSION = "${{values.apiVersion}}";
  public static final String KIND = "QuarkusApplication";
  public static final String PLURAL = "quarkusapplications";
  public static final String NAME = PLURAL + "." + GROUP;
}
