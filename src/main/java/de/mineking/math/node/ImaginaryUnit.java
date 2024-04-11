package de.mineking.math.node;

import de.mineking.math.MathEnvironment;
import org.jetbrains.annotations.NotNull;

public class ImaginaryUnit implements Node {
	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		return this;
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		return Constant.ZERO;
	}

	@NotNull
	@Override
	public Node getIntegral(@NotNull String variable) {
		return Product.create(this, Variable.create(variable));
	}

	@Override
	public double getDegree(@NotNull String variable) {
		return 0;
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public String toString() {
		return "i";
	}
}
