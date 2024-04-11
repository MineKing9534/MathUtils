import de.mineking.math.MathEnvironment;
import de.mineking.math.function.DefaultFunctions;
import de.mineking.math.node.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTest {
	static {
		MathEnvironment.DEFAULT.factorOut();
	}

	@Test
	public void sumCombine() {
		assertEquals(
				Product.create(
						Constant.create(4),
						Variable.defaultVariable
				),
				Sum.create(
						Variable.defaultVariable,
						Variable.defaultVariable,
						Product.create(
								Constant.create(2),
								Variable.defaultVariable
						)
				)
		);

		assertEquals(
				Product.create(
						Constant.TWO,
						Sum.create(
								Constant.ONE,
								Variable.defaultVariable
						)
				),
				Sum.create(
						Variable.defaultVariable,
						Product.create(
								Constant.NEGATIVE,
								Variable.defaultVariable
						).negate(),
						Constant.TWO
				)
		);
	}

	@Test
	public void productCombine() {
		assertEquals(
				Exponent.create(Variable.defaultVariable, Constant.TWO),
				Product.create(Variable.defaultVariable, Variable.defaultVariable)
		);

		assertEquals(
				Product.create(
						Constant.TWO,
						Exponent.create(Variable.defaultVariable, Constant.create(3)),
						UnaryFunctionCall.create(DefaultFunctions.sin, Variable.defaultVariable)
				),
				Product.create(
						Exponent.create(Variable.defaultVariable, Constant.TWO),
						Product.create(
								Constant.TWO,
								Variable.defaultVariable,
								UnaryFunctionCall.create(DefaultFunctions.sin, Variable.defaultVariable)
						)
				)
		);
	}

	@Test
	public void fractionTest() {
		assertEquals(
				Constant.ONE,
				Fraction.create(
						Variable.defaultVariable,
						Variable.defaultVariable
				)
		);
	}
}
