package com.hacknife.databinding.compiler;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class BindingTarget {
    public static final String FULL_ObservableField = "androidx.databinding.ObservableField";
    public static final String FULL_BINDABLE = "androidx.databinding.Bindable";
    public static final String Bindable = "Bindable";
    public static final String METHOD_notifyPropertyChanged = "notifyPropertyChanged(BR.%s)";
    public static final String ObservableField = "ObservableField<%s>";
    public static final String new_ObservableField = "new ObservableField<>()";
    private String fullClass;
    private String sourceFullClass;
    private String clazz;
    private String sourceClazz;
    private Element element;
    private String fullPackage;

    public BindingTarget(String fullClass, Element element) {
        this.fullClass = fullClass + "Binding";
        this.element = element;
        this.sourceFullClass = fullClass;
        this.sourceClazz = fullClass.substring(this.fullClass.lastIndexOf(".") + 1);
        this.clazz = this.fullClass.substring(this.fullClass.lastIndexOf(".") + 1);
        this.fullPackage = fullClass.substring(0, fullClass.lastIndexOf("."));
    }

    public Element getElement() {
        return element;
    }

    public String getFullClass() {
        return fullClass;
    }

    public String createClass() throws FileNotFoundException {
        CompilationUnit unit = new CompilationUnit();
        CompilationUnit dest = JavaParser.parse(BindingHelper.sourceFile(element)) ;
        unit.setPackageDeclaration(fullPackage);
        unit.addImport(FULL_ObservableField);
        unit.addImport(sourceFullClass);
        unit.addImport(FULL_BINDABLE);
        unit.setBlockComment("Created by http://github.com/hacknife/OnLite");
        ClassOrInterfaceDeclaration clazzOrInterface = unit.addClass(clazz);
        clazzOrInterface.setBlockComment("*\n" +
                " * author  : Hacknife\n" +
                " * e-mail  : hacknife@outlook.com\n" +
                " * github  : http://github.com/hacknife/DatabindingObservableCompiler\n" +
                " * project : DatabindingObservableCompiler\n" +
                " ");

        BindingHelper.copyImport(dest,unit);
        BindingHelper.copyFiled(dest.getClassByName(element.getSimpleName().toString()).get(),clazzOrInterface,sourceClazz);
        BindingHelper.generateGettingAndSetting(clazzOrInterface);
        BindingHelper.createMethod(dest.getClassByName(element.getSimpleName().toString()).get(),clazzOrInterface);
        BindingHelper.createValue(dest.getClassByName(element.getSimpleName().toString()).get(),clazzOrInterface);
        return unit.toString();
    }


    @Override
    public String toString() {
        return "{" +
                "\"fullClass\":\'" + fullClass + "\'" +
                ", \"clazz\":\'" + clazz + "\'" +
                ", \"element\":" + element +
                ", \"fullPackage\":\'" + fullPackage + "\'" +
                '}';
    }
}
