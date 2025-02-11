/*
 * Copyright (c) 2016-2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.config.inject;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.osgi.annotation.bundle.Requirement.Resolution.OPTIONAL;
import static org.osgi.service.cdi.CDIConstants.CDI_EXTENSION_PROPERTY;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.osgi.annotation.bundle.Requirement;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

/**
 * Binds the injection point with a configured value.
 * <p>
 * Can be used to annotate injection points of type {@code TYPE}, {@code Optional<TYPE>} or
 * {@code jakarta.inject.Provider<TYPE>}, where {@code TYPE} can be {@code String} and all types which have appropriate
 * converters.
 * <p>
 * Injected values are the same values that would be retrieved from an injected
 * {@link org.eclipse.microprofile.config.Config} instance or from the instance retrieved from
 * {@link org.eclipse.microprofile.config.ConfigProvider#getConfig()}
 *
 * <h2>Examples</h2>
 *
 * <h3>Injecting Native Values</h3>
 *
 * <p>
 * The first sample injects the configured value of the {@code my.long.property} property. The injected value does not
 * change even if the underline property value changes in the {@link org.eclipse.microprofile.config.Config}.
 * <p>
 * Injecting a native value is recommended for a mandatory property and its value does not change at runtime or used by
 * a bean with RequestScoped.
 * <p>
 * A further recommendation is to use the built in {@code META-INF/microprofile-config.properties} file mechanism to
 * provide default values inside an Application. If no configured value exists for this property, a
 * {@code DeploymentException} will be thrown during startup.
 *
 * <pre>
 * &#064;Inject
 * &#064;ConfigProperty(name = "my.long.property")
 * private Long injectedLongValue;
 * </pre>
 *
 * <h3>Injecting Optional Values</h3>
 *
 * <p>
 * Contrary to natively injecting, if the property is not specified, this will not lead to a DeploymentException. The
 * following code injects a Long value to the {@code my.optional.long.property}. If the property is not defined, the
 * value {@code 123} will be assigned to {@code injectedLongValue}.
 *
 * <pre>
 * &#064;Inject
 * &#064;ConfigProperty(name = "my.optional.long.property", defaultValue = "123")
 * private Long injectedLongValue;
 * </pre>
 *
 * <p>
 * The following code injects an Optional value of {@code my.optional.int.property}.
 *
 * <pre>
 * &#064;Inject
 * &#064;ConfigProperty(name = "my.optional.int.property")
 * private Optional&lt;Integer&gt; intConfigValue;
 * </pre>
 *
 * <h3>Injecting Dynamic Values</h3>
 *
 * <p>
 * The next sample injects a Provider for the value of {@code my.long.property} property to resolve the property
 * dynamically. Each invocation to {@code Provider#get()} will resolve the latest value from underlying
 * {@link org.eclipse.microprofile.config.Config} again. The existence of configured values will get checked during
 * startup. Instances of {@code Provider<T>} are guaranteed to be Serializable.
 *
 * <pre>
 * &#064;Inject
 * &#064;ConfigProperty(name = "my.long.property" defaultValue="123")
 * private Provider&lt;Long&gt; longConfigValue;
 * </pre>
 *
 * <p>
 * If {@code ConfigProperty} is used with a type where no {@link org.eclipse.microprofile.config.spi.Converter} exists,
 * a deployment error will be thrown.
 *
 * @author Ondrej Mihalyi
 * @author <a href="mailto:emijiang@uk.ibm.com">Emily Jiang</a>
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
@javax.inject.Qualifier
@Qualifier
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
/*
 * Two @Requirement annotations are defined so that the result is a _weak requirement_. One requirement is
 * resolution:=optional which means that at runtime, if satisfied, it will be wired, otherwise it is simply ignored.
 * Another requirement is effective:=active which means it is not visible at runtime, but applicable during assembly
 * where an effectviness of _active_ is specified.
 */
@Requirement(namespace = CDI_EXTENSION_PROPERTY, name = "org.eclipse.microprofile.config", effective = "active")
@Requirement(namespace = CDI_EXTENSION_PROPERTY, name = "org.eclipse.microprofile.config", resolution = OPTIONAL)
public @interface ConfigProperty {
    String UNCONFIGURED_VALUE = "org.eclipse.microprofile.config.configproperty.unconfigureddvalue";
    /**
     * The key of the config property used to look up the configuration value.
     * <p>
     * If it is not specified, it will be derived automatically as {@code <class_name>.<injection_point_name>}, where
     * {@code injection_point_name} is the field name or parameter name, {@code class_name} is the fully qualified name
     * of the class being injected to.
     * <p>
     * If one of the {@code class_name} or {@code injection_point_name} cannot be determined, the value has to be
     * provided.
     *
     * @return Name (key) of the config property to inject
     */
    @Nonbinding
    String name() default "";

    /**
     * The default value if the configured property does not exist. This value acts as a configure source with the
     * lowest ordinal.
     * <p>
     * If the target Type is not String, a proper {@link org.eclipse.microprofile.config.spi.Converter} will get
     * applied.
     *
     * Empty string as the default value will be ignored, which is same as not setting the default value. That means
     * that any default value string should follow the formatting rules of the registered Converters.
     *
     * If a property has been emptied by a config source with a higher ordinal by setting an empty configuration value
     * or by using a value causing the used converter returning {@code null}, the default value will not be used.
     *
     * @return the default value as a string
     */
    @Nonbinding
    String defaultValue() default UNCONFIGURED_VALUE;
}
