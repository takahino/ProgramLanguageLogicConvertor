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

package io.github.takahino.cpp2csharp.rule;

import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link ConversionRuleLoader} のユニットテスト。
 */
@DisplayName("ConversionRuleLoader テスト")
class ConversionRuleLoaderTest {

	private final ConversionRuleLoader loader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());

	@Test
	@DisplayName("基本的なルールを正しく読み込める")
	void testBasicRule() {
		String content = """
				from: this . AfxMessageBox ( ABSTRACT_PARAM00 , MB_OK | MB_ICONERROR ) ;
				to: MessageBox.Show(ABSTRACT_PARAM00, "", MessageBoxButtons.OK, MessageBoxIcon.Error);
				""";
		List<ConversionRule> rules = loader.loadFromString(content, "test.rule");
		assertThat(rules).hasSize(1);

		ConversionRule rule = rules.get(0);
		// this . AfxMessageBox ( ABSTRACT_PARAM00 , MB_OK | MB_ICONERROR ) ; = 11トークン
		assertThat(rule.getFromTokens()).hasSize(11);
		assertThat(rule.getToTemplate())
				.isEqualTo("MessageBox.Show(ABSTRACT_PARAM00, \"\", MessageBoxButtons.OK, MessageBoxIcon.Error);");
		assertThat(rule.getSourceFile()).isEqualTo("test.rule");
	}

	@Test
	@DisplayName("コメント行と空行を無視できる")
	void testIgnoreCommentsAndBlanks() {
		String content = """
				# これはコメント

				# 別のコメント
				from: sin ( ABSTRACT_PARAM00 )
				to: Math.Sin(ABSTRACT_PARAM00)

				""";
		List<ConversionRule> rules = loader.loadFromString(content, "test.rule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getFromTokens().get(0).getValue()).isEqualTo("sin");
	}

	@Test
	@DisplayName("複数のルールを正しく読み込める")
	void testMultipleRules() {
		String content = """
				from: sin ( ABSTRACT_PARAM00 )
				to: Math.Sin(ABSTRACT_PARAM00)

				from: cos ( ABSTRACT_PARAM00 )
				to: Math.Cos(ABSTRACT_PARAM00)

				from: pow ( ABSTRACT_PARAM00 , ABSTRACT_PARAM01 )
				to: Math.Pow(ABSTRACT_PARAM00, ABSTRACT_PARAM01)
				""";
		List<ConversionRule> rules = loader.loadFromString(content, "math.rule");
		assertThat(rules).hasSize(3);
		assertThat(rules.get(0).getFromTokens().get(0).getValue()).isEqualTo("sin");
		assertThat(rules.get(1).getFromTokens().get(0).getValue()).isEqualTo("cos");
		assertThat(rules.get(2).getFromTokens().get(0).getValue()).isEqualTo("pow");
	}

	@Test
	@DisplayName("ABSTRACT_PARAM を正しくトークン化できる")
	void testAbstractParamTokenization() {
		String content = """
				from: pow ( ABSTRACT_PARAM00 , ABSTRACT_PARAM01 )
				to: Math.Pow(ABSTRACT_PARAM00, ABSTRACT_PARAM01)
				""";
		List<ConversionRule> rules = loader.loadFromString(content, "test.rule");
		List<ConversionToken> tokens = rules.get(0).getFromTokens();

		// pow ( ABSTRACT_PARAM00 , ABSTRACT_PARAM01 )
		assertThat(tokens).hasSize(6);
		assertThat(tokens.get(0).isAbstractParam()).isFalse();
		assertThat(tokens.get(2).isAbstractParam()).isTrue();
		assertThat(tokens.get(2).getParamIndex()).isEqualTo(0);
		assertThat(tokens.get(4).isAbstractParam()).isTrue();
		assertThat(tokens.get(4).getParamIndex()).isEqualTo(1);
	}

	@Test
	@DisplayName("型変換ルール (具体トークンのみ) を正しく読み込める")
	void testTypeConversionRule() {
		String content = """
				from: CString
				to: string
				""";
		List<ConversionRule> rules = loader.loadFromString(content, "types.rule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getFromTokens()).hasSize(1);
		assertThat(rules.get(0).getFromTokens().get(0).getValue()).isEqualTo("CString");
		assertThat(rules.get(0).getToTemplate()).isEqualTo("string");
	}

	@Test
	@DisplayName("getAbstractParamCount が正しいカウントを返す")
	void testAbstractParamCount() {
		String content = """
				from: pow ( ABSTRACT_PARAM00 , ABSTRACT_PARAM01 )
				to: Math.Pow(ABSTRACT_PARAM00, ABSTRACT_PARAM01)
				""";
		List<ConversionRule> rules = loader.loadFromString(content, "test.rule");
		assertThat(rules.get(0).getAbstractParamCount()).isEqualTo(2);
	}

	@Test
	@DisplayName("getArgumentCount が括弧内カンマ数から引数個数を導出する")
	void testGetArgumentCount() {
		// 1引数 (カンマなし)
		ConversionRule r1 = loader
				.loadFromString("from: AfxMessageBox ( ABSTRACT_PARAM00 ) ;\nto: MessageBox.Show(ABSTRACT_PARAM00);",
						"test.rule")
				.get(0);
		assertThat(r1.getArgumentCount()).isEqualTo(1);

		// 2引数 (カンマ1つ)
		ConversionRule r2 = loader.loadFromString(
				"from: AfxMessageBox ( ABSTRACT_PARAM00 , MB_OK | MB_ICONERROR ) ;\nto: x;", "test.rule").get(0);
		assertThat(r2.getArgumentCount()).isEqualTo(2);

		// 2引数 (MB_YESNO)
		ConversionRule r3 = loader.loadFromString(
				"from: AfxMessageBox ( ABSTRACT_PARAM00 , MB_YESNO | MB_ICONQUESTION )\nto: x", "test.rule").get(0);
		assertThat(r3.getArgumentCount()).isEqualTo(2);

		// 括弧なし (型変換) → -1
		ConversionRule r4 = loader.loadFromString("from: CString\nto: string", "test.rule").get(0);
		assertThat(r4.getArgumentCount()).isEqualTo(-1);

		// 空括弧 → 0
		ConversionRule r5 = loader.loadFromString("from: func ( ) ;\nto: x;", "test.rule").get(0);
		assertThat(r5.getArgumentCount()).isEqualTo(0);

		// ネストした括弧 (f ( a , g ( x , y ) ))
		ConversionRule r6 = loader.loadFromString("from: f ( ABSTRACT_PARAM00 , g ( x , y ) )\nto: x", "test.rule")
				.get(0);
		assertThat(r6.getArgumentCount()).isEqualTo(2);
	}

	@Test
	@DisplayName("ANTLR トークン化: スペースなしでも正しく分割される")
	void testAntlrTokenizationWithoutSpaces() {
		String content = """
				from: AfxMessageBox(ABSTRACT_PARAM00,MB_OK|MB_ICONERROR);
				to: MessageBox.Show(ABSTRACT_PARAM00, "", MessageBoxButtons.OK, MessageBoxIcon.Error);
				""";
		List<ConversionRule> rules = loader.loadFromString(content, "test.rule");
		assertThat(rules).hasSize(1);
		List<ConversionToken> tokens = rules.get(0).getFromTokens();
		assertThat(tokens).extracting(ConversionToken::getValue).containsExactly("AfxMessageBox", "(",
				"ABSTRACT_PARAM00", ",", "MB_OK", "|", "MB_ICONERROR", ")", ";");
	}

	@Test
	@DisplayName("不正な from パターンで字句解析エラー")
	void testInvalidFromPatternThrows() {
		assertThatThrownBy(() -> loader.loadFromString("from: @invalid@\nto: x", "test.rule"))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("字句解析に失敗");
	}

	@Test
	@DisplayName("test: と assrt: を正しく読み込める")
	void testTestAndAssrtParsing() {
		String content = """
				from: AfxMessageBox ( ABSTRACT_PARAM00 , MB_OK ) ;
				to: MessageBox.Show(ABSTRACT_PARAM00, "", MessageBoxButtons.OK, MessageBoxIcon.None);
				test: void f() { AfxMessageBox("Hello", MB_OK); }
				assrt: void f ( ) { MessageBox.Show("Hello", "", MessageBoxButtons.OK, MessageBoxIcon.None); }
				""";
		List<ConversionRule> rules = loader.loadFromString(content, "test.rule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getTestCases()).hasSize(1);
		assertThat(rules.get(0).getTestCases().get(0).testInput())
				.isEqualTo("void f() { AfxMessageBox(\"Hello\", MB_OK); }");
		assertThat(rules.get(0).getTestCases().get(0).expectedOutput()).isEqualTo(
				"void f ( ) { MessageBox.Show(\"Hello\", \"\", MessageBoxButtons.OK, MessageBoxIcon.None); }");
	}

	@Test
	@DisplayName("test: と assrt: を複数読み込める")
	void testMultipleTestAssrtPairs() {
		String content = """
				from: sin ( ABSTRACT_PARAM00 )
				to: Math.Sin(ABSTRACT_PARAM00)
				test: void f() { sin(1.0); }
				assrt: void f ( ) { Math.Sin ( 1.0 ) ; }
				test: void g() { sin(x); }
				assrt: void g ( ) { Math.Sin ( x ) ; }
				""";
		List<ConversionRule> rules = loader.loadFromString(content, "math.rule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getTestCases()).hasSize(2);
		assertThat(rules.get(0).getTestCases().get(0).testInput()).isEqualTo("void f() { sin(1.0); }");
		assertThat(rules.get(0).getTestCases().get(1).testInput()).isEqualTo("void g() { sin(x); }");
	}
}
