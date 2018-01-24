/**
 *  Copyright 2016-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.antheminc.oss.nimbus.domain.config.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.Constraint;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import com.antheminc.oss.nimbus.domain.RepeatContainer;
import com.antheminc.oss.nimbus.domain.config.builder.attributes.AnnotationAttributeHandler;
import com.antheminc.oss.nimbus.domain.config.builder.attributes.ConstraintAnnotationAttributeHandler;
import com.antheminc.oss.nimbus.domain.config.builder.attributes.DefaultAnnotationAttributeHandler;
import com.antheminc.oss.nimbus.domain.defn.InvalidConfigException;
import com.antheminc.oss.nimbus.domain.model.config.AnnotationConfig;

/**
 * @author Soham Chakravarti
 *
 */
public class AnnotationConfigHandler {
	
	/**
	 * Default Handler for generating attribute values
	 */
	private static AnnotationAttributeHandler defaultAttributeHandler = new DefaultAnnotationAttributeHandler();
	
	// TODO Move to spring context
	private static Map<Class<?>, AnnotationAttributeHandler> attributeHandlers = new HashMap<>();
	static {
		attributeHandlers.put(Constraint.class, new ConstraintAnnotationAttributeHandler());
	}
	
	public static AnnotationConfig handleSingle(AnnotatedElement aElem, Class<? extends Annotation> metaAnnotationType) {
		final List<AnnotationConfig> aConfigs = handle(aElem, metaAnnotationType);
		if(CollectionUtils.isEmpty(aConfigs)) {
			return null;
		}
		
		if(aConfigs.size() != 1) {
			throw new InvalidConfigException(String.format("Found more than one element of config: %s. Expecting only one config element", aConfigs));
		}
		
		return aConfigs.get(0);
	}

	public static List<AnnotationConfig> handle(AnnotatedElement aElem, Class<? extends Annotation> metaAnnotationType) {
		final boolean hasIt = AnnotatedElementUtils.hasMetaAnnotationTypes(aElem, metaAnnotationType);
		if (!hasIt) {
			return null;
		}

		final List<AnnotationConfig> aConfigs = new ArrayList<>();
		
		final Annotation arr[] = aElem.getAnnotations();
		for(final Annotation a : arr) {
			final Set<String> metaTypes = AnnotatedElementUtils.getMetaAnnotationTypes(aElem, a.annotationType());
			
			if (metaTypes != null && metaTypes.contains(metaAnnotationType.getName())) {
				final AnnotationConfig ac = new AnnotationConfig();
				ac.setAnnotation(a);
				ac.setName(ClassUtils.getShortName(a.annotationType()));
				ac.setAttributes(getAttributesHandlerForType(metaAnnotationType).generateFrom(aElem, a));
				aConfigs.add(ac);
			}
		}
		
		// TODO null may be unreachable
		return CollectionUtils.isEmpty(aConfigs) ? null : aConfigs;
	}
	
	public static List<Annotation> handleRepeatable(AnnotatedElement aElem, Class<? extends Annotation> repeatableMetaAnnotationType) {
		final List<Annotation> annotations = new ArrayList<>();
		
		final Annotation arr[] = aElem.getAnnotations();

		for(final Annotation currDeclaredAnnotation : arr) {
			final Set<String> metaTypesOnCurrDeclaredAnnotation = AnnotatedElementUtils.getMetaAnnotationTypes(aElem, currDeclaredAnnotation.annotationType());
			
			// handle repeatable container
			if(metaTypesOnCurrDeclaredAnnotation!=null && metaTypesOnCurrDeclaredAnnotation.contains(RepeatContainer.class.getName())) {
				
				// get repeat container meta annotation and use declared repeatable annotaion
				RepeatContainer repeatContainerMetaAnnotation = AnnotationUtils.getAnnotation(currDeclaredAnnotation, RepeatContainer.class);
				Class<? extends Annotation> repeatableAnnotationDeclared = repeatContainerMetaAnnotation.value();
				
				// check that the declared annotation has passed in meta annotation type
				
				boolean hasRepeatable = AnnotatedElementUtils.hasAnnotation(repeatableAnnotationDeclared, repeatableMetaAnnotationType);
				if(hasRepeatable) {
					Object value = AnnotationUtils.getValue(currDeclaredAnnotation);
					if(value==null || !value.getClass().isArray())
						throw new InvalidConfigException("Repeatable container annotation is expected to follow convention: '{RepeableAnnotationType}[] value();' but not found in: "+currDeclaredAnnotation);
					
					Annotation[] annArr = (Annotation[])value;
					annotations.addAll(Arrays.asList(annArr));
				}
			}
			
			// handle non-repeating meta annotation
			if(metaTypesOnCurrDeclaredAnnotation!=null && metaTypesOnCurrDeclaredAnnotation.contains(repeatableMetaAnnotationType.getName())) {
				annotations.add(currDeclaredAnnotation);
			}
		}
		
		return annotations;
	}

	/**
	 * If an <tt>AnnotationAttributeHandler</tt> is registered for the provided type of
	 * <tt>metaAnnotationType</tt> it will be returned.
	 * 
	 * Otherwise this instance's <tt>defaultAttributeHandler</tt> will be returned.
	 * 
	 * @param metaAnnotationType
	 * @see com.antheminc.oss.nimbus.domain.config.builder.attributes.DefaultAnnotationAttributeHandler
	 * @return
	 */
	private static AnnotationAttributeHandler getAttributesHandlerForType(Class<? extends Annotation> metaAnnotationType) {
		return Optional.ofNullable(attributeHandlers.get(metaAnnotationType)).orElse(defaultAttributeHandler);
	}
}