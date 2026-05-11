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

package io.github.takahino.cpp2csharp.dynamic;

import io.github.takahino.cpp2csharp.converter.ConversionResult;
import io.github.takahino.cpp2csharp.converter.CppToCSharpConverter;
import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader.ThreePassRuleSet;
import io.github.takahino.cpp2csharp.rule.ConversionToken;
import io.github.takahino.cpp2csharp.rule.MainPhaseSubPhase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 外部提供値からの動的ルール生成テスト。
 *
 * <p>
 * VC++6 の enum はすべて int 固定のため、int との直接比較・代入が可能だった。 C# 化すると明示的キャストが必要になる。
 * </p>
 *
 * <p>
 * 別処理（字句解析・別ファイル解析）で収集した enum メンバ名を
 * {@link DynamicRuleGenerator#generateFromValues} に渡し、 キャストルールを動的生成する振る舞いを検証する。
 * </p>
 */
@DisplayName("外部値からの動的ルール生成テスト（enum キャスト変換）")
class DynamicRuleFromExternalValuesTest {

	private CppToCSharpConverter converter;
	private DynamicRuleGenerator generator;

	@BeforeEach
	void setUp() {
		converter = new CppToCSharpConverter();
		generator = new DynamicRuleGenerator(new ConversionRuleLoader(CppParserFactory.asLexerFactory()));
	}

	/** 外部値リスト + テンプレートからルールを生成して 3パス変換を実行するヘルパー。 */
	private ConversionResult convertWithExternalValues(String cpp, List<String> enumMembers,
			List<DynamicRuleSpec.FromToTemplate> templates) {
		List<ConversionRule> rules = generator.generateFromValues(enumMembers, templates, "enum-cast");
		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(), List.of(new MainPhaseSubPhase("test", rules, true)),
				List.of(), List.of(), List.of());
		return converter.convertSourceThreePass(cpp, ruleSet);
	}

	// =========================================================================
	// ルール生成の単体検証
	// =========================================================================

	@Test
	@DisplayName("外部値リストからルールが正しい件数生成される")
	void generateCorrectNumberOfRules() {
		List<String> enumMembers = List.of("apple", "banana", "cherry");
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED"));

		List<ConversionRule> rules = generator.generateFromValues(enumMembers, templates, "fruit-enum");

		// 3値 × 1テンプレート = 3ルール
		assertThat(rules).hasSize(3);
	}

	@Test
	@DisplayName("生成ルールの from パターンに具体値が含まれ COLLECTED が除去される")
	void fromPatternContainsConcreteValue() {
		List<String> enumMembers = List.of("apple");
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED"));

		List<ConversionRule> rules = generator.generateFromValues(enumMembers, templates, "test");

		assertThat(rules).hasSize(1);
		List<String> fromValues = rules.get(0).getFromTokens().stream().map(ConversionToken::getValue).toList();
		assertThat(fromValues).contains("apple");
		assertThat(fromValues).doesNotContain("COLLECTED");
	}

	@Test
	@DisplayName("生成ルールの to テンプレートに具体値が含まれ COLLECTED が除去される")
	void toTemplateContainsConcreteValue() {
		List<String> enumMembers = List.of("banana");
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED"));

		List<ConversionRule> rules = generator.generateFromValues(enumMembers, templates, "test");

		assertThat(rules.get(0).getToTemplate()).isEqualTo("(int) banana");
		assertThat(rules.get(0).getToTemplate()).doesNotContain("COLLECTED");
	}

	@Test
	@DisplayName("複数テンプレートがある場合: 1値 × N テンプレート = N ルール")
	void multipleTemplatesPerValue() {
		List<String> enumMembers = List.of("apple");
		List<DynamicRuleSpec.FromToTemplate> templates = List.of(
				new DynamicRuleSpec.FromToTemplate("COLLECTED", "(FruitKind) COLLECTED"),
				new DynamicRuleSpec.FromToTemplate("= COLLECTED ;", "= (FruitKind) COLLECTED ;"));

		List<ConversionRule> rules = generator.generateFromValues(enumMembers, templates, "test");

		assertThat(rules).hasSize(2);
	}

	@Test
	@DisplayName("外部値が空の場合はルールを生成しない")
	void emptyExternalValuesProducesNoRules() {
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED"));

		List<ConversionRule> rules = generator.generateFromValues(List.of(), templates, "test");

		assertThat(rules).isEmpty();
	}

	@Test
	@DisplayName("テンプレートが空の場合はルールを生成しない")
	void emptyTemplatesProducesNoRules() {
		List<ConversionRule> rules = generator.generateFromValues(List.of("apple", "banana"), List.of(), "test");

		assertThat(rules).isEmpty();
	}

	// =========================================================================
	// 変換パイプライン統合検証
	// =========================================================================

	@Test
	@DisplayName("int との比較で使われる enum メンバがキャストに変換される")
	void enumMemberInComparisonGetsIntCast() {
		String cpp = "void f() { if ( nSel == apple ) { } }";

		// 外部解析で収集した enum メンバ
		List<String> enumMembers = List.of("apple", "banana", "cherry");
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED"));

		ConversionResult result = convertWithExternalValues(cpp, enumMembers, templates);

		assertThat(result.getCsCode()).contains("(int) apple");
		// "(int) apple" の中に "apple" は残るが、先頭に "(int) " が付いていること
		assertThat(result.getCsCode()).doesNotContain("== apple");
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("int への代入で使われる enum メンバがキャストに変換される")
	void enumMemberInAssignmentGetsIntCast() {
		String cpp = "void f() { int n = banana ; }";

		List<String> enumMembers = List.of("apple", "banana", "cherry");
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED"));

		ConversionResult result = convertWithExternalValues(cpp, enumMembers, templates);

		assertThat(result.getCsCode()).contains("(int) banana");
	}

	@Test
	@DisplayName("収集されていない識別子は変換されない")
	void nonEnumIdentifierNotConverted() {
		String cpp = "void f() { int n = pear ; }";

		// pear は列挙に含まれない
		List<String> enumMembers = List.of("apple", "banana", "cherry");
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED"));

		ConversionResult result = convertWithExternalValues(cpp, enumMembers, templates);

		assertThat(result.getCsCode()).contains("pear");
		assertThat(result.getCsCode()).doesNotContain("(int) pear");
	}

	@Test
	@DisplayName("複数の enum メンバが同一コード内に存在する場合、それぞれ変換される")
	void multipleEnumMembersInSameCode() {
		String cpp = "void f() { if ( x == apple ) { n = banana ; } }";

		List<String> enumMembers = List.of("apple", "banana", "cherry");
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED"));

		ConversionResult result = convertWithExternalValues(cpp, enumMembers, templates);

		assertThat(result.getCsCode()).contains("(int) apple");
		assertThat(result.getCsCode()).contains("(int) banana");
	}

	@Test
	@DisplayName("enum型名付きキャストテンプレート: (FruitKind) に変換される")
	void enumTypeNamedCast() {
		String cpp = "void f() { if ( x == cherry ) { } }";

		List<String> enumMembers = List.of("apple", "banana", "cherry");
		// int ではなく enum 型名付きキャスト
		List<DynamicRuleSpec.FromToTemplate> templates = List
				.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(FruitKind) COLLECTED"));

		ConversionResult result = convertWithExternalValues(cpp, enumMembers, templates);

		assertThat(result.getCsCode()).contains("(FruitKind) cherry");
	}

	@Test
	@DisplayName("外部値ルールと静的ルールが共存して両方適用される")
	void externalValuesAndStaticRulesCoexist() {
		// sin と apple を別文に分けることで、フェーズ1の sin→Math.Sin 変換後も
		// apple が独立トークンとして残りフェーズ2の enum キャストが適用される
		String cpp = "void f() { double d = sin ( x ) ; int n = apple ; }";

		// 静的ルール: sin → Math.Sin
		ConversionRule sinRule = new ConversionRuleLoader(CppParserFactory.asLexerFactory())
				.loadFromString("from: sin ( ABSTRACT_PARAM00 )\nto: Math.Sin(ABSTRACT_PARAM00)", "sin.rule").get(0);

		// 外部値ルール: apple → (int) apple
		List<ConversionRule> enumRules = generator.generateFromValues(List.of("apple", "banana"),
				List.of(new DynamicRuleSpec.FromToTemplate("COLLECTED", "(int) COLLECTED")), "fruit-enum");

		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(),
				List.of(new MainPhaseSubPhase("test-1", List.of(sinRule), true), // 2フェーズ
						new MainPhaseSubPhase("test-2", enumRules, true)),
				List.of(), List.of(), List.of());

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSet);

		// sin は Math.Sin に変換、apple は (int) apple に変換
		assertThat(result.getCsCode()).contains("Math.Sin");
		assertThat(result.getCsCode()).contains("(int) apple");
	}
}
