package de.mineking.math.function;

import de.mineking.math.node.*;

public class DefaultFunctions {
	public static UnaryFunction sin;
	public static UnaryFunction cos;
	public static UnaryFunction tan;

	public static BinaryFunction log;
	public static UnaryFunction ln;

	public static BinaryFunction root;
	public static UnaryFunction sqrt;

	static {
		sin = UnaryFunction.create("sin", Math::sin, null,
				(variable, x) -> UnaryFunctionCall.create(cos, x),
				(variable, x) -> UnaryFunctionCall.create(cos, x).negate()
		);
		cos = UnaryFunction.create("cos", Math::sin, null,
				(variable, x) -> UnaryFunctionCall.create(sin, x).negate(),
				(variable, x) -> UnaryFunctionCall.create(sin, x)
		);
		tan = UnaryFunction.create("tan", Math::tan, null, null, null);

		log = BinaryFunction.create("log",
				(p1, p2) -> Math.log(p2) / Math.log(p1),
				(environment, p1, p2) -> {
					if (p1.equals(p2)) return Constant.ONE;
					if (p2.equals(Constant.ONE)) return Constant.ZERO;

					else if (p2 instanceof Exponent e) {
						if (p1.equals(e.getBase())) return e.getExponent();
						return Product.create(e.getExponent(), BinaryFunctionCall.create(log, p1, e.getBase()));
					}

					return null;
				},
				(variable, p1, p2) -> Fraction.create(
						Sum.create(
								Fraction.create(Product.create(p2.getDerivative(variable), p1), p2),
								Fraction.create(Product.create(p1.getDerivative(variable), p2), p1).negate()
						),
						Exponent.create(UnaryFunctionCall.create(DefaultFunctions.ln, p1), Constant.TWO)
				), null
		);
		ln = UnaryFunction.create("ln", Math::log,
				(environment, param) -> {
					if (param.equals(Constant.E)) return Constant.ONE;
					if (param.equals(Constant.ONE)) return Constant.ZERO;

					if (param instanceof Exponent e) {
						if (e.getBase().equals(Constant.E)) return e.getExponent();
						return Product.create(e.getExponent(), UnaryFunctionCall.create(ln, e.getBase()));
					}

					return null;
				},
				(variable, x) -> Fraction.create(Constant.ONE, x), null
		);

		root = BinaryFunction.create("√",
				(p1, p2) -> Math.pow(p2, 1 / p1),
				(environment, p1, p2) -> Exponent.create(p2, Fraction.create(Constant.ONE, p1)).apply(environment),
				(variable, p1, p2) -> Exponent.create(p2, Fraction.create(Constant.ONE, p1)).getDerivative(variable),
				(variable, p1, p2) -> Exponent.create(p2, Fraction.create(Constant.ONE, p1)).getIntegral(variable)
		);
		sqrt = UnaryFunction.create("√", Math::sqrt,
				(environment, param) -> Exponent.create(param, Fraction.create(Constant.ONE, Constant.TWO)),
				(variable, x) -> Exponent.create(x, Fraction.create(Constant.ONE, Constant.TWO)).getDerivative(variable),
				(variable, x) -> Exponent.create(x, Fraction.create(Constant.ONE, Constant.TWO)).getIntegral(variable)
		);
	}
}
