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

import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionToken;
import io.github.takahino.cpp2csharp.tree.AstNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DynamicRuleGenerator テスト")
class DynamicRuleGeneratorTest {

	private DynamicRuleGenerator generator;
	private DynamicRuleLoader loader;
	private ConversionRuleLoader ruleLoader;

	@BeforeEach
	void setUp() {
		ruleLoader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());
		generator = new DynamicRuleGenerator(ruleLoader);
		loader = new DynamicRuleLoader(CppParserFactory.asLexerFactory());
	}

	/** トークン文字列列から AstNode リストを生成するヘルパー。 */
	private List<AstNode> nodes(String... tokens) {
		List<AstNode> result = new java.util.ArrayList<>();
		for (int i = 0; i < tokens.length; i++) {
			result.add(AstNode.tokenNode(tokens[i], 1, i));
		}
		return result;
	}

	@Test
	@DisplayName("collect パターンで単一トークンを収集してルールを生成する")
	void generateFromSingleCollectedValue() {
		// collect: ABSTRACT_PARAM00 :: → "MyClass" を収集
		DynamicRuleSpec spec = loader.parse("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""", "test.drule");

		List<AstNode> tokenNodes = nodes("void", "MyClass", "::", "MyMethod", "(", ")");

		List<ConversionRule> rules = generator.generate(tokenNodes, List.of(spec));

		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getToTemplate()).isEqualTo("private void ABSTRACT_PARAM00()");
		// from パターンに "MyClass" が含まれる
		List<String> fromTexts = rules.get(0).getFromTokens().stream().map(ConversionToken::getValue).toList();
		assertThat(fromTexts).contains("MyClass");
		assertThat(fromTexts).contains("void");
		assertThat(fromTexts).doesNotContain("COLLECTED");
	}

	@Test
	@DisplayName("複数の収集値それぞれにルールを生成する")
	void generateForMultipleCollectedValues() {
		DynamicRuleSpec spec = loader.parse("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""", "test.drule");

		// ClassA と ClassB の両方が :: の前に出現する
		List<AstNode> tokenNodes = nodes("void", "ClassA", "::", "MethodA", "(", ")", "void", "ClassB", "::", "MethodB",
				"(", ")");

		List<ConversionRule> rules = generator.generate(tokenNodes, List.of(spec));

		assertThat(rules).hasSize(2);
		List<String> fromFirstTokens = rules.stream().flatMap(r -> r.getFromTokens().stream())
				.map(ConversionToken::getValue).toList();
		assertThat(fromFirstTokens).contains("ClassA");
		assertThat(fromFirstTokens).contains("ClassB");
	}

	@Test
	@DisplayName("同じ収集値は重複排除される")
	void deduplicatesCollectedValues() {
		DynamicRuleSpec spec = loader.parse("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""", "test.drule");

		// MyClass が2回出現しても1つのルールのみ生成
		List<AstNode> tokenNodes = nodes("void", "MyClass", "::", "Method1", "(", ")", "void", "MyClass", "::",
				"Method2", "(", ")");

		List<ConversionRule> rules = generator.generate(tokenNodes, List.of(spec));

		assertThat(rules).hasSize(1);
	}

	@Test
	@DisplayName("複数テンプレートがある場合、収集値ごとに全テンプレートからルールを生成する")
	void generateAllTemplatesPerValue() {
		DynamicRuleSpec spec = loader.parse("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( ABSTRACT_PARAM01 )
				to: private void ABSTRACT_PARAM00(ABSTRACT_PARAM01)
				from: bool COLLECTED :: ABSTRACT_PARAM00 ( ABSTRACT_PARAM01 )
				to: private bool ABSTRACT_PARAM00(ABSTRACT_PARAM01)
				""", "test.drule");

		List<AstNode> tokenNodes = nodes("void", "MyClass", "::", "Method", "(", ")");

		List<ConversionRule> rules = generator.generate(tokenNodes, List.of(spec));

		// 1値 × 2テンプレート = 2ルール
		assertThat(rules).hasSize(2);
		assertThat(rules.get(0).getToTemplate()).contains("void");
		assertThat(rules.get(1).getToTemplate()).contains("bool");
	}

	@Test
	@DisplayName("collect パターンにマッチしない場合は空リストを返す")
	void returnsEmptyWhenNoMatch() {
		DynamicRuleSpec spec = loader.parse("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""", "test.drule");

		// :: が存在しないトークン列
		List<AstNode> tokenNodes = nodes("int", "x", "=", "1", ";");

		List<ConversionRule> rules = generator.generate(tokenNodes, List.of(spec));

		assertThat(rules).isEmpty();
	}

	@Test
	@DisplayName("ABSTRACT_PARAM00 が複数トークンにマッチする場合は収集しない")
	void ignoresMultiTokenCaptures() {
		DynamicRuleSpec spec = loader.parse("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""", "test.drule");

		// "a b ::" → ABSTRACT_PARAM00 が "a b" の2トークンになるため収集しない
		// PatternMatcher は "a" を AP00 にキャプチャし "::" にマッチする可能性があるが、
		// 単一トークンキャプチャのみを採用するフィルタで "a b ::" は除外される
		List<AstNode> tokenNodes = nodes("a", "b", "::", "Method", "(", ")");

		List<ConversionRule> rules = generator.generate(tokenNodes, List.of(spec));

		// "a" は単一トークンとして収集される（PatternMatcher が最短マッチで "a" を選ぶ可能性あり）
		// 少なくとも "a b" という2トークンキャプチャは排除される
		for (ConversionRule rule : rules) {
			rule.getFromTokens().stream()
					.filter(t -> !t.isAbstractParam() && !t.getValue().equals("void") && !t.getValue().equals("::")
							&& !t.getValue().equals("(") && !t.getValue().equals(")"))
					.forEach(t -> assertThat(t.getValue()).doesNotContain(" "));
		}
	}

	@Test
	@DisplayName("to テンプレートの COLLECTED も収集値で置換される")
	void collectedReplacedInToTemplate() {
		// enum メンバ収集: COLLECTED が to テンプレートにも現れるケース
		DynamicRuleSpec spec = loader.parse("""
				collect: , ABSTRACT_PARAM00 ,
				from: COLLECTED
				to: (int) COLLECTED
				""", "test.drule");

		List<AstNode> tokenNodes = nodes("enum", "{", "apple", ",", "banana", ",", "cherry", "}");

		List<ConversionRule> rules = generator.generate(tokenNodes, List.of(spec));

		// banana と cherry が収集される（"," に挟まれた単一トークン）
		assertThat(rules).isNotEmpty();
		rules.forEach(rule -> assertThat(rule.getToTemplate()).startsWith("(int) "));
	}

	@Test
	@DisplayName("dynamicSpecs が空の場合は空リストを返す")
	void emptySpecsReturnsEmptyRules() {
		List<AstNode> tokenNodes = nodes("void", "MyClass", "::", "Method", "(", ")");
		List<ConversionRule> rules = generator.generate(tokenNodes, List.of());
		assertThat(rules).isEmpty();
	}
}
