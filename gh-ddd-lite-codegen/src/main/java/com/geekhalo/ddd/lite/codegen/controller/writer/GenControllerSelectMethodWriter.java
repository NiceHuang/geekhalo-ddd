package com.geekhalo.ddd.lite.codegen.controller.writer;

import com.geekhalo.ddd.lite.codegen.Description;
import com.geekhalo.ddd.lite.codegen.TypeCollector;
import com.geekhalo.ddd.lite.codegen.controller.GenControllerAnnotationParser;
import com.geekhalo.ddd.lite.codegen.controller.GenControllerMethodMeta;
import com.geekhalo.ddd.lite.codegen.controller.request.RequestBodyInfo;
import com.geekhalo.ddd.lite.codegen.controller.request.RequestBodyInfoUtils;
import com.squareup.javapoet.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.math.BigInteger;
import java.util.List;

import static com.geekhalo.ddd.lite.codegen.controller.request.RequestBodyInfoUtils.parseAndCreateForPage;
import static com.geekhalo.ddd.lite.codegen.controller.request.RequestBodyInfoUtils.parseAndCreateOfAllParams;
import static com.geekhalo.ddd.lite.codegen.controller.writer.PathUtils.getPathFromMethod;
import static com.geekhalo.ddd.lite.codegen.utils.MethodUtils.*;

public final class GenControllerSelectMethodWriter extends GenControllerMethodWriterSupport {

    public GenControllerSelectMethodWriter(GenControllerAnnotationParser parser,
                                           RequestBodyInfoUtils.RequestBodyCreator creator,
                                           List<GenControllerMethodMeta.MethodMeta> methods, TypeCollector typeCollector) {
        super(parser, creator, methods, typeCollector);
    }

    @Override
    protected void writeMethod(GenControllerMethodMeta.MethodMeta executableElement, TypeSpec.Builder builder) {
        MethodSpec.Builder methodBuilder = null;
        if (isGetByIdMethod(executableElement.getExecutableElement())){
            methodBuilder = createGetByIdMethod(executableElement);
        }
        if (isListMethod(executableElement.getExecutableElement())){
            methodBuilder = createListMethod(executableElement);
        }
        if (isPageMethod(executableElement.getExecutableElement())){
            methodBuilder = createPageMethod(executableElement);
        }
        if (methodBuilder != null){
            builder.addMethod(methodBuilder.build());
        }
    }

