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

package io.github.takahino.cpp2csharp.integration;

import io.github.takahino.cpp2csharp.converter.ConversionResult;
import io.github.takahino.cpp2csharp.converter.CppToCSharpConverter;
import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * LR寄せ計画の検証ケース: チェイン・ネスト・拒否系の回帰テスト。
 *
 * <p>
 * 計画の Phase 5 で定義した重点テストケースをカバーする。
 * </p>
 */
@DisplayName("チェイン・ネスト・拒否系 回帰テスト")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChainAndNestRegressionTest {

	private CppToCSharpConverter converter;
	private List<List<ConversionRule>> rulesByPhase;

	@BeforeAll
	void loadRules() throws IOException {
		ConversionRuleLoader loader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());
		List<List<ConversionRule>> phases = new ArrayList<>();
		phases.add(loader.loadFromResource("rules/main/[01]_ブロックコメント/pointer.rule"));
		List<ConversionRule> phase2 = new ArrayList<>();
		phase2.addAll(loader.loadFromResource("rules/main/[02]_標準置き換え/AfxMessageBox.rule"));
		phase2.addAll(loader.loadFromResource("rules/main/[02]_標準置き換え/array_declaration.rule"));
		phase2.addAll(loader.loadFromResource("rules/main/[02]_標準置き換え/math_functions.rule"));
		phase2.addAll(loader.loadFromResource("rules/main/[02]_標準置き換え/type_conversions.rule"));
		phase2.addAll(loader.loadFromResource("rules/main/[02]_標準置き換え/printf_functions.rule"));
		phase2.addAll(loader.loadFromResource("rules/main/[02]_標準置き換え/format_migration.rule"));
		phase2.addAll(loader.loadFromResource("rules/main/[02]_標準置き換え/migration_helper.rule"));
		phase2.addAll(loader.loadFromResource("rules/main/[02]_標準置き換え/cstring_methods.rule"));
		phases.add(phase2);
		phases.add(loader.loadFromResource("rules/main/[03]_パッチ置き換え/ToBool.rule"));
		rulesByPhase = phases;
	}

	@BeforeEach
	void setUp() {
		converter = new CppToCSharpConverter();
	}

	@Test
	@DisplayName("正常系: this->m_str.Left(5)")
	void thisArrowMStrLeft() throws IOException {
		String cpp = "void f() { CString s = this->m_str.Left(5); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("Substring");
		assertThat(result.getCsCode()).contains("this");
		assertThat(result.getCsCode()).contains("m_str");
	}

	@Test
	@DisplayName("正常系: app.method().field.Left(5)")
	void appMethodFieldLeft() throws IOException {
		String cpp = "void f() { CString s = app.method().field.Left(5); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("Substring");
		assertThat(result.getCsCode()).contains("app");
	}

	@Test
	@DisplayName("正常系: arr[0].Left(5)")
	void arrSubscriptLeft() throws IOException {
		String cpp = "void f() { CString s = arr[0].Left(5); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("Substring");
		assertThat(result.getCsCode()).contains("arr");
	}

	@Test
	@DisplayName("正常系: MakeString(data).Left(10)")
	void makeStringLeft() throws IOException {
		String cpp = "void f() { CString s = MakeString(data).Left(10); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("Substring");
		assertThat(result.getCsCode()).contains("MakeString");
	}

	@Test
	@DisplayName("正常系: time.Format(\"%Y\").Left(5)")
	void timeFormatLeft() throws IOException {
		String cpp = "void f() { CString s = time.Format(\"%Y\").Left(5); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("MigrationHelper.Format");
		assertThat(result.getCsCode()).contains("Substring");
	}

	@Test
	@DisplayName("回帰系: str.Find(time.Format(\"%Y/%m/%d\")) — 引数ネスト")
	void strFindTimeFormat() throws IOException {
		String cpp = "void f() { int i = str.Find(time.Format(\"%Y/%m/%d\")); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("MigrationHelper.Find");
		assertThat(result.getCsCode()).contains("MigrationHelper.Format");
	}

	@Test
	@DisplayName("回帰系: time.Format(\"%Y/%m/%d\").Find(\"/\") — ドットチェーン")
	void timeFormatFind() throws IOException {
		String cpp = "void f() { int i = time.Format(\"%Y/%m/%d\").Find(\"/\"); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("MigrationHelper.Format");
		assertThat(result.getCsCode()).contains("MigrationHelper.Find");
	}

	@Test
	@DisplayName("拒否系: (a+b).Left(5) — 括弧付き二項演算はレシーバーにならないため変換されない")
	void rejectBinaryOpReceiver() throws IOException {
		String cpp = "void f() { CString s = (a + b).Left(5); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).doesNotContain("Substring");
		assertThat(result.getCsCode()).contains(".Left(5)");
	}

	@Test
	@DisplayName("拒否系: cond?x:y.Left(5) — y のみレシーバー、三項演算全体はレシーバーにならない")
	void rejectTernaryReceiver() throws IOException {
		String cpp = "void f() { CString s = cond ? x : y.Left(5); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("Substring");
	}

	@Test
	@DisplayName("拒否系: a+b.Left(5) 括弧なし — b.Left(5) のみ変換")
	void rejectBinaryOpNoParens() throws IOException {
		String cpp = "void f() { CString s = a + b.Left(5); }";

		ConversionResult result = converter.convertSourceWithPhases(cpp, rulesByPhase);

		assertThat(result.getTransformErrors()).isEmpty();
		assertThat(result.getCsCode()).contains("Substring");
		assertThat(result.getCsCode()).contains("b");
	}
}
