/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.swagger;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.util.NamedValue;
import org.restlet.util.Series;

/**
 * Resource that is able to automatically describe itself with Swagger. This
 * description can be customized by overriding the {@link #describe()} and
 * {@link #describeMethod(Method, MethodInfo)} methods.<br>
 * <br>
 * When used to describe a class of resources in the context of a parent
 * application, a special instance will be created using the default constructor
 * (with no request, response associated). In this case, the resource should do
 * its best to return the generic information when the Swagger description methods
 * are invoked, like {@link #describe()} and delegate methods.
 * 
 * @author Jerome Louvel
 */
public class SwaggerServerResource extends ServerResource {

    /**
     * Indicates if the resource should be automatically described via Swagger when
     * an OPTIONS request is handled.
     */
    private volatile boolean autoDescribing;

    /**
     * The description of this documented resource. Is seen as the text content
     * of the "doc" tag of the "resource" element in a Swagger document.
     */
    private volatile String description;

    /**
     * The name of this documented resource. Is seen as the title of the "doc"
     * tag of the "resource" element in a Swagger document.
     */
    private volatile String name;

    /**
     * Constructor.
     */
    public SwaggerServerResource() {
        this.autoDescribing = true;
    }

    /**
     * Indicates if the given method exposes its Swagger description. By default,
     * HEAD and OPTIONS are not exposed. This method is called by
     * {@link #describe(String, ResourceInfo)}.
     * 
     * @param method
     *            The method
     * @return True if the method exposes its description, false otherwise.
     */
    public boolean canDescribe(Method method) {
        return !(Method.HEAD.equals(method) || Method.OPTIONS.equals(method));
    }

    /**
     * Creates a new HTML representation for a given {@link ApplicationInfo}
     * instance describing an application.
     * 
     * @param applicationInfo
     *            The application description.
     * @return The created {@link SwaggerRepresentation}.
     */
    protected Representation createHtmlRepresentation(
            ApplicationInfo applicationInfo) {
        return new SwaggerRepresentation(applicationInfo).getHtmlRepresentation();
    }

    /**
     * Creates a new Swagger representation for a given {@link ApplicationInfo}
     * instance describing an application.
     * 
     * @param applicationInfo
     *            The application description.
     * @return The created {@link SwaggerRepresentation}.
     */
    protected Representation createSwaggerRepresentation(
            ApplicationInfo applicationInfo) {
        return new SwaggerRepresentation(applicationInfo);
    }

    /**
     * Describes the resource as a standalone Swagger document.
     * 
     * @return The Swagger description.
     */
    protected Representation describe() {
        return describe(getPreferredSwaggerVariant());
    }

    /**
     * Updates the description of the parent application. This is typically used
     * to add documentation on global representations used by several methods or
     * resources. Does nothing by default.
     * 
     * @param applicationInfo
     *            The parent application.
     */
    protected void describe(ApplicationInfo applicationInfo) {
    }

    /**
     * Describes a representation class and variant couple as Swagger information.
     * The variant contains the target media type that can be converted to by
     * one of the available Restlet converters.
     * 
     * @param methodInfo
     *            The parent method description.
     * @param representationClass
     *            The representation bean class.
     * @param variant
     *            The target variant.
     * @return The Swagger representation information.
     */
    protected RepresentationInfo describe(MethodInfo methodInfo,
            Class<?> representationClass, Variant variant) {
        return new RepresentationInfo(variant);
    }

    /**
     * Describes a representation class and variant couple as Swagger information
     * for the given method and request. The variant contains the target media
     * type that can be converted to by one of the available Restlet converters.<br>
     * <br>
     * By default, it calls {@link #describe(MethodInfo, Class, Variant)}.
     * 
     * @param methodInfo
     *            The parent method description.
     * @param requestInfo
     *            The parent request description.
     * @param representationClass
     *            The representation bean class.
     * @param variant
     *            The target variant.
     * @return The Swagger representation information.
     */
    protected RepresentationInfo describe(MethodInfo methodInfo,
            RequestInfo requestInfo, Class<?> representationClass,
            Variant variant) {
        return describe(methodInfo, representationClass, variant);
    }

    /**
     * Describes a representation class and variant couple as Swagger information
     * for the given method and response. The variant contains the target media
     * type that can be converted to by one of the available Restlet converters.<br>
     * <br>
     * By default, it calls {@link #describe(MethodInfo, Class, Variant)}.
     * 
     * @param methodInfo
     *            The parent method description.
     * @param responseInfo
     *            The parent response description.
     * @param representationClass
     *            The representation bean class.
     * @param variant
     *            The target variant.
     * @return The Swagger representation information.
     */
    protected RepresentationInfo describe(MethodInfo methodInfo,
            ResponseInfo responseInfo, Class<?> representationClass,
            Variant variant) {
        return describe(methodInfo, representationClass, variant);
    }

