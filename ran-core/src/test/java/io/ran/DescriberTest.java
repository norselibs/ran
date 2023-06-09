/* Copyright 2021 PSQR
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.ran;

import io.ran.testclasses.Brand;
import io.ran.testclasses.Car;
import io.ran.testclasses.Door;
import io.ran.testclasses.Engine;
import io.ran.testclasses.GraphNode;
import io.ran.testclasses.GraphNodeLink;
import io.ran.token.Token;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class DescriberTest {
	private GenericFactory factory;
	private GuiceHelper helper;

	@BeforeClass
	public static void beforeClass() {

	}

	@Before
	public void setup() {
		helper = new GuiceHelper();
		factory = helper.factory;
	}

	@Test
	public void car() throws Throwable {
		TypeDescriber<Car> describer = TypeDescriberImpl.getTypeDescriber(Car.class);
		assertEquals(8, describer.fields().size());
		assertEquals(1, describer.primaryKeys().size());
		assertEquals("id", describer.primaryKeys().get(0).getToken().snake_case());
		assertEquals(String.class, describer.primaryKeys().get(0).getType().clazz);
		assertEquals(4, describer.relations().size());
		assertEquals("id", describer.relations().get(Door.class).get().getFromKeys().get(0).getToken().camelHump());
		assertEquals(String.class, describer.relations().get(Door.class).get().getFromKeys().get(0).getType().clazz);
		assertEquals(Car.class, describer.relations().get(Door.class).get().getFromKeys().get(0).getOn().clazz);

		assertEquals("carId", describer.relations().get(Door.class).get().getToKeys().get(0).getToken().camelHump());
		assertEquals(String.class, describer.relations().get(Door.class).get().getToKeys().get(0).getType().clazz);
		assertEquals(Door.class, describer.relations().get(Door.class).get().getToKeys().get(0).getOn().clazz);

		assertEquals(RelationType.OneToMany, describer.relations().get(Door.class).get().getType());


		assertEquals("engineId", describer.relations().get(Engine.class).get().getFromKeys().get(0).getToken().camelHump());
		assertEquals(UUID.class, describer.relations().get(Engine.class).get().getFromKeys().get(0).getType().clazz);
		assertEquals("id", describer.relations().get(Engine.class).get().getToKeys().get(0).getToken().camelHump());
		assertEquals(UUID.class, describer.relations().get(Engine.class).get().getToKeys().get(0).getType().clazz);
		assertEquals(RelationType.OneToOne, describer.relations().get(Engine.class).get().getType());

		assertEquals("id", describer.relations().get(HeadLights.class).get().getFromKeys().get(0).getToken().camelHump());
		assertEquals(String.class, describer.relations().get(HeadLights.class).get().getFromKeys().get(0).getType().clazz);
		assertEquals("on", describer.relations().get(HeadLights.class).get().getToKeys().get(0).getToken().camelHump());
		assertEquals(String.class, describer.relations().get(HeadLights.class).get().getToKeys().get(0).getType().clazz);
		assertEquals(RelationType.OneToOne, describer.relations().get(HeadLights.class).get().getType());

	}

	@Test
	public void door() throws Throwable {
		TypeDescriber<Door> describer = TypeDescriberImpl.getTypeDescriber(Door.class);

		assertEquals("carId", describer.relations().get(Car.class).get().getFromKeys().get(0).getToken().camelHump());
		assertEquals(String.class, describer.relations().get(Car.class).get().getFromKeys().get(0).getType().clazz);
		assertEquals("id", describer.relations().get(Car.class).get().getToKeys().get(0).getToken().camelHump());
		assertEquals(String.class, describer.relations().get(Car.class).get().getToKeys().get(0).getType().clazz);
	}


	@Test
	public void engine() throws Throwable {
		TypeDescriber<Engine> describer = TypeDescriberImpl.getTypeDescriber(Engine.class);

		assertEquals("id", describer.relations().get(Car.class).get().getFromKeys().get(0).getToken().camelHump());
		assertEquals(UUID.class, describer.relations().get(Car.class).get().getFromKeys().get(0).getType().clazz);
		assertEquals("engineId", describer.relations().get(Car.class).get().getToKeys().get(0).getToken().camelHump());
		assertEquals(UUID.class, describer.relations().get(Car.class).get().getToKeys().get(0).getType().clazz);
	}

	@Test
	public void getValue() throws Throwable {
		TypeDescriber<Engine> describer = TypeDescriberImpl.getTypeDescriber(Engine.class);

		Engine engine = factory.get(Engine.class);
		Mapping engineMapping = (Mapping) engine;
		engine.setId(UUID.randomUUID());

		Object actual = engineMapping._getValue(describer.fields().get(Token.of("id")));
		assertEquals(engine.getId(), actual);
	}

	@Test
	public void getKey() throws Throwable {
		Engine engine = factory.get(Engine.class);
		engine.setId(UUID.randomUUID());

		CompoundKey actual = ((Mapping) engine)._getKey();
		assertEquals(1, actual.getValues().size());
		assertEquals(engine.getId(), actual.getValue(Token.of("id")));
	}

	@Test
	public void setRelation() throws Throwable {
		TypeDescriber<Car> describer = TypeDescriberImpl.getTypeDescriber(Car.class);

		Engine engine = factory.get(Engine.class);
		Car car = factory.get(Car.class);
		Mapping carMapping = (Mapping) car;
		carMapping._setRelation(describer.relations().get(0), engine);

		assertSame(engine, car.getEngine());
	}

	@Test
	public void setCollectionRelation() throws Throwable {
		TypeDescriber<Engine> describer = TypeDescriberImpl.getTypeDescriber(Engine.class);

		Engine engine = factory.get(Engine.class);
		Mapping engineMapping = (Mapping) engine;
		Car car = new Car();
		engineMapping._setRelation(describer.relations().get(0), Arrays.asList(car));

		assertSame(car, engine.getCars().stream().findFirst().get());
	}

	@Test
	public void getRelation() throws Throwable {
		TypeDescriber<Car> describer = TypeDescriberImpl.getTypeDescriber(Car.class);
		Car car = factory.get(Car.class);
		Mapping carMapping = (Mapping) car;

		Object relation = carMapping._getRelation(describer.relations().get(0));
		assertNull(relation);

		Engine engine = new Engine();
		engine.setId(UUID.randomUUID());
		car.setEngine(engine);
		relation = carMapping._getRelation(describer.relations().get(0));
		assertSame(engine, relation);
	}

	@Test
	public void isChanged() throws Throwable {
		TypeDescriber<Car> describer = TypeDescriberImpl.getTypeDescriber(Car.class);
		Car car = factory.get(Car.class);
		Mapping carMapping = (Mapping) car;

		assertFalse(carMapping._isChanged());

		car.setBrand(Brand.Porsche);

		assertTrue(carMapping._isChanged());
	}

	@Test
	public void handleGraphs() throws Throwable {
		TypeDescriber<GraphNode> describer = TypeDescriberImpl.getTypeDescriber(GraphNode.class);
		assertEquals(Clazz.of(GraphNodeLink.class), describer.relations().get("next_nodes").getToKeys().get(0).getOn());
		assertEquals("from_id", describer.relations().get("next_nodes").getToKeys().get(0).getProperty().getSnakeCase());
		assertEquals("id", describer.relations().get("next_nodes").getFromKeys().get(0).getProperty().getSnakeCase());
		List<RelationDescriber> via = describer.relations().get("next_nodes").getVia();
		assertEquals("id", via.get(0).getFromKeys().get(0).getProperty().getSnakeCase());
		assertEquals(Clazz.of(GraphNode.class), via.get(0).getFromKeys().get(0).getOn());
		assertEquals("from_id", via.get(0).getToKeys().get(0).getProperty().getSnakeCase());
		assertEquals(Clazz.of(GraphNodeLink.class), via.get(0).getToKeys().get(0).getOn());
		assertEquals("to_id", via.get(1).getFromKeys().get(0).getProperty().getSnakeCase());
		assertEquals(Clazz.of(GraphNodeLink.class), via.get(1).getFromKeys().get(0).getOn());
		assertEquals("id", via.get(1).getToKeys().get(0).getProperty().getSnakeCase());
		assertEquals(Clazz.of(GraphNode.class), via.get(1).getToKeys().get(0).getOn());

		assertEquals(Clazz.of(GraphNodeLink.class), describer.relations().get("previous_nodes").getToKeys().get(0).getOn());
		assertEquals("to_id", describer.relations().get("previous_nodes").getToKeys().get(0).getProperty().getSnakeCase());
		assertEquals("id", describer.relations().get("previous_nodes").getFromKeys().get(0).getProperty().getSnakeCase());
		via = describer.relations().get("previous_nodes").getVia();
		assertEquals("id", via.get(0).getFromKeys().get(0).getProperty().getSnakeCase());
		assertEquals(Clazz.of(GraphNode.class), via.get(0).getFromKeys().get(0).getOn());
		assertEquals("to_id", via.get(0).getToKeys().get(0).getProperty().getSnakeCase());
		assertEquals(Clazz.of(GraphNodeLink.class), via.get(0).getToKeys().get(0).getOn());
		assertEquals("from_id", via.get(1).getFromKeys().get(0).getProperty().getSnakeCase());
		assertEquals(Clazz.of(GraphNodeLink.class), via.get(1).getFromKeys().get(0).getOn());
		assertEquals("id", via.get(1).getToKeys().get(0).getProperty().getSnakeCase());
		assertEquals(Clazz.of(GraphNode.class), via.get(1).getToKeys().get(0).getOn());
	}
}
