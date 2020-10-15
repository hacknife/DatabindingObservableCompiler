package com.hacknife.databinding.compiler;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.Element;

import static com.github.javaparser.JavaParser.parseExpression;
import static com.hacknife.databinding.compiler.BindingTarget.Bindable;
import static com.hacknife.databinding.compiler.BindingTarget.METHOD_notifyPropertyChanged;
import static com.hacknife.databinding.compiler.BindingTarget.ObservableField;
import static com.hacknife.databinding.compiler.BindingTarget.new_ObservableField;

public class BindingHelper {

    public static File sourceFile(Element element) {
        return new File(invokeAttributeConvertString(invokeAttributeConvertObject(element, "sourcefile", true), "file", false));
    }

    public static String invokeAttributeConvertString(Object o, String attribute, boolean useField) {
        if (o == null) return null;
        try {
            Field field = useField ? o.getClass().getField(attribute) : o.getClass().getDeclaredField(attribute);
            field.setAccessible(true);
            Object obj = field.get(o);
            return obj == null ? null : obj.toString();
        } catch (Exception e) {
        }
        return null;
    }

    public static Object invokeAttributeConvertObject(Object o, String attribute, boolean useField) {
        if (o == null) return null;
        try {
            Field field = useField ? o.getClass().getField(attribute) : o.getClass().getDeclaredField(attribute);
            field.setAccessible(true);
            return field.get(o);
        } catch (Exception e) {
        }
        return null;
    }

    public static void printVar(Object o) {
        System.out.println("\n\n\n\n==============================================================");
        printVar(o, 0);
        System.out.println("==============================================================\n\n\n\n");
    }

    public static void printVar(Object o, int level) {
        if (o == null) return;
        if (o instanceof Short) return;
        if (o instanceof Integer) return;
        if (o instanceof Long) return;
        if (o instanceof Double) return;
        if (o instanceof Float) return;
        if (o instanceof String) return;
        if (o instanceof File) return;
        if (o instanceof Boolean) return;
        if (o instanceof Map) return;
        if (o instanceof Collection) return;
        if (o instanceof Class) return;
        if (o instanceof Byte) return;
        if (o instanceof byte[]) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("Entry")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("Locale")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("CompileState")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("TypeTag")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("Target")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("Source")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("Symtab")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("Names")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("NameImpl")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("Warner")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("Types")) return;
        if (o.getClass().getSimpleName().equalsIgnoreCase("ClassReader")) return;
        if (level > 0) return;
        try {
            Field[] fields = o.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                System.out.println(blank(level) + "declared field:" + field.getName() + " , class:" + o.getClass().getSimpleName() + "  , value:" + field.get(o));
                printVar(field.get(o), level + 1);
            }