    /**
     * Returns a Swagger description of the current resource, leveraging the
     * {@link #getResourcePath()} method.
     * 
     * @param info
     *            Swagger description of the current resource to update.
     */
    public void describe(ResourceInfo info) {
        describe(getResourcePath(), info);
    }

    /**
     * Returns a Swagger description of the current resource.
     * 
     * @param path
     *            Path of the current resource.
     * @param info
     *            Swagger description of the current resource to update.
     */
    public void describe(String path, ResourceInfo info) {
        ResourceInfo.describe(null, info, this, path);
    }

    /**
     * Describes the resource as a Swagger document for the given variant.
     * 
     * @param variant
     *            The Swagger variant.
     * @return The Swagger description.
     */
    protected Representation describe(Variant variant) {
        Representation result = null;

        if (variant != null) {
            ResourceInfo resource = new ResourceInfo();
            describe(resource);
            ApplicationInfo application = resource.createApplication();
            describe(application);

            if (MediaType.APPLICATION_JSON.equals(variant.getMediaType())) {
                result = createSwaggerRepresentation(application);
            } else if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
                result = createHtmlRepresentation(application);
            }
        }

        return result;
    }

    /**
     * Describes the DELETE method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeDelete(MethodInfo info) {
        MethodInfo.describeAnnotations(info, this);
    }

    /**
     * Describes the GET method.<br>
     * By default, it describes the response with the available variants based
     * on the {@link #getVariants()} method. Thus in the majority of cases, the
     * method of the super class must be called when overridden.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeGet(MethodInfo info) {
        MethodInfo.describeAnnotations(info, this);
    }

    /**
     * Returns a Swagger description of the current method.
     * 
     * @return A Swagger description of the current method.
     */
    protected MethodInfo describeMethod() {
        MethodInfo result = new MethodInfo();
        describeMethod(getMethod(), result);
        return result;
    }

    /**
     * Returns a Swagger description of the given method.
     * 
     * @param method
     *            The method to describe.
     * @param info
     *            The method description to update.
     */
    protected void describeMethod(Method method, MethodInfo info) {
        info.setName(method);

        if (Method.GET.equals(method)) {
            describeGet(info);
        } else if (Method.POST.equals(method)) {
            describePost(info);
        } else if (Method.PUT.equals(method)) {
            describePut(info);
        } else if (Method.DELETE.equals(method)) {
            describeDelete(info);
        } else if (Method.OPTIONS.equals(method)) {
            describeOptions(info);
        } else if (Method.PATCH.equals(method)) {
            describePatch(info);
        }
    }

    /**
     * Describes the OPTIONS method.<br>
     * By default it describes the response with the available variants based on
     * the {@link #getSwaggerVariants()} method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeOptions(MethodInfo info) {
        // Describe each variant
        for (Variant variant : getSwaggerVariants()) {
            RepresentationInfo result = new RepresentationInfo(variant);
            info.getResponse().getRepresentations().add(result);
        }
    }

    /**
     * Returns the description of the parameters of this resource. Returns null
     * by default.
     * 
     * @return The description of the parameters.
     */
    protected List<ParameterInfo> describeParameters() {
        return null;
    }

    /**
     * Describes the Patch method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describePatch(MethodInfo info) {
        MethodInfo.describeAnnotations(info, this);
    }

    /**
     * Describes the POST method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describePost(MethodInfo info) {
        MethodInfo.describeAnnotations(info, this);
    }

    /**
     * Describes the PUT method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describePut(MethodInfo info) {
        MethodInfo.describeAnnotations(info, this);
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.autoDescribing = true;
    }

    /**
     * Returns the description of this documented resource. Is seen as the text
     * content of the "doc" tag of the "resource" element in a Swagger document.
     * 
     * @return The description of this documented resource.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the set of headers as a collection of {@link Parameter} objects.
     * 
     * @return The set of headers as a collection of {@link Parameter} objects.
     */
    @SuppressWarnings("unchecked")
    private Series<Header> getHeaders() {
        return (Series<Header>) getRequestAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
    }

    /**
     * Returns the name of this documented resource. Is seen as the title of the
     * "doc" tag of the "resource" element in a Swagger document.
     * 
     * @return The name of this documented resource.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the first parameter found in the current context (entity, query,
     * headers, etc) with the given name.
     * 
     * @param name
     *            The parameter name.
     * @return The first parameter found with the given name.
     */
    protected NamedValue<String> getParameter(String name) {
        NamedValue<String> result = null;
        Series<? extends NamedValue<String>> set = getParameters(name);

        if (set != null) {
            result = set.getFirst(name);
        }

        return result;
    }

    /**
     * Returns a collection of parameters objects contained in the current
     * context (entity, query, headers, etc) given a ParameterInfo instance.
     * 
     * @param parameterInfo
     *            The ParameterInfo instance.
     * @return A collection of parameters objects
     */
    private Series<? extends NamedValue<String>> getParameters(
            ParameterInfo parameterInfo) {
        Series<? extends NamedValue<String>> result = null;

        if (parameterInfo.getFixed() != null) {
            result = new Series<Parameter>(Parameter.class);
            result.add(parameterInfo.getName(), parameterInfo.getFixed());
        } else if (ParameterStyle.HEADER.equals(parameterInfo.getStyle())) {
            result = getHeaders().subList(parameterInfo.getName());
        } else if (ParameterStyle.TEMPLATE.equals(parameterInfo.getStyle())) {
            Object parameter = getRequest().getAttributes().get(
                    parameterInfo.getName());

            if (parameter != null) {
                result = new Series<Parameter>(Parameter.class);
                result.add(parameterInfo.getName(),
                        Reference.decode((String) parameter));
            }
        } else if (ParameterStyle.MATRIX.equals(parameterInfo.getStyle())) {
            result = getMatrix().subList(parameterInfo.getName());
        } else if (ParameterStyle.QUERY.equals(parameterInfo.getStyle())) {
            result = getQuery().subList(parameterInfo.getName());
        } else if (ParameterStyle.PLAIN.equals(parameterInfo.getStyle())) {
            // TODO not yet implemented.
        }

        if (result == null && parameterInfo.getDefaultValue() != null) {
            result = new Series<Parameter>(Parameter.class);
            result.add(parameterInfo.getName(), parameterInfo.getDefaultValue());
        }

        return result;
    }

    /**
     * Returns a collection of parameters found in the current context (entity,
     * query, headers, etc) given a parameter name. It returns null if the
     * parameter name is unknown.
     * 
     * @param name
     *            The name of the parameter.
     * @return A collection of parameters.
     */
    protected Series<? extends NamedValue<String>> getParameters(String name) {
        Series<? extends NamedValue<String>> result = null;

        if (describeParameters() != null) {
            for (ParameterInfo parameter : describeParameters()) {
                if (name.equals(parameter.getName())) {
                    result = getParameters(parameter);
                }
            }
        }

        return result;
    }

    /**
     * Returns the preferred Swagger variant according to the client preferences
     * specified in the request.
     * 
     * @return The preferred Swagger variant.
     */
    protected Variant getPreferredSwaggerVariant() {
        return getConnegService().getPreferredVariant(getSwaggerVariants(),
                getRequest(), getMetadataService());
    }

    /**
     * Returns the resource's relative path.
     * 
     * @return The resource's relative path.
     */
    protected String getResourcePath() {
        Reference ref = new Reference(getRequest().getRootRef(), getRequest()
                .getResourceRef());
        return ref.getRemainingPart();
    }

    /**
     * Returns the application resources base URI.
     * 
     * @return The application resources base URI.
     */
    protected Reference getResourcesBase() {
        return getRequest().getRootRef();
    }

    /**
     * Returns the available Swagger variants.
     * 
     * @return The available Swagger variants.
     */
    protected List<Variant> getSwaggerVariants() {
        List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.APPLICATION_JSON));
        result.add(new Variant(MediaType.TEXT_HTML));
        return result;
    }

    /**
     * Indicates if the resource should be automatically described via Swagger when
     * an OPTIONS request is handled.
     * 
     * @return True if the resource should be automatically described via Swagger.
     */
    public boolean isAutoDescribing() {
        return this.autoDescribing;
    }

    @Override
    public Representation options() {
        if (isAutoDescribing()) {
            return describe();
        }

        return null;
    }

    /**
     * Indicates if the resource should be automatically described via Swagger when
     * an OPTIONS request is handled.
     * 
     * @param autoDescribed
     *            True if the resource should be automatically described via
     *            Swagger.
     */
    public void setAutoDescribing(boolean autoDescribed) {
        this.autoDescribing = autoDescribed;
    }

    /**
     * Sets the description of this documented resource. Is seen as the text
     * content of the "doc" tag of the "resource" element in a Swagger document.
     * 
     * @param description
     *            The description of this documented resource.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the name of this documented resource. Is seen as the title of the
     * "doc" tag of the "resource" element in a Swagger document.
     * 
     * @param name
     *            The name of this documented resource.
     */
    public void setName(String name) {
        this.name = name;
    }

}
