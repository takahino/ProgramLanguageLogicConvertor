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

package io.github.takahino.cpp2csharp.matcher;

import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link PatternMatcher} のユニットテスト。
 */
@DisplayName("PatternMatcher テスト")
class PatternMatcherTest {

	private PatternMatcher matcher;
	private ConversionRuleLoader loader;

	@BeforeEach
	void setUp() {
		matcher = new PatternMatcher();
		loader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());
	}

	/**
	 * ルール文字列からルールを生成するヘルパー。
	 */
	private ConversionRule rule(String from, String to) {
		String content = "from: " + from + "\nto: " + to;
		return loader.loadFromString(content, "test.rule").get(0);
	}

	@Test
	@DisplayName("具体トークンのみのパターンがマッチする")
	void testConcreteTokenMatch() {
		ConversionRule r = rule("CString", "string");
		List<String> tokens = List.of("CString");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getStartIndex()).isEqualTo(0);
		assertThat(results.get(0).getEndIndex()).isEqualTo(1);
	}

	@Test
	@DisplayName("具体トークンが不一致の場合はマッチしない")
	void testConcreteTokenNoMatch() {
		ConversionRule r = rule("CString", "string");
		List<String> tokens = List.of("int");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("ABSTRACT_PARAM を含むパターンが正しくマッチする")
	void testAbstractParamMatch() {
		ConversionRule r = rule("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");
		List<String> tokens = List.of("sin", "(", "x", ")");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getCapturedTokens(0)).isEqualTo(List.of("x"));
	}

	@Test
	@DisplayName("複数トークンの ABSTRACT_PARAM がマッチする")
	void testAbstractParamMultipleTokens() {
		ConversionRule r = rule("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");
		// sin ( CreateMessage ( a , b ) )
		List<String> tokens = List.of("sin", "(", "CreateMessage", "(", "a", ",", "b", ")", ")");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getCapturedTokens(0)).isEqualTo(List.of("CreateMessage", "(", "a", ",", "b", ")"));
	}

	@Test
	@DisplayName("AfxMessageBox パターンが正しくマッチする")
	void testAfxMessageBoxPattern() {
		ConversionRule r = rule("AfxMessageBox ( ABSTRACT_PARAM00 , MB_OK | MB_ICONERROR ) ;",
				"MessageBox.Show(ABSTRACT_PARAM00, \"\", MessageBoxButtons.OK, MessageBoxIcon.Error);");
		List<String> tokens = List.of("AfxMessageBox", "(", "\"エラーが発生しました\"", ",", "MB_OK", "|", "MB_ICONERROR", ")",
				";");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getCapturedText(0)).isEqualTo("\"エラーが発生しました\"");
	}

	@Test
	@DisplayName("複雑な ABSTRACT_PARAM (ネスト式) がマッチする")
	void testNestedExpressionAbstractParam() {
		ConversionRule r = rule("AfxMessageBox ( ABSTRACT_PARAM00 , MB_OK | MB_ICONERROR ) ;",
				"MessageBox.Show(ABSTRACT_PARAM00, \"\", MessageBoxButtons.OK, MessageBoxIcon.Error);");
		// CreateMessage(a, b) + "abcd" の部分が ABSTRACT_PARAM00 になる
		List<String> tokens = List.of("AfxMessageBox", "(", "CreateMessage", "(", "a", ",", "b", ")", "+", "\"abcd\"",
				",", "MB_OK", "|", "MB_ICONERROR", ")", ";");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).hasSize(1);

		List<String> captured = results.get(0).getCapturedTokens(0);
		assertThat(captured).containsExactly("CreateMessage", "(", "a", ",", "b", ")", "+", "\"abcd\"");
	}

	@Test
	@DisplayName("2つの ABSTRACT_PARAM を含むパターンがマッチする")
	void testTwoAbstractParams() {
		ConversionRule r = rule("pow ( ABSTRACT_PARAM00 , ABSTRACT_PARAM01 )",
				"Math.Pow(ABSTRACT_PARAM00, ABSTRACT_PARAM01)");
		List<String> tokens = List.of("pow", "(", "x", ",", "2.0", ")");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getCapturedText(0)).isEqualTo("x");
		assertThat(results.get(0).getCapturedText(1)).isEqualTo("2.0");
	}

	@Test
	@DisplayName("トークン列の途中にマッチを見つけられる")
	void testMatchInMiddle() {
		ConversionRule r = rule("CString", "string");
		List<String> tokens = List.of("void", "func", "(", "CString", "param", ")");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getStartIndex()).isEqualTo(3);
		assertThat(results.get(0).getEndIndex()).isEqualTo(4);
	}

	@Test
	@DisplayName("matchAll で複数ルールをまとめて検索できる")
	void testMatchAll() {
		List<ConversionRule> rules = List.of(rule("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)"),
				rule("cos ( ABSTRACT_PARAM00 )", "Math.Cos(ABSTRACT_PARAM00)"));
		List<String> tokens = List.of("sin", "(", "x", ")");

		List<MatchResult> results = matcher.matchAll(rules, tokens);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getRule().getToTemplate()).isEqualTo("Math.Sin(ABSTRACT_PARAM00)");
	}

	@Test
	@DisplayName("1引数ルールは2引数の呼び出しにマッチしない（引数数保護）")
	void testSingleArgRuleDoesNotMatchTwoArgCall() {
		ConversionRule r = rule("AfxMessageBox ( ABSTRACT_PARAM00 ) ;", "MessageBox.Show(ABSTRACT_PARAM00);");
		// AfxMessageBox("Hello", MB_OK) ; — 引数2個なのでマッチしてはいけない
		List<String> tokens = List.of("AfxMessageBox", "(", "\"Hello\"", ",", "MB_OK", ")", ";");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).as("引数2個の呼び出しは1引数ルールにマッチしないこと").isEmpty();
	}

	@Test
	@DisplayName("1引数ルールはネスト式（内部カンマあり）にマッチする")
	void testSingleArgRuleMatchesNestedExprWithInternalComma() {
		ConversionRule r = rule("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");
		// sin ( CreateMessage ( a , b ) ) — カンマはdepth1なのでマッチする
		List<String> tokens = List.of("sin", "(", "CreateMessage", "(", "a", ",", "b", ")", ")");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).as("ネスト式（depth>0のカンマ）は1引数ルールにマッチすること").hasSize(1);
		assertThat(results.get(0).getCapturedTokens(0)).isEqualTo(List.of("CreateMessage", "(", "a", ",", "b", ")"));
	}

	@Test
	@DisplayName("マッチなしの場合は空リストを返す")
	void testNoMatchReturnsEmptyList() {
		ConversionRule r = rule("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");
		List<String> tokens = List.of("cos", "(", "x", ")");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("MB_YESNO | MB_ICONQUESTION パターンがマッチする")
	void testAfxMessageBoxMbYesNo() {
		ConversionRule r = rule("AfxMessageBox ( ABSTRACT_PARAM00 , MB_YESNO | MB_ICONQUESTION )",
				"MessageBox.Show(ABSTRACT_PARAM00, \"\", MessageBoxButtons.YesNo, MessageBoxIcon.Question)");
		List<String> tokens = List.of("AfxMessageBox", "(", "question", ",", "MB_YESNO", "|", "MB_ICONQUESTION", ")",
				";");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).as("MB_YESNO ルールがマッチすること").hasSize(1);
		assertThat(results.get(0).getCapturedText(0)).isEqualTo("question");
	}

	@Test
	@DisplayName("getCapturedText で ABSTRACT_PARAM のテキストを取得できる")
	void testGetCapturedText() {
		ConversionRule r = rule("pow ( ABSTRACT_PARAM00 , ABSTRACT_PARAM01 )",
				"Math.Pow(ABSTRACT_PARAM00, ABSTRACT_PARAM01)");
		List<String> tokens = List.of("pow", "(", "a", "+", "b", ",", "c", "*", "d", ")");

		List<MatchResult> results = matcher.matchRule(r, tokens);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getCapturedText(0)).isEqualTo("a + b");
		assertThat(results.get(0).getCapturedText(1)).isEqualTo("c * d");
	}
}
