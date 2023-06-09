/* Copyright 2021 PSQR
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.ran;

import io.ran.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RelationDescriber {
	private Clazz collectionType;
	private Token field;
	private Clazz<?> fromClass;
	private Clazz<?> toClass;
	private KeySet fromKeys;
	private KeySet toKeys;
	private RelationType type;
	private RelationDescriberList via;
	private Relation relationAnnotation;

	private RelationDescriber() {
	}

	public static RelationDescriberList list(Clazz fromClass) {
		return new RelationDescriberList(fromClass);
	}

	public static RelationDescriber describer(Clazz fromClass, Relation relationAnnotation, Token field, Clazz<?> toClass, KeySet fromKeys, KeySet toKeys, RelationType type, Clazz collectionType) {

		RelationDescriber desc = new RelationDescriber();
		desc.relationAnnotation = relationAnnotation;
		desc.collectionType = collectionType;
		desc.fromClass = fromClass;
		desc.field = field;
		desc.toClass = toClass;
		desc.fromKeys = fromKeys;
		desc.type = type;
		desc.toKeys = toKeys;
		desc.via = new RelationDescriberList(desc.fromClass);
		return desc;
	}

	public Clazz<?> getFromClass() {
		return fromClass;
	}

	public Clazz<?> getToClass() {
		return toClass;
	}

	public KeySet getFromKeys() {
		return fromKeys;
	}

	public RelationType getType() {
		return type;
	}

	public Token getField() {
		return field;
	}

	public KeySet getToKeys() {
		return toKeys;
	}

	public Clazz getCollectionType() {
		return collectionType;
	}

	public boolean isCollectionRelation() {
		return collectionType != null;
	}

	public List<RelationDescriber> getVia() {
		return via;
	}

	public RelationDescriber inverse() {
		RelationDescriber inverse = RelationDescriber.describer(toClass, null, null, fromClass, toKeys, fromKeys, null, null);
		if (!getVia().isEmpty()) {
			inverse.getVia().add(getVia().get(1).inverse());
			inverse.getVia().add(getVia().get(0).inverse());
		}
		return inverse;
	}

	public Relation getRelationAnnotation() {
		return relationAnnotation;
	}

	public boolean requiredFieldsEquals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RelationDescriber that = (RelationDescriber) o;
		return Objects.equals(collectionType, that.collectionType) && Objects.equals(fromClass, that.fromClass) && Objects.equals(toClass, that.toClass) && Objects.equals(fromKeys, that.fromKeys) && Objects.equals(toKeys, that.toKeys) && Objects.equals(via, that.via);
	}

	public static class RelationDescriberList extends ArrayList<RelationDescriber> {
		private Clazz<?> fromClass;

		public RelationDescriberList(Clazz<?> fromClass) {
			this.fromClass = fromClass;
		}

		public RelationDescriber get(String snakeCaseField) {
			return stream().filter(rd -> rd.getField().snake_case().equals(snakeCaseField)).findFirst().orElseThrow(() -> new RuntimeException("Could not find field " + snakeCaseField));
		}

		public Optional<RelationDescriber> get(Class<?> toClass) {
			return stream().filter(rd -> rd.getToClass().clazz.equals(toClass)).findFirst();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			RelationDescriberList that = (RelationDescriberList) o;
			return fromClass.equals(that.fromClass);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), fromClass);
		}

		public RelationDescriberList addRelations(List<RelationDescriber> relations) {
			addAll(relations);
			return this;
		}
	}
}
