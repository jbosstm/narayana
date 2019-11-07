package io.narayana.lra.coordinator.api;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

// mark the war as a JAX-RS archive
@ApplicationPath("/")
@OpenAPIDefinition(
    info = @Info(title = "LRA Coordinator", version = JaxRsActivator.LRA_API_VERSION),
    tags = @Tag(name = "LRA Coordinator")
)
public class JaxRsActivator extends Application {
    static final String LRA_API_VERSION = "1.0-RC1";
}
