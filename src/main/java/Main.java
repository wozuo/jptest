//  This file is part of jptest.
//
//  Created by Marc Tarnutzer on 08.01.2019.
//  Copyright © 2019 Marc Tarnutzer. All rights reserved.
//

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    static List<CompilationUnit> compilationUnits = null;

    public static void main(String args[]) {
        // Path to Android test application code
        String path = "/Users/marc/AndroidStudioProjects/JPTestProject";

        File androidProject = new File(path);
        if (!androidProject.exists() || !androidProject.isDirectory()) {
            System.out.println("Invalid Android project path");
        }

        SymbolSolverCollectionStrategy symbolSolverCollectionStrategy = new SymbolSolverCollectionStrategy();
        ProjectRoot projectRoot = symbolSolverCollectionStrategy.collect(androidProject.toPath());

        for (SourceRoot sourceRoot : projectRoot.getSourceRoots()) {
            System.out.println("Source root: " + sourceRoot);
            try {
                sourceRoot.tryToParse();
                compilationUnits = sourceRoot.getCompilationUnits();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            for (CompilationUnit compilationUnit : compilationUnits) {
                String p = compilationUnit.getStorage().map(CompilationUnit.Storage::getPath)
                        .map(Path::toString).orElse("");
                String name = compilationUnit.getStorage().map(CompilationUnit.Storage::getPath)
                        .map(Path::getFileName).map(Path::toString).orElse("");

                if (p == "" || name == "") {
                    continue;
                }

                System.out.println("Path: " + path + ", name: " + name);

                if (name.equals("PathCode.java")) {
                    analyzeMethodCallExpr(compilationUnit);
                    analyzeMethodCallExprFix(compilationUnit);
                } else if (name.equals("URLCode.java")) {
                    analyzeNameExpr(compilationUnit);
                }
            }
        }
    }

    public static void analyzeNameExpr(Node node) {
        if (node instanceof NameExpr) {
            System.out.println("Found NameExpr: " + node);

            ResolvedValueDeclaration resolvedValueDeclaration = null;
            try {
                resolvedValueDeclaration = ((NameExpr) node).resolve();
            } catch (Exception e) {
                System.out.println("Error resolving NameExpr: " + e);
            }

            if (resolvedValueDeclaration != null) {
                System.out.println("Resolved: " + resolvedValueDeclaration);
            }
        }

        for (Node child: node.getChildNodes()) {
            analyzeNameExpr(child);
        }
    }

    public static void analyzeMethodCallExpr(Node node) {
        if (node instanceof MethodCallExpr) {
            if (((MethodCallExpr) node).getName().asString().equals("getURL")) {
                ResolvedMethodDeclaration resolvedMethodDeclaration = null;
                try {
                    resolvedMethodDeclaration = ((MethodCallExpr) node).resolve();
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }

                if (resolvedMethodDeclaration != null) {
                    System.out.println("MethodCallExpr resolved: " + resolvedMethodDeclaration);

                    MethodDeclaration methodDeclaration = ((JavaParserMethodDeclaration) resolvedMethodDeclaration)
                            .getWrappedNode();

                    for (ReturnStmt returnStmt : methodDeclaration.findAll(ReturnStmt.class)) {
                        Expression expression = returnStmt.getExpression().get();

                        analyzeNameExpr(expression);
                    }
                }
            }
        }

        for (Node child: node.getChildNodes()) {
            analyzeMethodCallExpr(child);
        }
    }

    public static void analyzeMethodCallExprFix(Node node) {
        if (node instanceof MethodCallExpr) {
            if (((MethodCallExpr) node).getName().asString().equals("getURL")) {
                ResolvedMethodDeclaration resolvedMethodDeclaration = null;
                try {
                    resolvedMethodDeclaration = ((MethodCallExpr) node).resolve();
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }

                if (resolvedMethodDeclaration != null) {
                    System.out.println("MethodCallExpr resolved: " + resolvedMethodDeclaration);

                    MethodDeclaration methodDeclaration = ((JavaParserMethodDeclaration) resolvedMethodDeclaration)
                            .getWrappedNode();

                    for (CompilationUnit compilationUnit : compilationUnits) {
                        for (MethodDeclaration methodDeclaration1 : compilationUnit.findAll(MethodDeclaration.class)) {
                            if (methodDeclaration1.equals(methodDeclaration)) {
                                methodDeclaration = methodDeclaration1;
                            }
                        }
                    }

                    for (ReturnStmt returnStmt : methodDeclaration.findAll(ReturnStmt.class)) {
                        Expression expression = returnStmt.getExpression().get();

                        analyzeNameExpr(expression);
                    }
                }
            }
        }

        for (Node child: node.getChildNodes()) {
            analyzeMethodCallExprFix(child);
        }
    }

}
