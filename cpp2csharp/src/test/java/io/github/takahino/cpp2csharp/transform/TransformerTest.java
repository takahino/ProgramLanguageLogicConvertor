// === LICENSE_START ===
// # LICENSE
// 
// This software is licensed only under the T. Hino Commercial License
// (THCL) v1.0. Use, copying, modification, distribution, academic use,
// commercial use, and use by corporations or legal entities require
// compliance with the terms below.
// 
// ---
// 
// ## T. Hino Commercial License (THCL) v1.0
// 
// Copyright (c) 2026 T. Hino. All rights reserved.
// 
// This license governs the use of ProgramLanguageLogicConvertor
// (hereinafter "the Software"), developed by T. Hino (hereinafter "the Author").
// 
// 1. Grant of License
//    Any person or entity wishing to use, copy, modify, distribute, or
//    otherwise handle the Software must submit a usage application to the
//    Author and obtain written or electronic approval before a license is
//    granted.
//    Any use without such approval shall be deemed copyright infringement.
// 
//    Electronic records include:
//    - Email
//    - Comments made by the Author on the Software's repository
// 
// 2. License Term
//    The license is valid for one (1) year from the date of grant.
//    To continue use, a renewal application must be submitted to the Author
//    no later than thirty (30) days before expiration, and re-approval must
//    be obtained.
// 
// 3. License Fee
//    The license fee shall be determined separately by mutual agreement
//    between the Author and the licensee.
//    If the license is granted free of charge, such agreement shall be
//    explicitly stated in writing or electronic record.
//    The Author reserves the right to set a new license fee upon each renewal.
// 
// 4. Effect of License Expiration
//    If renewal is not approved, the license to use the Software itself
//    shall expire at the end of the license term.
//    However, any output or deliverables (e.g., converted source code)
//    generated using the Software during the valid license period may
//    continue to be used after license expiration.
// 
// 5. Restriction on Modification and Redistribution
//    Any modification or redistribution of the Software requires separate
//    written or electronic approval from the Author.
//    Use, distribution, or publication of modified versions without such
//    approval shall constitute a violation of this license.
// 
// 6. Retention of Copyright Notice
//    The following copyright notice must be retained in all copies and
//    derivative works of the Software:
// 
//    "Copyright (c) 2026 T. Hino. Licensed under THCL."
// 
//    The method of retention shall be as follows depending on usage:
// 
//    (a) When copying or modifying source code:
//        The above notice must be included in a comment at the top of
//        each source file.
// 
//    (b) When distributing in binary or executable form:
//        At least one of the following must be satisfied:
//        - Include the above notice in the application's About dialog
//        - Include the above notice in documentation (e.g., README)
//          bundled with the distribution
// 
//    (c) When used as an internal tool or system:
//        The above notice must be included in the help screen or
//        version information screen of the system.
// 
//    Modification or deletion of the above notice shall constitute
//    a violation of this license.
// 
// 7. Disclaimer
//    The Software is provided "as is" without warranty of any kind.
//    The Author shall not be liable for any damages arising from the
//    use of the Software.
// 
// 8. Citation Requirement for Academic Use
//    When the logic, algorithms, or design concepts of the Software are
//    used or referenced in papers, technical documents, academic presentations,
//    or similar works, the Author and the Software must be explicitly cited
//    in the following format:
// 
//    [Citation Format]
//    T. Hino, "ProgramLanguageLogicConvertor", GitHub,
//    https://github.com/takahino/ProgramLanguageLogicConvertor, [Date Accessed]
// 
//    Academic use without proper citation shall constitute a violation
//    of this license.
//    If a citation is made, it is recommended that the Author be notified
//    via email or a repository Issue.
// 
// ---
// 
// Contact  : takahino@ymail.ne.jp
// Inquiries: https://github.com/takahino/ProgramLanguageLogicConvertor/issues
// Repository: https://github.com/takahino/ProgramLanguageLogicConvertor
// 
// ---
// 
// ## Applicable License
// 
// All use cases are governed by THCL v1.0. A usage application and approval
// from the Author are required before use unless the Author has separately
// granted permission in writing or electronic record.
// === LICENSE_END ===

