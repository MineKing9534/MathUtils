package de.mineking.math.node;

import de.mineking.math.MathEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Product implements Node {
	private final List<Node> nodes;

	Product(@NotNull List<Node> nodes) {
		this.nodes = nodes;
	}

	@NotNull
	public List<Node> getNodes() {
		return nodes;
	}

	@NotNull
	public static Node create(@NotNull Collection<Node> nodes) {
		var temp = process(nodes, MathEnvironment.DEFAULT);

		if (temp.stream().anyMatch(n -> n instanceof Fraction)) return Fraction.create(
				Product.create(temp.stream().map(n -> n instanceof Fraction f ? f.getTop() : n).toList()),
				Product.create(temp.stream().filter(n -> n instanceof Fraction).map(n -> ((Fraction) n).getBottom()).toList())
		);


		if (temp.isEmpty()) return Constant.ONE;
		if (temp.size() == 1) return temp.stream().findFirst().get();

		if (temp.stream().anyMatch(n -> n == Constant.ZERO)) return Constant.ZERO;

		return new Product(temp);
	}

	@NotNull
	public static Node create(@NotNull Node... nodes) {
		return create(Arrays.asList(nodes));
	}

	@NotNull
	private static List<Node> process(@NotNull Collection<Node> nodes, @NotNull MathEnvironment environment) {
		var constant = new AtomicReference<>(1.0);
		var temp = nodes.stream()
				.flatMap(n -> n instanceof Product p ? p.nodes.stream() : Stream.of(n))
				.filter(n -> {
					if (n.hasValue(environment)) {
						constant.updateAndGet(d -> d * n.value());
						return false;
					}

					return true;
				})
				.map(n -> {
					if (n.isNegative()) {
						constant.updateAndGet(d -> d * -1);
						return n.negate();
					} else return n;
				})
				.filter(n -> n != Constant.ONE)
				.collect(Collectors.toList());

		if (constant.get() != 1) temp.add(Constant.create(constant.get()));

		return temp;
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		var temp = create(this.nodes.stream().map(n -> n.apply(environment)).toList());

		if (temp instanceof Product product) {
			var nodes = process(product.nodes, environment);

			var map = new HashMap<Node, Node>();

			for(var n : nodes) {
				var base = n instanceof Exponent e ? e.getBase() : n;
				var exponent = n instanceof Exponent e ? e.getExponent() : Constant.ONE;

				map.compute(base, (k, v) -> v == null ? exponent : Sum.create(v, exponent));
			}

			return create(map.entrySet().stream()
					.map(e -> Exponent.create(e.getKey(), e.getValue()))
					.toList()
			);
		}

		return temp.apply(environment);
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		return Sum.create(nodes.stream()
				.map(n -> {
					var temp = nodes.stream().filter(x -> x != n).collect(Collectors.toList());
					temp.add(n.getDerivative(variable));

					return Product.create(temp);
				})
				.toList()
		);
	}

	@Override
	public double getDegree(@NotNull String variable) {
		if (nodes.stream().anyMatch(n -> n.getDegree(variable) == Integer.MAX_VALUE)) return Integer.MAX_VALUE - 1;
		return nodes.stream().mapToDouble(node -> node.getDegree(variable)).sum();
	}

	@Override
	public boolean isConstant() {
		return nodes.stream().allMatch(Node::isConstant);
	}

	@Override
	public boolean isNegative() {
		return nodes.stream().filter(Node::isNegative).count() % 2 == 1;
	}

	@NotNull
	@Override
	public List<Node> getFactors(@NotNull MathEnvironment environment) {
		return nodes.stream()
				.flatMap(n -> n.getFactors(environment).stream())
				.toList();
	}

	@Nullable
	@Override
	public Node removeFactor(@NotNull Node node, @NotNull MathEnvironment environment) {
		var result = new ArrayList<>(nodes);

		for (int i = 0; i < result.size(); i++) {
			var temp = result.get(i).removeFactor(node, environment);
			if (temp != null) {
				result.set(i, temp);
				return create(result);
			}
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node n)) return false;
		var a = simplify();
		var b = n.simplify();

		if (a instanceof Product pa) {
			if (b instanceof Product pb) return new HashSet<>(pa.nodes).equals(new HashSet<>(pb.nodes));
			else return false;
		}
		return a.equals(b);
	}

	@Override
	public int hashCode() {
		return Objects.hash("product", nodes);
	}

	@Override
	public String toString() {
		var temp = removeFactor(Constant.NEGATIVE, MathEnvironment.DEFAULT);

		if (temp == null || !isNegative()) {
			return nodes.stream()
					.sorted(Comparator.comparing(n -> n.getDegree(Node.defaultVariable)))
					.map(n -> n.isNegative() ? n.stringWithParentheses(true) : n.stringWithParentheses(getPriority()))
					.collect(Collectors.joining(" "));
		} else return "- " + temp.stringWithParentheses(getPriority());
	}

	@Override
	public int getPriority() {
		return 100;
	}
}