            fields = o.getClass().getFields();
            for (Field field : fields) {
                field.setAccessible(true);
                System.out.println("         field:" + field.getName() + " , class:" + o.getClass().getSimpleName() + " , value:" + field.get(o));
                printVar(field.get(o), level + 1);
            }
        } catch (Exception e) {
        }

    }

    public static String blank(int len) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(">>");
        }
        return builder.toString();
    }

    public static void copyFiled(ClassOrInterfaceDeclaration from, ClassOrInterfaceDeclaration to,String sourceClass) {
        for (FieldDeclaration declaration : from.getFields()) {
            NodeList<VariableDeclarator> declarators = declaration.getVariables();
            for (VariableDeclarator declarator : declarators) {
                to.addFieldWithInitializer(new ClassOrInterfaceType(null, new SimpleName("ObservableField"), new NodeList<Type>(new TypeParameter(declaration.getCommonType().asString()))), declarator.getNameAsString(), parseExpression(new_ObservableField), declaration.getModifiers().toArray(new Modifier[0]))
                        .setFinal(true)
                        .setBlockComment("*\n" +
                                "     * class: {@link "+sourceClass+"}\n" +
                                "     * field: {@link "+sourceClass+"."+declarator.getNameAsString()+"}\n" +
                                "     ")
                ;
            }
        }
    }

    public static void copyImport(CompilationUnit from, CompilationUnit to) {
        for (ImportDeclaration p : from.getImports()) {
            to.addImport(p);
        }
    }

    public static void generateGettingAndSetting(ClassOrInterfaceDeclaration clazz) {
        for (FieldDeclaration field : clazz.getFields()) {
            for (VariableDeclarator variable : field.getVariables()) {
                ClassOrInterfaceType type = (ClassOrInterfaceType) field.getCommonType();
                clazz.addMethod(String.format("get%s", variable.getNameAsString().substring(0, 1).toUpperCase() + variable.getNameAsString().substring(1)))
                        .setType(field.getCommonType())
                        .setModifier(Modifier.PUBLIC, true)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(variable.getNameAsExpression())))
                ;

                clazz.addMethod(String.format("set%s", variable.getNameAsString().substring(0, 1).toUpperCase() + variable.getNameAsString().substring(1)))
                        .setType(clazz.getNameAsString())
                        .setModifier(Modifier.PUBLIC, true)
                        .setParameters(new NodeList<Parameter>(new Parameter(type.getTypeArguments().get().get(0), variable.getNameAsString())))
                        .setBody(new BlockStmt()
                                .addStatement(new MethodCallExpr(new FieldAccessExpr(new FieldAccessExpr(new ThisExpr(), variable.getNameAsString()), "set").toString(), parseExpression(variable.getNameAsString())))
                                .addStatement(new ReturnStmt().setExpression(new ThisExpr())))
                ;
            }
        }
    }

    public static void createMethod(ClassOrInterfaceDeclaration from, ClassOrInterfaceDeclaration to) {
        BlockStmt stmt = new BlockStmt();
        stmt.addStatement(new AssignExpr(new TypeExpr(new ClassOrInterfaceType(null, new SimpleName(String.format("%s %s", to.getNameAsString(), to.getNameAsString().toLowerCase())), null)), parseExpression(String.format("new %s()", to.getNameAsString())), AssignExpr.Operator.ASSIGN));

        for (FieldDeclaration field : to.getFields()) {
            for (VariableDeclarator variable : field.getVariables()) {
                stmt.addStatement(new MethodCallExpr(
                        new FieldAccessExpr(new FieldAccessExpr(new NameExpr(to.getNameAsString().toLowerCase()), variable.getNameAsString()), "set").toString(),
                        new MethodCallExpr(String.format("%s.get%s", from.getNameAsString().toLowerCase(), variable.getNameAsString().substring(0, 1).toUpperCase() + variable.getNameAsString().substring(1)))
                ));
            }
        }
        stmt.addStatement(new ReturnStmt().setExpression(parseExpression(to.getNameAsString().toLowerCase())));
        to.addMethod("create", Modifier.PUBLIC, Modifier.STATIC)
                .setParameters(new NodeList(new Parameter(new TypeParameter(from.getNameAsString()), from.getNameAsString().toLowerCase())))
                .setType(to.getNameAsString())
                .setBody(stmt);
    }

    public static void createValue(ClassOrInterfaceDeclaration from, ClassOrInterfaceDeclaration to) {
        BlockStmt stmt = new BlockStmt();
        stmt.addStatement(new AssignExpr(new TypeExpr(new ClassOrInterfaceType(null, new SimpleName(String.format("%s %s", from.getNameAsString(), from.getNameAsString().toLowerCase())), null)), parseExpression(String.format("new %s()", from.getNameAsString())), AssignExpr.Operator.ASSIGN));

        for (FieldDeclaration field : from.getFields()) {
            for (VariableDeclarator variable : field.getVariables()) {
                stmt.addStatement(new MethodCallExpr(
                        new FieldAccessExpr(new NameExpr(from.getNameAsString().toLowerCase()), String.format("set%s", variable.getNameAsString().substring(0, 1).toUpperCase() + variable.getNameAsString().substring(1))).toString(),
                        new MethodCallExpr(new MethodCallExpr(String.format("this.get%s", variable.getNameAsString().substring(0, 1).toUpperCase() + variable.getNameAsString().substring(1))), "get")
                ));
            }
        }
        stmt.addStatement(new ReturnStmt().setExpression(parseExpression(from.getNameAsString().toLowerCase())));
        to.addMethod("value", Modifier.PUBLIC)
                .setType(from.getNameAsString())
                .setBody(stmt);
    }
}
