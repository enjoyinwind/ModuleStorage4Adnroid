package com.lxf.processors;

import com.google.auto.service.AutoService;
import com.lxf.annotations.ModuleDatabase;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
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
        FieldSpec versionMapFieldSpec = FieldSpec.builder(Map.class, "mVersionMap", Modifier.PRIVATE)
                .build();
        FieldSpec listenerMapFieldSpec = FieldSpec.builder(Map.class, "mListenerMap", Modifier.PRIVATE)
                .build();

        int newDatabaseVersion = 0;
        MethodSpec constructor = MethodSpec.methodBuilder("ModuleDatabaseHelper")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(String.class, "name")
                .addParameter(TypeName.INT, "version")
                .addCode("super(context, name, null, $L);", newDatabaseVersion)
                .addStatement("this.mVersionMap = new HashMap<$T, $T>", String.class, VersionInfo.class)
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder("ModuleDatabaseHelper")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ClassName.get("android.database.sqlite", "SQLiteOpenHelper"))
                .addField(versionMapFieldSpec)
                .addField(listenerMapFieldSpec)
                .addMethod(constructor)
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

    private Map<String, VersionInfo> readVersionMap(RoundEnvironment roundEnv){
        Map<String, VersionInfo> map = new HashMap<>();

        //处理前版本数据

        //处理注解
        for (Element element : roundEnv.getElementsAnnotatedWith(ModuleDatabase.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                ModuleDatabase annotation = typeElement.getAnnotation(ModuleDatabase.class);

                VersionInfo versionInfo = map.get(annotation.name());
                if(null == versionInfo){
                    versionInfo = new VersionInfo();
                }
                versionInfo.newVersion = annotation.version();
                map.put(annotation.name(), versionInfo);
            }
        }

        int oldMasterDbVersion = 0;
        if(map.get(MasterDbVersionKey) != null){
            oldMasterDbVersion = map.get(MasterDbVersionKey).oldVersion;
        }
        Map<String, VersionInfo> result = new HashMap<>(map.size());
        for(String key : map.keySet()){
            VersionInfo versionInfo = map.get(key);
            if(!key.equals(MasterDbVersionKey) && versionInfo != null && versionInfo.newVersion != versionInfo.oldVersion){
                result.put(key, versionInfo);
            }
        }

        if(result.size() > 0){
            VersionInfo masterDbVersionInfo = map.get(MasterDbVersionKey);
            if(masterDbVersionInfo != null){
                masterDbVersionInfo.newVersion = oldMasterDbVersion + 1;
            } else {
                masterDbVersionInfo = new VersionInfo();
                masterDbVersionInfo.oldVersion = oldMasterDbVersion;
                masterDbVersionInfo.newVersion = oldMasterDbVersion + 1;
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
}