package io.github.takahino.cpp2csharp.transform;

import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.transform.strategy.RightmostFirstSelectionStrategy;
import io.github.takahino.cpp2csharp.tree.AstNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link Transformer} のユニットテスト。 フラットなトークンリストを直接構築して変換処理を検証する。
 */
@DisplayName("Transformer テスト")
class TransformerTest {

	private Transformer transformer;
	private ConversionRuleLoader loader;

	@BeforeEach
	void setUp() {
		transformer = new Transformer();
		loader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());
	}

	/**
	 * ルール文字列からルールリストを生成するヘルパー。
	 */
	private List<ConversionRule> rules(String from, String to) {
		String content = "from: " + from + "\nto: " + to;
		return loader.loadFromString(content, "test.rule");
	}

	/**
	 * 複数のルール文字列からルールリストを生成するヘルパー。
	 */
	private List<ConversionRule> multiRules(String... fromToAlternating) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fromToAlternating.length - 1; i += 2) {
			sb.append("from: ").append(fromToAlternating[i]).append("\n");
			sb.append("to: ").append(fromToAlternating[i + 1]).append("\n");
		}
		return loader.loadFromString(sb.toString(), "test.rule");
	}

	/**
	 * フラットなトークン列から {@code List<AstNode>} を生成するヘルパー。
	 */
	private List<AstNode> buildFlatTokenList(String... tokenTexts) {
		List<AstNode> result = new ArrayList<>();
		int col = 0;
		for (String text : tokenTexts) {
			result.add(AstNode.tokenNode(text, 1, col++));
		}
		return result;
	}

	@Test
	@DisplayName("型変換ルールを単純リストに適用できる")
	void testTypeConversionOnFlatGraph() {
		List<AstNode> tokens = buildFlatTokenList("CString", "s", ";");
		List<ConversionRule> r = rules("CString", "string");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("string");
		assertThat(result).doesNotContain("CString");
	}

	@Test
	@DisplayName("sin 関数変換を単純リストに適用できる")
	void testSinConversionOnFlatGraph() {
		List<AstNode> tokens = buildFlatTokenList("sin", "(", "x", ")");
		List<ConversionRule> r = rules("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("Math.Sin");
		assertThat(result).contains("x");
	}

	@Test
	@DisplayName("変換後も残りのトークンが保持される")
	void testRemainingTokensPreserved() {
		List<AstNode> tokens = buildFlatTokenList("double", "y", "=", "sin", "(", "x", ")", ";");
		List<ConversionRule> r = rules("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("double");
		assertThat(result).contains("y");
		assertThat(result).contains("=");
		assertThat(result).contains("Math.Sin");
		assertThat(result).contains(";");
	}

	@Test
	@DisplayName("マッチなしの場合はエラーが記録されない")
	void testNoMatchNoErrors() {
		List<AstNode> tokens = buildFlatTokenList("int", "x", "=", "42", ";");
		List<ConversionRule> r = rules("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");

		transformer.transform(tokens, r);
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("ルールなしの場合はトークンをそのまま結合する")
	void testNoRulesPassthrough() {
		List<AstNode> tokens = buildFlatTokenList("int", "x", "=", "42", ";");

		String result = transformer.transform(tokens, List.of());
		assertThat(result).contains("int");
		assertThat(result).contains("x");
		assertThat(result).contains("42");
		assertThat(result).contains(";");
	}

	@Test
	@DisplayName("ドットチェーン a.Foo().Bar() を内側から正しく変換できる (0引数メソッド)")
	void testDotChainConvertedInsideOut() {
		// トークン列: a . Foo ( ) . Bar ( )
		List<AstNode> tokens = buildFlatTokenList("a", ".", "Foo", "(", ")", ".", "Bar", "(", ")");
		List<ConversionRule> r = multiRules("ABSTRACT_PARAM00 . Foo ( )", "Foo(ABSTRACT_PARAM00)",
				"ABSTRACT_PARAM00 . Bar ( )", "Bar(ABSTRACT_PARAM00)");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("Bar(Foo(a))");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("pow 変換 (2つの ABSTRACT_PARAM) をリストに適用できる")
	void testPowConversionOnFlatGraph() {
		List<AstNode> tokens = buildFlatTokenList("pow", "(", "base", ",", "exp", ")");
		List<ConversionRule> r = rules("pow ( ABSTRACT_PARAM00 , ABSTRACT_PARAM01 )",
				"Math.Pow(ABSTRACT_PARAM00, ABSTRACT_PARAM01)");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("Math.Pow");
		assertThat(result).contains("base");
		assertThat(result).contains("exp");
	}

	// ── RECEIVER テスト ────────────────────────────────────────────────────

	@Test
	@DisplayName("RECEIVER: 単体識別子レシーバー str.Left(5)")
	void testReceiver00SimpleIdentifier() {
		List<AstNode> tokens = buildFlatTokenList("str", ".", "Left", "(", "5", ")");
		List<ConversionRule> r = rules("RECEIVER . Left ( ABSTRACT_PARAM00 )",
				"RECEIVER . Substring ( 0 , ABSTRACT_PARAM00 )");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("str");
		assertThat(result).contains("Substring");
		assertThat(result).contains("0");
		assertThat(result).contains("5");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: メンバアクセス連鎖 app.m_str.Left(5)")
	void testReceiver00MemberAccessChain() {
		List<AstNode> tokens = buildFlatTokenList("app", ".", "m_str", ".", "Left", "(", "5", ")");
		List<ConversionRule> r = rules("RECEIVER . Left ( ABSTRACT_PARAM00 )",
				"RECEIVER . Substring ( 0 , ABSTRACT_PARAM00 )");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("app");
		assertThat(result).contains("m_str");
		assertThat(result).contains("Substring");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: アロー演算子チェーン this->m_str.Left(5)")
	void testReceiver00ArrowChain() {
		List<AstNode> tokens = buildFlatTokenList("this", "->", "m_str", ".", "Left", "(", "5", ")");
		List<ConversionRule> r = rules("RECEIVER . Left ( ABSTRACT_PARAM00 )",
				"RECEIVER . Substring ( 0 , ABSTRACT_PARAM00 )");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("this");
		assertThat(result).contains("->");
		assertThat(result).contains("m_str");
		assertThat(result).contains("Substring");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: 添字アクセスレシーバー arr[0].Left(5)")
	void testReceiver00SubscriptReceiver() {
		List<AstNode> tokens = buildFlatTokenList("arr", "[", "0", "]", ".", "Left", "(", "5", ")");
		List<ConversionRule> r = rules("RECEIVER . Left ( ABSTRACT_PARAM00 )",
				"RECEIVER . Substring ( 0 , ABSTRACT_PARAM00 )");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("arr");
		assertThat(result).contains("Substring");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: 変換済み合成トークンをレシーバーにできる（MakeString が単一トークン化された場合）")
	void testReceiver00SyntheticTokenReceiver() {
		List<AstNode> tokens = buildFlatTokenList("MakeString(data)", ".", "Left", "(", "10", ")");
		List<ConversionRule> r = rules("RECEIVER . Left ( ABSTRACT_PARAM00 )",
				"RECEIVER . Substring ( 0 , ABSTRACT_PARAM00 )");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("MakeString(data)");
		assertThat(result).contains("Substring");
		assertThat(result).contains("10");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: 未変換の関数呼び出し結果もレシーバーとして変換できる")
	void testReceiver00RejectUnconvertedFunctionCallReceiver() {
		List<AstNode> tokens = buildFlatTokenList("MakeString", "(", "data", ")", ".", "Left", "(", "10", ")");
		List<ConversionRule> r = rules("RECEIVER . Left ( ABSTRACT_PARAM00 )",
				"RECEIVER . Substring ( 0 , ABSTRACT_PARAM00 )");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("Substring");
		assertThat(result).contains("MakeString");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: 関数呼び出し結果に対して Find が変換できる")
	void testReceiver00FunctionCallReceiverWithFind() {
		List<AstNode> tokens = buildFlatTokenList("GetString", "(", "data", ")", ".", "Find", "(", "\"/ \"", ")");
		List<ConversionRule> r = rules("RECEIVER . Find ( ABSTRACT_PARAM00 )",
				"MigrationHelper.Find(RECEIVER, ABSTRACT_PARAM00)");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("MigrationHelper.Find");
		assertThat(result).contains("GetString");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: 多段メソッドチェーン app.method().field.Left(5)")
	void testReceiver00MultiStepChain() {
		List<AstNode> tokens = buildFlatTokenList("app", ".", "method", "(", ")", ".", "field", ".", "Left", "(", "5",
				")");
		List<ConversionRule> r = rules("RECEIVER . Left ( ABSTRACT_PARAM00 )",
				"RECEIVER . Substring ( 0 , ABSTRACT_PARAM00 )");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("app");
		assertThat(result).contains("Substring");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: 拒否系 - a+b はレシーバーにならない（b.Left のみ変換）")
	void testReceiver00RejectBinaryOpAtDepthZero() {
		List<AstNode> tokens = buildFlatTokenList("a", "+", "b", ".", "Left", "(", "5", ")");
		List<ConversionRule> r = rules("RECEIVER . Left ( ABSTRACT_PARAM00 )",
				"RECEIVER . Substring ( 0 , ABSTRACT_PARAM00 )");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("Substring");
		assertThat(result).contains("a");
		assertThat(result).contains("+");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: RECEIVER をto側にも展開できる")
	void testReceiver00InToTemplate() {
		List<AstNode> tokens = buildFlatTokenList("str", ".", "Find", "(", "x", ")");
		List<ConversionRule> r = rules("RECEIVER . Find ( ABSTRACT_PARAM00 )",
				"MigrationHelper.Find(RECEIVER, ABSTRACT_PARAM00)");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("MigrationHelper.Find(str, x)");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("RECEIVER: this.m_str.Find(x) が変換される（従来 ABSTRACT_PARAM では不可）")
	void testReceiver00DotChainFindPreviouslyBlocked() {
		List<AstNode> tokens = buildFlatTokenList("this", ".", "m_str", ".", "Find", "(", "x", ")");
		List<ConversionRule> r = rules("RECEIVER . Find ( ABSTRACT_PARAM00 )",
				"MigrationHelper.Find(RECEIVER, ABSTRACT_PARAM00)");

		String result = transformer.transform(tokens, r);
		assertThat(result).contains("MigrationHelper.Find");
		assertThat(result).contains("this");
		assertThat(result).contains("m_str");
		assertThat(transformer.getErrors()).isEmpty();
	}

	// ── 選択戦略・可視化テスト ────────────────────────────────────────────────────

	@Test
	@DisplayName("RightmostFirstSelectionStrategy: 適用ログに選択戦略が記録される")
	void testSelectionStrategyRecordedInAppliedTransforms() {
		Transformer rightmostTransformer = new Transformer(50000, new RightmostFirstSelectionStrategy());
		List<AstNode> tokens = buildFlatTokenList("sin", "(", "x", ")");
		List<ConversionRule> r = rules("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");

		rightmostTransformer.transform(tokens, r);

		assertThat(rightmostTransformer.getAppliedTransforms()).hasSize(1);
		var t = rightmostTransformer.getAppliedTransforms().get(0);
		assertThat(t.selectedStrategy()).isEqualTo("RightmostFirstSelectionStrategy");
		assertThat(t.fallbackFrom()).isNull();
		assertThat(t.selectionReason()).isNotEmpty();
	}

	@Test
	@DisplayName("RightmostFirstSelectionStrategy: sin(x)+sin(y) で右端の sin(y) が先に変換される")
	void testRightmostFirstTieBreakByPosition() {
		Transformer rmFirst = new Transformer(50000, new RightmostFirstSelectionStrategy());
		List<AstNode> tokens = buildFlatTokenList("sin", "(", "x", ")", "+", "sin", "(", "y", ")");
		List<ConversionRule> r = rules("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");

		String result = rmFirst.transform(tokens, r);
		assertThat(result).contains("Math.Sin");
		assertThat(result).contains("x");
		assertThat(result).contains("y");
		assertThat(rmFirst.getErrors()).isEmpty();
		assertThat(rmFirst.getAppliedTransforms())
				.allMatch(t -> "RightmostFirstSelectionStrategy".equals(t.selectedStrategy()));
		assertThat(rmFirst.getAppliedTransforms()).allMatch(t -> t.fallbackFrom() == null);
	}

	// ── hasDotWithParenAtDepthZero 検証（ABSTRACT_PARAM00
	// 先頭ルール）────────────────────────────
	//
	// 【背景】RightmostFirstSelectionStrategy.passesLeadingAbstractParamFilter は、
	// 先頭トークンが ABSTRACT_PARAM00 のルールにのみ適用される。その中の (b) 条件として
	// hasDotWithParenAtDepthZero があり、「キャプチャ内で深さ0に . と ( の両方がある」場合に
	// そのマッチを拒否する。目的は「time.Format(...).Foo()」のようなドットチェーン内の
	// 関数呼び出しを ABSTRACT_PARAM で丸ごとキャプチャさせず、RECEIVER ルールで内側から
	// 変換させること。「this.m_str」のような単純メンバアクセスは . のみで ( がないため許可。
	//
	// 【ルールファイルを使わない理由】現行ルールセットには先頭が ABSTRACT_PARAM00 のルールが
	// 存在しないため、このフィルタは実質デッドコード。将来用・防御的実装として残しており、
	// 検証には loadFromString でテスト専用ルールを生成する。

	@Test
	@DisplayName("ABSTRACT_PARAM00先頭: 単一トークン obj は許可（hasDotWithParenAtDepthZero=false）")
	void testLeadingAbstractParamAllowsSimpleMemberAccess() {
		// キャプチャ "obj" には . も ( も含まない → hasDotWithParenAtDepthZero=false → 許可。
		// 単一トークンにしたのは、this.m_str.Foo() だと右端優先で m_str.Foo() が選ばれ
		// 本フィルタの検証にならないため。
		List<AstNode> tokens = buildFlatTokenList("obj", ".", "Foo", "(", ")");
		List<ConversionRule> r = rules("ABSTRACT_PARAM00 . Foo ( )", "Foo(ABSTRACT_PARAM00)");

		String result = transformer.transform(tokens, r);

		assertThat(result).contains("Foo(obj)");
		assertThat(transformer.getErrors()).isEmpty();
	}

	@Test
	@DisplayName("ABSTRACT_PARAM00先頭: time.Format(...) は拒否し Format が先に変換される（hasDotWithParenAtDepthZero=true）")
	void testLeadingAbstractParamRejectsDotChainWithFunctionCall() {
		// 入力: time.Format("%Y/%m/%d").Foo()
		// Foo ルールの ABSTRACT_PARAM00 が "time.Format("%Y/%m/%d")" をキャプチャしようとするが、
		// 深さ0で . と ( の両方がある → hasDotWithParenAtDepthZero=true → 拒否。
		// その結果 Format ルールが先に適用され、2パス目で Foo が MigrationHelper.Format(...) に適用。
		// 期待: Foo(MigrationHelper.Format(time, "%Y/%m/%d")) のような内側から正しい変換順序。
		List<AstNode> tokens = buildFlatTokenList("time", ".", "Format", "(", "\"%Y/%m/%d\"", ")", ".", "Foo", "(",
				")");
		List<ConversionRule> r = multiRules("RECEIVER . Format ( ABSTRACT_PARAM00 )",
				"MigrationHelper.Format(RECEIVER, ABSTRACT_PARAM00)", "ABSTRACT_PARAM00 . Foo ( )",
				"Foo(ABSTRACT_PARAM00)");

		String result = transformer.transform(tokens, r);

		assertThat(result).contains("MigrationHelper.Format");
		assertThat(result).contains("Foo(");
		assertThat(result).contains("time");
		assertThat(transformer.getErrors()).isEmpty();
	}
}
