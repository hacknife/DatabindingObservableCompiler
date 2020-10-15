package com.hacknife.databinding.compiler;

import com.google.auto.service.AutoService;
import com.hacknife.databinding.annotation.Binding;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;


@AutoService(Processor.class)
public class BindingCompiler extends AbstractProcessor {
    List<BindingTarget> targets = new ArrayList<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportType = new LinkedHashSet<>();
        supportType.add(Binding.class.getCanonicalName());
        return supportType;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        processBinding(roundEnv);
        process();
        return true;
    }

    private void processBinding(RoundEnvironment roundEnv) {
//        BindingHelper.printVar(BindingHelper.invokeAttributeConvertObject(roundEnv,"processingEnv",false));
        Set<? extends Element> bindings = roundEnv.getElementsAnnotatedWith(Binding.class);
        for (Element element : bindings) {
            String fullClass = element.asType().toString();
            targets.add(new BindingTarget(fullClass, element));
        }
    }

    private void process() {
        for (BindingTarget target : targets) {
            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                        target.getFullClass(),
                        target.getElement()
                );
                Writer writer = jfo.openWriter();
                writer.write(target.createClass());
                writer.flush();
                writer.close();
            } catch (Exception ignored) {
            }
        }
    }
}
