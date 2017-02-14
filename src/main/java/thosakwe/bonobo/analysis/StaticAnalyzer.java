package thosakwe.bonobo.analysis;

import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;
import thosakwe.bonobo.language.objects.BonoboObjectImpl;
import thosakwe.bonobo.language.types.*;

import java.util.HashMap;
import java.util.Map;

public class StaticAnalyzer extends BaseStaticAnalyzer {
    public BonoboLibrary analyzeCompilationUnit(BonoboParser.CompilationUnitContext ctx) throws BonoboException {
        BonoboLibrary library = new BonoboLibrary(ctx);

        for (BonoboParser.TopLevelDefContext topLevelDefContext : ctx.topLevelDef()) {
            if (topLevelDefContext instanceof BonoboParser.TopLevelFuncDefContext) {
                BonoboFunction function = analyzeTopLevelFunctionDefinition((BonoboParser.TopLevelFuncDefContext) topLevelDefContext);
                library.addExport(function.getName(), function, function.getSource());
            } else if (topLevelDefContext instanceof BonoboParser.ConstDefContext) {
                Map<String, BonoboObject> variables = analyzeConstantDefinition((BonoboParser.ConstDefContext) topLevelDefContext);

                for (String name : variables.keySet()) {
                    BonoboObject value = variables.get(name);
                    library.addExport(name, value, value.getSource());
                }
            }
        }

        return library;
    }


    public Map<String, BonoboObject> analyzeConstantDefinition(BonoboParser.ConstDefContext ctx) throws BonoboException {
        Map<String, BonoboObject> result = new HashMap<>();

        for (BonoboParser.VariableDeclarationContext declarationContext : ctx.variableDeclaration()) {
            String name = declarationContext.name.getText();
            BonoboObject value = analyzeExpression(declarationContext.expr());
            result.put(name, value);
        }

        return result;
    }

    public BonoboFunction analyzeTopLevelFunctionDefinition(BonoboParser.TopLevelFuncDefContext ctx) throws BonoboException {
        BonoboFunction function = new BonoboFunction(ctx);
        BonoboParser.FuncSignatureContext signature = ctx.funcSignature();

        // Get name
        function.setName(signature.name.getText());

        // Add params
        for (BonoboParser.ParamSpecContext paramSpec : signature.params) {
            function.getParameters().add(analyzeParameterSpecification(paramSpec));
        }

        // Get return type if specified
        if (signature.returnType != null) {
            function.setReturnType(analyzeType(signature.returnType));
        }

        // TODO: Check all possible paths to ensure value is of correct return type
        // TODO: If not explicitly specified, try to infer a return type

        return function;
    }

    public BonoboFunctionParameter analyzeParameterSpecification(BonoboParser.ParamSpecContext ctx) throws BonoboException {
        String name = null;
        BonoboType type = null;

        if (ctx instanceof BonoboParser.SimpleParamSpecContext) {
            // TODO: Infer type of simple; for now assume int
            BonoboParser.SimpleParamSpecContext context = (BonoboParser.SimpleParamSpecContext) ctx;
            name = context.ID().getText();
            type = BonoboUnknownType.INSTANCE;
        } else if (ctx instanceof BonoboParser.TypedParamSpecContext) {
            BonoboParser.TypedParamSpecContext context = (BonoboParser.TypedParamSpecContext) ctx;
            name = context.ID().getText();
            type = analyzeType(context.type());
        } else if (ctx instanceof BonoboParser.FunctionParamSpecContext) {
            // TODO: Build type for funcSignature
            BonoboParser.FunctionParamSpecContext context = (BonoboParser.FunctionParamSpecContext) ctx;
            name = context.funcSignature().name.getText();
            type = BonoboUnknownType.INSTANCE;
        }

        return new BonoboFunctionParameter(name, type, ctx);
    }

    public BonoboType analyzeType(BonoboParser.TypeContext ctx) throws BonoboException {
        if (ctx instanceof BonoboParser.IntTypeContext) {
            return BonoboIntegerType.INSTANCE;
        } else if (ctx instanceof BonoboParser.DoubleTypeContext) {
            return BonoboDoubleType.INSTANCE;
        } else if (ctx instanceof BonoboParser.NumberTypeContext) {
            return BonoboNumberType.INSTANCE;
        } else if (ctx instanceof BonoboParser.VariableTypeContext) {
            // TODO: Resolve variable types
        } else if (ctx instanceof BonoboParser.ListTypeContext) {
            return new BonoboListType(analyzeType(((BonoboParser.ListTypeContext) ctx).type()));
        }

        return null;
    }

    public BonoboObject analyzeExpression(BonoboParser.ExprContext ctx) throws BonoboException {
        if (ctx instanceof BonoboParser.IntegerLiteralExprContext) {
            return new BonoboObjectImpl(BonoboIntegerType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.DoubleLiteralExprContext) {
            return new BonoboObjectImpl(BonoboDoubleType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.ListLiteralExprContext) {
            BonoboType commonParentType = null;

            for (BonoboParser.ExprContext exprContext : ((BonoboParser.ListLiteralExprContext) ctx).expr()) {
                BonoboType type = analyzeExpression(exprContext).getType();

                if (commonParentType == null)
                    commonParentType = type;
                else {
                    BonoboType newCommon = commonParentType.getCommonParentType(type);

                    if (newCommon == null) {
                        throw new BonoboException(
                                "Cannot resolve common type for list literal. Ensure that all members share a base type.",
                                ctx);
                    } else if (commonParentType != newCommon) {
                        commonParentType = newCommon;
                    }
                }
            }

            return new BonoboObjectImpl(new BonoboListType(commonParentType), ctx);
        }

        // TODO: All expressions
        return new BonoboObjectImpl(BonoboUnknownType.INSTANCE, ctx);
    }
}
