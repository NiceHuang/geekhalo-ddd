package com.geekhalo.ddd.lite.codegen.application.repository;

import com.geekhalo.ddd.lite.codegen.support.MethodWriter;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.Data;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import static com.geekhalo.ddd.lite.codegen.application.repository.Utils.getReturnTypeName;
import static com.geekhalo.ddd.lite.codegen.utils.TypeUtils.bindDescription;
import static com.geekhalo.ddd.lite.codegen.utils.TypeUtils.createParameterSpecFromElement;

@Data
public final class RepositoryBasedApplicationMethodWriter implements MethodWriter {
    private final RepositoryBasedMethodMeta methodMeta;

    @Override
    public void writeTo(TypeSpec.Builder builder) {
        methodMeta.getQueryMethods().forEach(executableElement -> builder.addMethod(createQueryMethod(executableElement)));
    }

    private MethodSpec createQueryMethod(ExecutableElement executableElement) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(getReturnTypeName(executableElement.getReturnType(), methodMeta));
        bindDescription(executableElement, methodBuilder);
        executableElement.getParameters().forEach(variableElement -> {
            methodBuilder.addParameter(createParameterSpecFromElement(variableElement));

        });
        return methodBuilder.build();
    }


}