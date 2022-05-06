/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2022, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.lra.coordinator.api;

import io.narayana.lra.LRAConstants;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(title = "LRA Coordinator", version = LRAConstants.CURRENT_API_VERSION_STRING,
        contact = @Contact(name = "Narayana", url = "https://narayana.io")),
    tags = @Tag(name = "LRA Coordinator"),
    components = @Components(
        schemas = {
            @Schema(name = "LRAApiVersionSchema",
                description = "Format is `major.minor`, both components are required, they are to be numbers",
                type = SchemaType.STRING, pattern = "^\\d+\\.\\d+$", example = "1.0")
        },
        parameters = {
            @Parameter(name = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME, in = ParameterIn.HEADER,
                description = "Narayana LRA API version", schema = @Schema(ref = "LRAApiVersionSchema"))
        },
        headers = {
            @Header(name = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME, description = "Narayana LRA API version",
                schema = @Schema(ref = "LRAApiVersionSchema"))
        }
    )
)
@ApplicationPath("/")
public class JaxRsApplication extends Application {
}
