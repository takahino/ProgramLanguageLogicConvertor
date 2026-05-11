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

package io.github.takahino.cpp2csharp.converter;

import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link CppToCSharpConverter} の統合テスト。 実際の C++ コードをパースして変換ルールを適用するシナリオをテストする。
 */
@DisplayName("CppToCSharpConverter 統合テスト")
class CppToCSharpConverterTest {

	private CppToCSharpConverter converter;
	private ConversionRuleLoader loader;

	@BeforeEach
	void setUp() {
		// 各テストで 1 converter を使用。マルチスレッド化時は converter を共有しないこと。
		converter = new CppToCSharpConverter();
		loader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());
	}

	/**
	 * ルール文字列からルールリストを生成するヘルパー。
	 */
	private List<ConversionRule> rules(String... fromToPairs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fromToPairs.length; i += 2) {
			sb.append("from: ").append(fromToPairs[i]).append("\n");
			sb.append("to: ").append(fromToPairs[i + 1]).append("\n\n");
		}
		return loader.loadFromString(sb.toString(), "test.rule");
	}

	@Test
	@DisplayName("型変換: CString → string")
	void testTypeConversionCString() {
		// C++ の変数宣言を含む最小コード (翻訳単位として認識できる形)
		String cpp = "CString s;";
		List<ConversionRule> rules = rules("CString", "string");

		ConversionResult result = converter.convertSource(cpp, rules);
		assertThat(result.getCsCode()).contains("string");
		assertThat(result.getCsCode()).doesNotContain("CString");
	}

	@Test
	@DisplayName("sin 関数の変換")
	void testSinFunctionConversion() {
		String cpp = "double y = sin(x);";
		List<ConversionRule> rules = rules("sin ( ABSTRACT_PARAM00 )", "Math.Sin(ABSTRACT_PARAM00)");

		ConversionResult result = converter.convertSource(cpp, rules);
		assertThat(result.getCsCode()).contains("Math.Sin");
		assertThat(result.getCsCode()).doesNotContain("sin (");
	}

	@Test
	@DisplayName("pow 関数の変換 (2つの ABSTRACT_PARAM)")
	void testPowFunctionConversion() {
		String cpp = "double z = pow(x, 2.0);";
		List<ConversionRule> rules = rules("pow ( ABSTRACT_PARAM00 , ABSTRACT_PARAM01 )",
				"Math.Pow(ABSTRACT_PARAM00, ABSTRACT_PARAM01)");

		ConversionResult result = converter.convertSource(cpp, rules);
		assertThat(result.getCsCode()).contains("Math.Pow");
	}

	@Test
	@DisplayName("AfxMessageBox の変換 (ABSTRACT_PARAM に単純文字列リテラル)")
	void testAfxMessageBoxSimple() {
		String cpp = """
				void f() {
				    AfxMessageBox("エラーです", MB_OK | MB_ICONERROR);
				}
				""";
		List<ConversionRule> rules = rules("AfxMessageBox ( ABSTRACT_PARAM00 , MB_OK | MB_ICONERROR ) ;",
				"MessageBox.Show(ABSTRACT_PARAM00, \"\", MessageBoxButtons.OK, MessageBoxIcon.Error);");

		ConversionResult result = converter.convertSource(cpp, rules);
		assertThat(result.getCsCode()).contains("MessageBox.Show");
		assertThat(result.getCsCode()).contains("MessageBoxButtons.OK");
		assertThat(result.getCsCode()).contains("MessageBoxIcon.Error");
	}

	@Test
	@DisplayName("NULL → null の変換")
	void testNullConversion() {
		String cpp = "void* p = NULL;";
		List<ConversionRule> rules = rules("NULL", "null");

		ConversionResult result = converter.convertSource(cpp, rules);
		assertThat(result.getCsCode()).contains("null");
		assertThat(result.getCsCode()).doesNotContain("NULL");
	}

	@Test
	@DisplayName("変換ルールがない場合は元トークンをそのまま出力する")
	void testNoRulesPassthrough() {
		String cpp = "int x = 42;";
		List<ConversionRule> rules = List.of();

		ConversionResult result = converter.convertSource(cpp, rules);
		assertThat(result.getCsCode()).contains("int");
		assertThat(result.getCsCode()).contains("x");
		assertThat(result.getCsCode()).contains("42");
	}

	@Test
	@DisplayName("複数のルールが順次適用される")
	void testMultipleRulesApplied() {
		String cpp = "CString s = NULL;";
		List<ConversionRule> rules = rules("CString", "string", "NULL", "null");

		ConversionResult result = converter.convertSource(cpp, rules);
		assertThat(result.getCsCode()).contains("string");
		assertThat(result.getCsCode()).contains("null");
		assertThat(result.getCsCode()).doesNotContain("CString");
		assertThat(result.getCsCode()).doesNotContain("NULL");
	}

	@Test
	@DisplayName("タスク2: #include / #define が出力に書き戻される")
	void testPreprocessorDirectivesPreserved() {
		String cpp = """
				#include "stdafx.h"
				#define MAX 100
				int x = 42;
				""";
		ConversionResult result = converter.convertSource(cpp, List.of());

		assertThat(result.getCsCode()).contains("#include \"stdafx.h\"");
		assertThat(result.getCsCode()).contains("#define MAX 100");
	}

	@Test
	@DisplayName("タスク3: 変換結果の末尾に <EOF> が含まれない")
	void testEofNotInOutput() {
		String cpp = "int x = 42;";
		ConversionResult result = converter.convertSource(cpp, List.of());

		assertThat(result.getCsCode()).doesNotContain("<EOF>");
	}

	@Test
	@DisplayName("タスク4: 改行が出力に保持される")
	void testNewlinesPreserved() {
		String cpp = """
				void f() {
				    int x = 1;
				}
				""";
		ConversionResult result = converter.convertSource(cpp, List.of());

		assertThat(result.getCsCode()).contains("\n");
	}
}
