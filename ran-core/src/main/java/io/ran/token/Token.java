/* Copyright 2021 PSQR
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.ran.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Token {
	List<String> parts = new ArrayList<>();
	private static Map<String, Token> tokenMap = new ConcurrentHashMap<>();
	private Map<Class, String> toStringMap = new ConcurrentHashMap<>();

	private Token(List<String> parts) {
		if (parts.stream().anyMatch(String::isEmpty)) {
			throw new InvalidTokenException("Empty token part in: {" + String.join("}{", parts) + "}");
		}
		this.parts.addAll(parts);
	}

	static public Token get(String anyFormat) {
		if (anyFormat.toLowerCase().contains(" ")) {
			return humanReadable(anyFormat);
		}
		if (anyFormat.substring(0, 1).toUpperCase().equals(anyFormat.substring(0, 1))) {
			return CamelCase(anyFormat);
		}
		if (anyFormat.toLowerCase().equals(anyFormat)) {
			return snake_case(anyFormat);
		}
		return camelHump(anyFormat);
	}

	static public Token of(String... parts) {
		return new Token(Arrays.asList(parts));
	}

	static public Token of(List<String> parts) {
		return new Token(parts);
	}

	static public TokenList list() {
		return new TokenList();
	}

	public static Token CamelCase(String tokenString) {
		return tokenMap.computeIfAbsent(tokenString + "_CamelCase", t -> {
			return new CamelCaseToken(tokenString).toToken();
		});
	}

	public static Token camelHump(String tokenString) {
		return tokenMap.computeIfAbsent(tokenString + "_camelHump", t -> {
			return new CamelHumpToken(tokenString).toToken();
		});
	}

	public static Token snake_case(String tokenString) {
		return tokenMap.computeIfAbsent(tokenString + "_snake_case", t -> {
			return new SnakeCaseToken(tokenString).toToken();
		});
	}

	public static Token javaMethod(String tokenString) {
		return tokenMap.computeIfAbsent(tokenString + "_javaMethod", t -> {
			if (tokenString.substring(0, 1).equals(tokenString.substring(0, 1).toLowerCase())) {
				return new CamelHumpToken(tokenString).toToken();
			} else {
				return new CamelCaseToken(tokenString).toToken();
			}
		});
	}

	public static Token humanReadable(String tokenString) {
		return new HumanReadableToken(tokenString).toToken();
	}

	public <T extends TokenType> String toString(Class<T> type) {

		return toStringMap.computeIfAbsent(type, t -> {
			try {
				T tokenType = type.newInstance();
				tokenType.setToken(this);
				return tokenType.toString();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new InvalidTokenException(e);
			}
		});

	}

	@Override
	public String toString() {
		return "{" + String.join("}{", parts) + "}";
	}

	public int stringLength() {
		return parts.stream().mapToInt(String::length).sum();
	}

	public List<String> getParts() {
		return parts;
	}

	public String snake_case() {
		return toString(SnakeCaseToken.class);
	}

	public String camelHump() {
		return toString(CamelHumpToken.class);
	}

	public String CamelBack() {
		return toString(CamelCaseToken.class);
	}

	public String javaGetter() {
		if (parts.get(0).length() == 1) {
			return camelHump();
		} else {
			return CamelBack();
		}
	}

	public String humanReadable() {
		return toString(HumanReadableToken.class);
	}

	public boolean endsWith(String id) {
		return parts.get(parts.size() - 1).equals(id);
	}

	public static class TokenList extends ArrayList<Token> {
		public void add(String snakeCase) {
			add(Token.snake_case(snakeCase));
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Token token = (Token) o;
		return Objects.equals(parts, token.parts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parts);
	}
}
