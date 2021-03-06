package thosakwe.bonobo.language.types;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;

import java.util.Collection;

public class BonoboNumberType extends BonoboType {
    public static final BonoboNumberType INSTANCE = new BonoboNumberType();

    private BonoboNumberType() {}

    @Override
    public String getName() {
        return "number";
    }

    public BonoboType modulo(BonoboType left, BonoboType right, ParserRuleContext source) throws BonoboException {
        if (left.isAssignableTo(INSTANCE) && right.isAssignableTo(INSTANCE))
            return BonoboIntegerType.INSTANCE;
        throw BonoboException.wrongTypeForOperation(left, right, "%", source);
    }

    @Override
    public BonoboType typeForConstruct(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.cannotInstantiateAbstractType(getName(), source);
    }

    @Override
    public BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.notAFunction(this, source);
    }

    private BonoboType typeForArithmetic(String operator, BonoboType otherType, ParserRuleContext source) throws BonoboException {
        if (otherType.isAssignableTo(BonoboIntegerType.INSTANCE) || otherType.isAssignableTo(INSTANCE))
            return INSTANCE;
        throw BonoboException.wrongTypeForOperation(this, otherType, operator, source);
    }

    @Override
    public BonoboType typeForPow(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("^", otherType, source);
    }

    @Override
    public BonoboType typeForMultiply(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("*", otherType, source);
    }

    @Override
    public BonoboType typeForDivide(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("/", otherType, source);
    }

    @Override
    public BonoboType typeForAdd(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("+", otherType, source);
    }

    @Override
    public BonoboType typeForSubtract(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("-", otherType, source);
    }

    @Override
    public BonoboType typeForModulo(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return modulo(this, otherType, source);
    }
}
