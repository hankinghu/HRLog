package com.example.hrprocessor;

import com.example.hrannotation.HRLog;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class HRProcessor extends AbstractProcessor {
    private static final String prefix = "_HR_LOG_PROCESSOR";
    /**
     * 用于类的写入
     */
    private Filer mFiler;
    /**
     * 用于打印messager
     */
    private Messager messager;
    private Trees trees;
    private HRLogTranslator visitor;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "init ProcessingEnvironment " + processingEnv.toString());
        trees = Trees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment)
                processingEnv).getContext();
        visitor = new HRLogTranslator(TreeMaker.instance(context), Names.instance(context).table, messager);
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.singleton(HRProcessor.class.getCanonicalName());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(HRLog.class.getName());
    }

    /**
     * 用于处理注解
     *
     * @param set              type Set
     * @param roundEnvironment 返回相关的类
     * @return false表示
     */

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //打印一下set.size,
        if (!roundEnvironment.processingOver()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "process set size " + set.size());
            //用这个方法拿到带有HRLog的地方
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(HRLog.class);
            //拿到element，这些element类型不同，可能是TypeElement,也可能是FieldElement
            for (Element element : elements) {
                messager.printMessage(Diagnostic.Kind.NOTE, "element simple name " + element.getSimpleName());
                //如果是类，在类上面加，遍历全部的方法，然后打印log，方法上加的先不管
                if (element instanceof TypeElement) {
                    //解析ast的类
                    handleElement(element);
                    //生成apt的类
//                generateLog(element);
                }

            }
        }
        return false;
    }

    /**
     * @param element 处理element
     */
    private void handleElement(Element element) {
        JCTree jcTree = (JCTree) trees.getTree(element);
        if (jcTree != null) {
            visitor.setTag(element.getKind() == ElementKind.CLASS ?
                    element.getSimpleName().toString() :
                    element.getEnclosingElement().getSimpleName().toString());
            jcTree.accept(visitor);
        }
    }

//    private void generateLog(Element element) {
//        //通过sb来添加log信息
//        String className = element.getSimpleName().toString();
//        String pageName = ((TypeElement) element).getQualifiedName().toString();
//        //生成一个类，然后生成相关的方法LogHelper类，然后对应每一个方法生成一个log
//        TypeSpec.Builder builder = TypeSpec.classBuilder(className + prefix).addModifiers(Modifier.PUBLIC);
//        //先把className加上
//        //返回类的方法，参数和成员类型,字段
//        ClassName LogClass = ClassName.get("android.util", "Log");
//        for (Element e : element.getEnclosedElements()) {
//            //打印了全部的方法名称
//            messager.printMessage(Diagnostic.Kind.NOTE, "e.getSimpleName() " + e.getSimpleName());
//            //拿到方法的element
//            if (e instanceof ExecutableElement) {
//                StringBuilder logSb = new StringBuilder();
//                ExecutableElement methodElement = (ExecutableElement) e;
//                //拿到方法名
//                String methodName = methodElement.getSimpleName().toString();
//                logSb.append("方法名：").append(methodName);
//                if (methodName.equals("<init>")) {
//                    continue;
//                }
//                //拿到方法里面的参数
//                for (VariableElement param : methodElement.getParameters()) {
//                    //循环获取方法里面的参数
////                            param.getEnclosingElement();
//                    logSb.append("  参数名为：").append(param.getSimpleName()).append("- 参数值为:").append(param.getConstantValue());
//                }
//                //生成一个方法
//                MethodSpec methodSpec = MethodSpec
//                        .methodBuilder(methodName)
//                        .addModifiers(Modifier.PUBLIC)
//                        .returns(TypeName.VOID)
//                        .addStatement("$T.d($S,$S)", LogClass, className, logSb)
//                        .build();
//                builder.addMethod(methodSpec);
//            }
//        }
//        JavaFile javaFile = JavaFile.builder(pageName.substring(0, pageName.lastIndexOf(".")), builder.build()).build();
//        try {
//            javaFile.writeTo(mFiler);
//        } catch (Exception e) {
//            messager.printMessage(Diagnostic.Kind.NOTE, e.getMessage());
//        }
//    }

}