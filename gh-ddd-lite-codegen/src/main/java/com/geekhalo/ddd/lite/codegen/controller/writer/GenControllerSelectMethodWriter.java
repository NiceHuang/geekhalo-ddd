package com.geekhalo.ddd.lite.codegen.controller.writer;

import com.geekhalo.ddd.lite.codegen.Description;
import com.geekhalo.ddd.lite.codegen.TypeCollector;
import com.geekhalo.ddd.lite.codegen.controller.request.RequestBodyInfo;
import com.geekhalo.ddd.lite.codegen.controller.request.RequestBodyInfoUtils;
import com.squareup.javapoet.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.List;

import static com.geekhalo.ddd.lite.codegen.controller.request.RequestBodyInfoUtils.parseAndCreateForPage;
import static com.geekhalo.ddd.lite.codegen.controller.request.RequestBodyInfoUtils.parseAndCreateOfAllParams;
import static com.geekhalo.ddd.lite.codegen.controller.writer.PathUtils.getPathFromMethod;
import static com.geekhalo.ddd.lite.codegen.utils.MethodUtils.*;

public final class GenControllerSelectMethodWriter extends GenControllerMethodWriterSupport {


    public GenControllerSelectMethodWriter(String pkgName, RequestBodyInfoUtils.RequestBodyCreator creator, List<ExecutableElement> methods, TypeCollector typeCollector) {
        super(pkgName, creator, methods, typeCollector);
    }

    @Override
    protected void writeMethod(ExecutableElement executableElement, TypeSpec.Builder builder) {
        MethodSpec.Builder methodBuilder = null;
        if (isGetByIdMethod(executableElement)){
            methodBuilder = createGetByIdMethod(executableElement);
        }
        if (isListMethod(executableElement)){
            methodBuilder = createListMethod(executableElement);
        }
        if (isPageMethod(executableElement)){
            methodBuilder = createPageMethod(executableElement);
        }
        if (methodBuilder != null){
            builder.addMethod(methodBuilder.build());
        }
    }

    private MethodSpec.Builder createGetByIdMethod(ExecutableElement executableElement) {
        String methodName = executableElement.getSimpleName().toString();
        Description description = getDescription(executableElement);
        String descriptionStr = description != null ? description.value() : "";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ResponseBody.class)
                .addAnnotation(AnnotationSpec.builder(ApiOperation.class)
                        .addMember("value", "\""+ descriptionStr +"\"")
                        .addMember("nickname","\"" + methodName + "\"")
                        .build())
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("value", "\"/{id}\"")
                        .addMember("method", "$T.GET", ClassName.get(RequestMethod.class))
                        .build());
        builder.addParameter(ParameterSpec.builder(ClassName.LONG.box(), "id")
                .addAnnotation(PathVariable.class)
                .build());

        String returnType = executableElement.getReturnType().toString();
        if (isOptional(returnType)){
            builder.addStatement("return this.getApplication().getById(id).orElse(null)");
            String type = getTypeFromOptional(returnType);
            builder.returns(ClassName.bestGuess(type));
        }else {
            builder.addStatement("return this.getApplication().getById(id)");
            builder.returns(TypeName.get(executableElement.getReturnType()));
        }
        return builder;
    }

    private String getTypeFromOptional(String returnType) {
        return returnType.replace("java.util.Optional", "").replace("<", "").replace(">", "");
    }

    private boolean isOptional(String returnType) {
        return returnType.startsWith("java.util.Optional");
    }

    private MethodSpec.Builder createListMethod(ExecutableElement executableElement) {
        String methodName = executableElement.getSimpleName().toString();
        Description description = getDescription(executableElement);
        String descriptionStr = description != null ? description.value() : "";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ResponseBody.class)
                .addAnnotation(AnnotationSpec.builder(ApiOperation.class)
                        .addMember("value", "\""+ descriptionStr +"\"")
                        .addMember("nickname","\"" + methodName + "\"")
                        .build())
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("value", "\"/_"+ getPathFromMethod(methodName) + "\"")
                        .addMember("method", "$T.POST", ClassName.get(RequestMethod.class))
                        .build());

        builder.returns(TypeName.get(executableElement.getReturnType()));

        RequestBodyInfo requestBodyInfo = parseAndCreateOfAllParams(executableElement, getPkgName(), getCreator());
        if (requestBodyInfo != null){
            builder.addParameter(ParameterSpec.builder(requestBodyInfo.getParameterType(), requestBodyInfo.getParameterName())
                    .addAnnotation(RequestBody.class)
                    .build());
            builder.addStatement("return this.getApplication().$L($L)",
                    methodName,
                    createParamListStr(requestBodyInfo.getCallParams()));
        }else {
            builder.addStatement("return this.getApplication().$L()",
                    methodName);
        }

        return builder;
    }


    private MethodSpec.Builder createPageMethod(ExecutableElement executableElement) {
        String methodName = executableElement.getSimpleName().toString();
        Description description = getDescription(executableElement);
        String descriptionStr = description != null ? description.value() : "";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ResponseBody.class)
                .addAnnotation(AnnotationSpec.builder(ApiOperation.class)
                        .addMember("value", "\""+ descriptionStr +"\"")
                        .addMember("nickname","\"" + methodName + "\"")
                        .build())
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("value", "\"/_"+ getPathFromMethod(methodName) + "\"")
                        .addMember("method", "$T.POST", ClassName.get(RequestMethod.class))
                        .build());
        builder.returns(TypeName.get(executableElement.getReturnType()));

        RequestBodyInfo requestBodyInfo = parseAndCreateForPage(executableElement, getPkgName(), getCreator());
        if (requestBodyInfo != null){
            builder.addParameter(ParameterSpec.builder(requestBodyInfo.getParameterType(), requestBodyInfo.getParameterName())
                    .addAnnotation(RequestBody.class)
                    .build());
            builder.addStatement("return this.getApplication().$L($L)",
                    methodName,
                    createParamListStr(requestBodyInfo.getCallParams()));
        }else {
            builder.addStatement("return this.getApplication().$L()",
                    methodName);
        }
        return builder;
    }

}
