package com.lxf.processors;

import com.google.auto.service.AutoService;
import com.lxf.annotations.ModuleDatabase;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class ModuleDatabaseAnnotationProcessor extends AbstractProcessor {
    private static final String MasterDbName = "moduleDatabase";
    private static final String MasterDbVersionKey = "MasterDbVersion";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, InnerVersionInfo> map = readVersionMap(roundEnv);
        if(map.size() < 1){
            return true;
        }

        FieldSpec versionMapFieldSpec = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get("java.util", "Map"),
                ClassName.get("java.lang", "String"),
                ClassName.get("com.lxf.storage", "VersionInfo")),
                "mVersionMap", Modifier.PRIVATE)
                .build();

        int newDatabaseVersion = map.get(MasterDbVersionKey).newVersion;
        map.remove(MasterDbVersionKey);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(String.class, "name")
                .addParameter(TypeName.INT, "version")
                .addCode("super(context, $S, null, $L);\n", MasterDbName, newDatabaseVersion);

        constructorBuilder.addStatement("this.mVersionMap = new $T<$T, $T>($L)", HashMap.class, String.class, ClassName.get("com.lxf.storage", "VersionInfo"), map.size());
        for(String key : map.keySet()){
            InnerVersionInfo versionInfo = map.get(key);

            constructorBuilder.addCode("this.mVersionMap.put($S, new VersionInfo($L, $L, new $T()));\n", key, versionInfo.oldVersion, versionInfo.newVersion, versionInfo.listenerType);
        }

        MethodSpec onCreateMethodSpec = MethodSpec.methodBuilder("onCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db")
                .beginControlFlow("for(VersionInfo item : this.mVersionMap.values())")
                .addCode("item.listener.onCreate(db);\n")
                .endControlFlow()
                .build();

        MethodSpec onUpgradeMethodSpec = MethodSpec.methodBuilder("onUpgrade")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db")
                .addParameter(TypeName.INT, "oldVersion")
                .addParameter(TypeName.INT, "newVersion")
                .beginControlFlow("for(VersionInfo item : this.mVersionMap.values())")
                .addCode("item.listener.onUpgrade(db, item.oldVersion, item.newVersion);\n")
                .endControlFlow()
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder("ModuleDatabaseHelper")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ClassName.get("android.database.sqlite", "SQLiteOpenHelper"))
                .addField(versionMapFieldSpec)
                .addMethod(constructorBuilder.build())
                .addMethod(onCreateMethodSpec)
                .addMethod(onUpgradeMethodSpec)
                .build();

        JavaFile javaFile = JavaFile.builder("com.lxf.storage", typeSpec)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private Map<String, InnerVersionInfo> readVersionMap(RoundEnvironment roundEnv){
        Map<String, InnerVersionInfo> map = new HashMap<>();

        //处理前版本数据
        VersionUtils.read(map);

        //处理注解
        for (Element element : roundEnv.getElementsAnnotatedWith(ModuleDatabase.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                ModuleDatabase annotation = typeElement.getAnnotation(ModuleDatabase.class);

                InnerVersionInfo versionInfo = map.get(annotation.name());
                if(null == versionInfo){
                    versionInfo = new InnerVersionInfo(0, annotation.version());
                } else {
                    versionInfo.newVersion = annotation.version();
                }
                versionInfo.listenerType = typeElement;

                map.put(annotation.name(), versionInfo);
            }
        }

        int oldMasterDbVersion = 0;
        if(map.get(MasterDbVersionKey) != null){
            oldMasterDbVersion = map.get(MasterDbVersionKey).oldVersion;
        }
        Map<String, InnerVersionInfo> result = new HashMap<>(map.size());
        for(String key : map.keySet()){
            InnerVersionInfo versionInfo = map.get(key);
            if(!key.equals(MasterDbVersionKey) && versionInfo != null && versionInfo.newVersion != versionInfo.oldVersion){
                result.put(key, versionInfo);
            }
        }

        if(result.size() > 0){
            InnerVersionInfo masterDbVersionInfo = map.get(MasterDbVersionKey);
            if(masterDbVersionInfo != null){
                masterDbVersionInfo.newVersion = oldMasterDbVersion + 1;
            } else {
                masterDbVersionInfo = new InnerVersionInfo(oldMasterDbVersion, oldMasterDbVersion + 1);
            }

            result.put(MasterDbVersionKey, masterDbVersionInfo);
        }

        return result;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>(1);
        set.add(ModuleDatabase.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
