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
import io.github.takahino.cpp2csharp.mrule.MultiReplaceRule;
import io.github.takahino.cpp2csharp.mrule.MultiReplaceRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader.ThreePassRuleSet;
import io.github.takahino.cpp2csharp.rule.MainPhaseSubPhase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 3パス構成（pre/main/post）の統合テスト。
 *
 * <p>
 * pre/post フェーズなしの3パス変換が既存の変換と同等の結果を生成することを検証する。
 * </p>
 */
@DisplayName("3パス統合テスト")
class ThreePassIntegrationTest {

	private final CppToCSharpConverter converter = new CppToCSharpConverter();
	private final ConversionRuleLoader ruleLoader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());
	private final MultiReplaceRuleLoader mruleLoader = new MultiReplaceRuleLoader(CppParserFactory.asLexerFactory());

	private String loadCppFile(String fileName) throws IOException {
		String path = "cpp/" + fileName;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
			if (is == null) {
				throw new IOException("リソースが見つかりません: " + path);
			}
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private String normalizeCode(String code) {
		return code.replaceAll("/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "").replaceAll("\\s+", "");
	}

	@Test
	@DisplayName("pre/post フェーズなしの3パス変換は既存変換と同等")
	void testThreePassWithEmptyPrePost() throws Exception {
		String cppSource = "void f() { int x = sin(y); }";

		ConversionRule sinRule = ruleLoader
				.loadFromString("from: sin ( ABSTRACT_PARAM00 )\nto: Math.Sin(ABSTRACT_PARAM00)", "test.rule").get(0);

		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(), // no pre phases
				List.of(new MainPhaseSubPhase("test", List.of(sinRule), true)), // main phases
				List.of(), // no post phases
				List.of(), // no comby phases
				List.of() // no dynamic specs
		);

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);

		assertThat(result.getCsCode()).contains("Math.Sin");
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("pre フェーズが BOOL を bool に変換し main フェーズが後続処理できる")
	void testPrePhaseSimpleReplacement() throws Exception {
		String cppSource = "void f() { BOOL x = sin(y); }";

		// Pre phase: BOOL → bool using mrule
		String mruleContent = """
				find: BOOL
				replace: bool
				""";
		List<MultiReplaceRule> preRules = mruleLoader.loadFromString(mruleContent, "pre.mrule");

		// Main phase: sin → Math.Sin
		ConversionRule sinRule = ruleLoader
				.loadFromString("from: sin ( ABSTRACT_PARAM00 )\nto: Math.Sin(ABSTRACT_PARAM00)", "test.rule").get(0);

		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(preRules),
				List.of(new MainPhaseSubPhase("test", List.of(sinRule), true)), List.of(), List.of(), List.of());

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);

		assertThat(result.getCsCode()).contains("bool");
		assertThat(result.getCsCode()).contains("Math.Sin");
	}

	@Test
	@DisplayName("post フェーズが main 変換後にトークンを処理できる")
	void testPostPhaseSimpleReplacement() throws Exception {
		String cppSource = "void f() { int x = sin(y); }";

		// Main phase: sin → Math.Sin
		ConversionRule sinRule = ruleLoader
				.loadFromString("from: sin ( ABSTRACT_PARAM00 )\nto: Math.Sin(ABSTRACT_PARAM00)", "test.rule").get(0);

		// Post phase: transform some token after main conversion
		// Note: after retokenization, main phase output is re-split into individual
		// tokens
		// so "Math.Sin(y)" becomes "Math", ".", "Sin", "(", "y", ")"
		// Post phase can then match those individual tokens
		String mruleContent = """
				find: int
				replace: int
				""";
		List<MultiReplaceRule> postRules = mruleLoader.loadFromString(mruleContent, "post.mrule");

		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(),
				List.of(new MainPhaseSubPhase("test", List.of(sinRule), true)), List.of(postRules), List.of(),
				List.of());

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);

		// After retokenization in post phase, Math.Sin(y) is split into individual
		// tokens
		// so the output contains "Math" and "Sin" but with spaces between them
		assertThat(result.getCsCode()).contains("Math");
		assertThat(result.getCsCode()).contains("Sin");
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("3パス変換は空のルールセットでも正常に動作する")
	void testThreePassWithAllEmptyPhases() {
		String cppSource = "void f() { int x = 1; }";

		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(), List.of(), List.of(), List.of(), List.of());

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);

		// Should produce output without throwing
		assertThat(result.getCsCode()).isNotNull();
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("ThreePassRuleSet のデフォルト読み込みは既存ルールを main として使用する")
	void testDefaultThreePassRuleSetLoading() throws Exception {
		// The default loading should detect rules/main/ and load from there
		ThreePassRuleSet ruleSet = ruleLoader.loadThreePassRules();

		// With the new structure (rules/main/*), main phases should have content
		assertThat(ruleSet).isNotNull();
		assertThat(ruleSet.prePhases()).isNotNull();
		assertThat(ruleSet.mainPhaseSpecs()).isNotNull();
		assertThat(ruleSet.postPhases()).isNotNull();
		// main phases should have rules since rules/main/ exists
		assertThat(ruleSet.mainPhaseSpecs()).isNotEmpty();
	}

	@Test
	@DisplayName("pre mrule が複数行の LPSTR 宣言と malloc 代入を相関付けて正規化できる")
	void testPrePhaseMallocStringAssignmentNormalization() throws Exception {
		String cppSource = loadCppFile("test_mrule_malloc_string_assignment.cpp");
		ThreePassRuleSet ruleSet = ruleLoader.loadThreePassRules();

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);
		String normalized = normalizeCode(result.getCsCode());

		assertThat(normalized).contains("stringpszName;");
		assertThat(normalized).contains("pszName=\"\";");
		assertThat(normalized).doesNotContain("malloc");
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("skip: により宣言と代入の間に別ロジックがあっても mrule でマッチして変換される")
	void testMallocWithLogicBetween() throws Exception {
		String cppSource = loadCppFile("test_mrule_malloc_with_logic_between.cpp");
		ThreePassRuleSet ruleSet = ruleLoader.loadThreePassRules();

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);
		String normalized = normalizeCode(result.getCsCode());

		assertThat(normalized).contains("stringpszBuf;");
		assertThat(normalized).contains("pszBuf=\"\";");
		assertThat(normalized).doesNotContain("malloc");
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("同一ブロック内の複数 LPSTR 変数がそれぞれ独立して string に変換される")
	void testMultipleMallocVarsInSameBlock() throws Exception {
		String cppSource = loadCppFile("test_mrule_malloc_multiple_vars.cpp");
		ThreePassRuleSet ruleSet = ruleLoader.loadThreePassRules();

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);
		String normalized = normalizeCode(result.getCsCode());

		assertThat(normalized).contains("stringpszName;");
		assertThat(normalized).contains("pszName=\"\";");
		assertThat(normalized).contains("stringpszPath;");
		assertThat(normalized).contains("pszPath=\"\";");
		assertThat(normalized).doesNotContain("malloc");
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("malloc 代入のない LPSTR 宣言はエラーなく処理される（変換なし）")
	void testLpstrWithoutMallocNotChanged() throws Exception {
		String cppSource = loadCppFile("test_mrule_malloc_no_match.cpp");
		ThreePassRuleSet ruleSet = ruleLoader.loadThreePassRules();

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);

		assertThat(result.getCsCode()).isNotNull();
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("free() 呼び出しが mrule により削除される")
	void testFreeCallRemoval() throws Exception {
		String cppSource = "void f() { LPSTR pszName; pszName = (char*)malloc(256); free(pszName); }";
		ThreePassRuleSet ruleSet = ruleLoader.loadThreePassRules();

		ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);
		String normalized = normalizeCode(result.getCsCode());

		assertThat(normalized).doesNotContain("free(");
		assertThat(result.getTransformErrors()).isEmpty();
	}
}
