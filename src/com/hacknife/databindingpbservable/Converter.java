package com.hacknife.databindingpbservable;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import static com.github.javaparser.JavaParser.parseExpression;


public class Converter extends AnAction {
    public static String FULL_CLASS_BINDABLE = "androidx.databinding.Bindable";
    public static String FULL_CLASS_OBSERVABLE = "androidx.databinding.BaseObservable";
    public static String Observable = "BaseObservable";
    public static String Bindable = "Bindable";

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Document document = editor.getDocument();
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        CompilationUnit javaUnit = JavaParser.parse(file.getText());
        String packages = javaUnit.getPackageDeclaration().get().getName().asString();


        CompilationUnit sourceUnit = new CompilationUnit();
        sourceUnit.addImport(FULL_CLASS_BINDABLE);
        sourceUnit.addImport(FULL_CLASS_OBSERVABLE);
        sourceUnit.setPackageDeclaration(packages);
        javaUnit.getImports().forEach(imports -> {
            if (!imports.getNameAsString().equals(FULL_CLASS_BINDABLE) && !imports.getNameAsString().equals(FULL_CLASS_OBSERVABLE))
                sourceUnit.addImport(imports);
        });
        javaUnit.getTypes().forEach(typeDeclaration -> {

            ClassOrInterfaceDeclaration javaClass = javaUnit.getClassByName(typeDeclaration.getNameAsString()).get();
            ClassOrInterfaceDeclaration sourceClass = sourceUnit.addClass(typeDeclaration.getNameAsString()).addExtendedType(Observable);
            javaClass.getFields().forEach(field -> field.getVariables().forEach(variable -> sourceClass.addField(variable.getType(), variable.getNameAsString(), Modifier.PRIVATE)));

            javaClass.getFields().forEach(field -> field.getVariables().forEach(variable -> {
                sourceClass.addMethod(String.format("get%s", variable.getNameAsString().substring(0, 1).toUpperCase() + variable.getNameAsString().substring(1)))
                        .setType(variable.getType())
                        .setModifier(Modifier.PUBLIC, true)
                        .addAnnotation(new MarkerAnnotationExpr(Bindable))
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(variable.getNameAsExpression())));


                sourceClass.addMethod(String.format("set%s", variable.getNameAsString().substring(0, 1).toUpperCase() + variable.getNameAsString().substring(1)))
                        .setType(sourceClass.getNameAsString())
                        .setModifier(Modifier.PUBLIC, true)
                        .addParameter(variable.getType(), variable.getNameAsString())
                        .setBody(new BlockStmt()
                                .addStatement(new AssignExpr(new FieldAccessExpr(new ThisExpr(), variable.getNameAsString()), variable.getNameAsExpression(), AssignExpr.Operator.ASSIGN))
                                .addStatement(parseExpression(String.format("notifyPropertyChanged(BR.%s)", variable.getNameAsString())))
                                .addStatement(new ReturnStmt().setExpression(new ThisExpr())));
            }));

        });

        new WriteCommandAction.Simple(project, file) {
            @Override
            protected void run() {
                document.deleteString(0, file.getTextLength());
                document.setText(sourceUnit.toString().replaceAll("\\r", ""));
            }
        }.execute();
    }
}