    private MethodSpec.Builder createGetByIdMethod(GenControllerMethodMeta.MethodMeta executableElement) {
        String methodName = executableElement.getMethodName();
        Description description = executableElement.getDescription();
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
        VariableElement idParams = getIdParam(executableElement.getExecutableElement());
        if (isLong(idParams)) {
            builder.addParameter(ParameterSpec.builder(ClassName.LONG.box(), "id")
                    .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                            .addMember("value", "\"id\"")
                            .build())
                    .build());
        }else if(isBigInter(idParams)){
            builder.addParameter(ParameterSpec.builder(ClassName.get(BigInteger.class), "id")
                    .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                            .addMember("value", "\"id\"")
                            .build())
                    .build());
        }else {
            builder.addParameter(ParameterSpec.builder(ClassName.get(String.class), "_id")
                    .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                            .addMember("value", "\"id\"")
                            .build())
                    .build());
            builder.addStatement("$T id = $T.apply(_id)", idParams, idParams);
        }

        String returnType = executableElement.getExecutableElement().getReturnType().toString();
        if (getParser().isWrapper()){
            if (isOptional(returnType)){
                builder.addStatement("return $T.success(this.getApplication().getById(id).orElse(null))",
                        ClassName.bestGuess(getParser().getWrapperCls()));
                String type = getTypeFromOptional(returnType);
                builder.returns(ParameterizedTypeName.get(ClassName.bestGuess(getParser().getWrapperCls()), ClassName.bestGuess(type)) );
            }else {
                builder.addStatement("return $T.success(this.getApplication().getById(id))",
                        ClassName.bestGuess(getParser().getWrapperCls()));
                builder.returns(ParameterizedTypeName.get(ClassName.bestGuess(getParser().getWrapperCls()),
                        TypeName.get(executableElement.getExecutableElement().getReturnType())));
            }
        }else {
            if (isOptional(returnType)){
                builder.addStatement("return this.getApplication().getById(id).orElse(null)");
                String type = getTypeFromOptional(returnType);
                builder.returns(ClassName.bestGuess(type));
            }else {
                builder.addStatement("return this.getApplication().getById(id)");
                builder.returns(TypeName.get(executableElement.getExecutableElement().getReturnType()));
            }
        }

        return builder;
    }


    private String getTypeFromOptional(String returnType) {
        return returnType.replace("java.util.Optional", "").replace("<", "").replace(">", "");
    }

    private String getTypeFromPage(String returnType) {
        return returnType.replace("org.springframework.data.domain.Page", "").replace("<", "").replace(">", "");
    }

    private boolean isOptional(String returnType) {
        return returnType.startsWith("java.util.Optional");
    }

    private MethodSpec.Builder createListMethod(GenControllerMethodMeta.MethodMeta executableElement) {
        String methodName = executableElement.getMethodName();
        Description description = executableElement.getDescription();
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

        if (getParser().isWrapper()){
            builder.returns(ParameterizedTypeName.get(ClassName.bestGuess(getParser().getWrapperCls()),TypeName.get(executableElement.getReturnType())));

            RequestBodyInfo requestBodyInfo = parseAndCreateOfAllParams(executableElement.getExecutableElement(), getPkgName(), getCreator());
            if (requestBodyInfo != null) {
                builder.addParameter(ParameterSpec.builder(requestBodyInfo.getParameterType(), requestBodyInfo.getParameterName())
                        .addAnnotation(RequestBody.class)
                        .build());
                builder.addStatement("return $T.success(this.getApplication().$L($L))",
                        ClassName.bestGuess(getParser().getWrapperCls()),
                        methodName,
                        createParamListStr(requestBodyInfo.getCallParams()));
            } else {
                builder.addStatement("return $T.success(this.getApplication().$L())",
                        ClassName.bestGuess(getParser().getWrapperCls()),
                        methodName);
            }
        }else {
            builder.returns(TypeName.get(executableElement.getReturnType()));

            RequestBodyInfo requestBodyInfo = parseAndCreateOfAllParams(executableElement.getExecutableElement(), getPkgName(), getCreator());
            if (requestBodyInfo != null) {
                builder.addParameter(ParameterSpec.builder(requestBodyInfo.getParameterType(), requestBodyInfo.getParameterName())
                        .addAnnotation(RequestBody.class)
                        .build());
                builder.addStatement("return this.getApplication().$L($L)",
                        methodName,
                        createParamListStr(requestBodyInfo.getCallParams()));
            } else {
                builder.addStatement("return this.getApplication().$L()",
                        methodName);
            }
        }

        return builder;
    }


    private MethodSpec.Builder createPageMethod(GenControllerMethodMeta.MethodMeta executableElement) {
        String methodName = executableElement.getMethodName();
        Description description = executableElement.getDescription();
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

        ClassName type = ClassName.bestGuess(this.getTypeFromPage(executableElement.getReturnType().toString()));
        ClassName pageVoClass = ClassName.bestGuess("com.geekhalo.ddd.lite.spring.mvc.PageVo");
        TypeName pageVoType = ParameterizedTypeName.get(pageVoClass, type);

        if (getParser().isWrapper()){
            builder.returns(ParameterizedTypeName.get(ClassName.bestGuess(getParser().getWrapperCls()), pageVoType));

            RequestBodyInfo requestBodyInfo = parseAndCreateForPage(executableElement.getExecutableElement(), getPkgName(), getCreator());
            if (requestBodyInfo != null) {
                builder.addParameter(ParameterSpec.builder(requestBodyInfo.getParameterType(), requestBodyInfo.getParameterName())
                        .addAnnotation(RequestBody.class)
                        .build());
                builder.addStatement("return $T.success($T.apply(this.getApplication().$L($L)))",
                        ClassName.bestGuess(getParser().getWrapperCls()),
                        pageVoClass,
                        methodName,
                        createParamListStr(requestBodyInfo.getCallParams()));
            } else {
                builder.addStatement("return $T.success($T.apply(this.getApplication().$L()))",
                        ClassName.bestGuess(getParser().getWrapperCls()),
                        pageVoClass,
                        methodName);
            }
        }else {
            builder.returns(pageVoType);

            RequestBodyInfo requestBodyInfo = parseAndCreateForPage(executableElement.getExecutableElement(), getPkgName(), getCreator());
            if (requestBodyInfo != null) {
                builder.addParameter(ParameterSpec.builder(requestBodyInfo.getParameterType(), requestBodyInfo.getParameterName())
                        .addAnnotation(RequestBody.class)
                        .build());
                builder.addStatement("return $T.apply(this.getApplication().$L($L))",
                        pageVoType,
                        methodName,
                        createParamListStr(requestBodyInfo.getCallParams()));
            } else {
                builder.addStatement("return $T.apply(this.getApplication().$L())",
                        pageVoType,
                        methodName);
            }
        }
        return builder;
    }

}
